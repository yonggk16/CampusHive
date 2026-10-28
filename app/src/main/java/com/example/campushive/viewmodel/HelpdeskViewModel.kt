package com.example.campushive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushive.data.FirestoreRepository
import com.example.campushive.model.HelpReport
import com.example.campushive.model.ReportStatus
import com.example.campushive.model.ReportType
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HelpdeskViewModel(
    private val repository: FirestoreRepository = FirestoreRepository(),
) : ViewModel() {

    private val _userReports = MutableStateFlow<List<HelpReport>>(emptyList())
    val userReports: StateFlow<List<HelpReport>> = _userReports.asStateFlow()

    private val _allReports = MutableStateFlow<List<HelpReport>>(emptyList())
    val allReports: StateFlow<List<HelpReport>> = _allReports.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isModeratorMode = MutableStateFlow(checkDefaultModeratorAccess())
    val isModeratorMode: StateFlow<Boolean> = _isModeratorMode.asStateFlow()

    private val localOptimisticReports = mutableListOf<HelpReport>()

    companion object {
        private val MODERATOR_EMAILS = setOf(
            "yonggk07@helplive.edu.my",
            "teammate2@gmail.com",
            "b2500904@helplive.edu.my",
            "b2501286@helplive.edu.my"
        )

        fun isModeratorEmail(email: String?): Boolean {
            val normalized = email?.trim()?.lowercase().orEmpty()
            if (normalized.isBlank()) return false
            return normalized in MODERATOR_EMAILS
        }
    }

    init {
        loadUserReports()
        loadAllReports()
    }

    private fun checkDefaultModeratorAccess(): Boolean {
        val email = Firebase.auth.currentUser?.email
        return isModeratorEmail(email)
    }

    fun refreshModeratorAccess() {
        if (checkDefaultModeratorAccess()) {
            _isModeratorMode.value = true
        }
    }

    fun toggleModeratorMode() {
        _isModeratorMode.value = !_isModeratorMode.value
    }

    /**
     * Connect real-time stream of reports submitted by the active user
     */
    fun loadUserReports() {
        viewModelScope.launch {
            repository.getUserReportsFlow().collect { remoteReports ->
                val remoteDescriptions = remoteReports.map { "${it.targetId}_${it.description}" }.toSet()
                val keptOptimistic = localOptimisticReports.filter {
                    "${it.targetId}_${it.description}" !in remoteDescriptions
                }
                _userReports.value = keptOptimistic + remoteReports
            }
        }
    }

    /**
     * Connect real-time stream of ALL campus reports for Platform Moderators
     */
    fun loadAllReports() {
        viewModelScope.launch {
            repository.getAllReportsFlow().collect { remoteReports ->
                val remoteDescriptions = remoteReports.map { "${it.targetId}_${it.description}" }.toSet()
                val keptOptimistic = localOptimisticReports.filter {
                    "${it.targetId}_${it.description}" !in remoteDescriptions
                }
                _allReports.value = keptOptimistic + remoteReports
            }
        }
    }

    /**
     * Submit a new problem or safety report to Cloud Firestore
     */
    fun submitReport(
        type: ReportType = ReportType.PROBLEM,
        category: String = "General",
        targetId: String = "",
        targetName: String = "",
        description: String = "",
        onResult: (Boolean) -> Unit = {}
    ) {
        if (_isSubmitting.value) return
        _isSubmitting.value = true

        val currentUser = Firebase.auth.currentUser
        val reporterUid = currentUser?.uid.orEmpty()
        val reporterName = currentUser?.displayName.orEmpty().ifBlank {
            currentUser?.email?.substringBefore("@").orEmpty().ifBlank { "Student User" }
        }

        val newReport = HelpReport(
            reportId = "local_${System.currentTimeMillis()}",
            reporterUid = reporterUid,
            reporterName = reporterName,
            type = type,
            category = category,
            targetId = targetId,
            targetName = targetName,
            description = description.trim(),
            status = ReportStatus.OPEN
        )

        localOptimisticReports.add(newReport)
        // Optimistic UI update
        _userReports.value = listOf(newReport) + _userReports.value
        _allReports.value = listOf(newReport) + _allReports.value

        viewModelScope.launch {
            val success = repository.submitReport(newReport.copy(reportId = ""))
            _isSubmitting.value = false
            onResult(success)
        }
    }

    /**
     * Update an existing report (Student edit)
     */
    fun updateReport(reportId: String, category: String, description: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.updateReport(reportId, category, description)
            if (success) {
                _userReports.value = _userReports.value.map {
                    if (it.reportId == reportId) it.copy(category = category, description = description) else it
                }
                _allReports.value = _allReports.value.map {
                    if (it.reportId == reportId) it.copy(category = category, description = description) else it
                }
            }
            onResult(success)
        }
    }

    /**
     * Moderator action: update ticket status and attach official moderator response/resolution note
     */
    fun moderateReport(
        reportId: String,
        newStatus: ReportStatus,
        moderatorNote: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        val currentUser = Firebase.auth.currentUser
        val moderatorName = currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Campus Moderator"

        // Optimistic UI update
        val cleanNote = moderatorNote.trim()
        _allReports.value = _allReports.value.map {
            if (it.reportId == reportId) it.copy(status = newStatus, moderatorNote = cleanNote, resolvedBy = moderatorName) else it
        }
        _userReports.value = _userReports.value.map {
            if (it.reportId == reportId) it.copy(status = newStatus, moderatorNote = cleanNote, resolvedBy = moderatorName) else it
        }

        viewModelScope.launch {
            val success = repository.updateReportModeration(
                reportId = reportId,
                status = newStatus,
                moderatorNote = cleanNote,
                resolvedBy = moderatorName
            )
            onResult(success)
        }
    }

    /**
     * Moderator action: pause ("INACTIVE"), restore ("ACTIVE"), or remove ("DELETED") a flagged listing
     */
    fun moderateListingStatus(
        listingId: String,
        newStatus: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        if (listingId.isBlank()) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            val success = if (newStatus == "DELETED") {
                repository.deleteListing(listingId)
            } else {
                repository.updateListingStatus(listingId, newStatus)
            }
            onResult(success)
        }
    }

    /**
     * Delete an existing report
     */
    fun deleteReport(reportId: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.deleteReport(reportId)
            if (success) {
                _userReports.value = _userReports.value.filterNot { it.reportId == reportId }
                _allReports.value = _allReports.value.filterNot { it.reportId == reportId }
                localOptimisticReports.removeIf { it.reportId == reportId }
            }
            onResult(success)
        }
    }
}
