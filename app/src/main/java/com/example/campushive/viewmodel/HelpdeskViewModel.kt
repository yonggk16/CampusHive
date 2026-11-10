package com.example.campushive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campushive.data.FirestoreRepository
import com.example.campushive.model.HelpReport
import com.example.campushive.model.ReportStatus
import com.example.campushive.model.ReportType
import com.example.campushive.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HelpdeskViewModel(
    private val repository: FirestoreRepository = FirestoreRepository(),
) : ViewModel() {

    // ==========================================================================
    // 1. REACTIVE STATE HOLDERS & INITIALIZATION
    // ==========================================================================
    private val _userReports = MutableStateFlow<List<HelpReport>>(emptyList())
    val userReports: StateFlow<List<HelpReport>> = _userReports.asStateFlow()

    private val _allReports = MutableStateFlow<List<HelpReport>>(emptyList())
    val allReports: StateFlow<List<HelpReport>> = _allReports.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isModeratorMode = MutableStateFlow(checkDefaultModeratorAccess())
    val isModeratorMode: StateFlow<Boolean> = _isModeratorMode.asStateFlow()

    private val localOptimisticReports = mutableListOf<HelpReport>()
    private var cachedUserProfile: UserProfile? = null

    init {
        loadUserReports()
        loadAllReports()
    }

    // ==========================================================================
    // 2. MODERATOR PRIVILEGE VERIFICATION & ACCESS CONTROL
    // ==========================================================================

    fun isModeratorUser(email: String?, userProfile: UserProfile? = null): Boolean {
        val targetProfile = userProfile ?: cachedUserProfile
        if (targetProfile?.isModerator == true) return true
        return isModeratorEmail(email)
    }

    fun checkDefaultModeratorAccess(userProfile: UserProfile? = null): Boolean {
        if (userProfile != null) cachedUserProfile = userProfile
        val email = Firebase.auth.currentUser?.email
        return isModeratorUser(email, userProfile ?: cachedUserProfile)
    }

    /**
     * Verifies current user's email or Firestore profile for dynamic moderator access
     */
    fun refreshModeratorAccess(userProfile: UserProfile? = null) {
        if (userProfile != null) cachedUserProfile = userProfile
        val hasAccess = checkDefaultModeratorAccess(userProfile)
        _isModeratorMode.value = hasAccess
        if (hasAccess) {
            loadAllReports(userProfile)
        } else {
            _allReports.value = emptyList()
        }
    }

    /**
     * Toggles moderator mode view state for authorized platform moderators
     */
    fun toggleModeratorMode(userProfile: UserProfile? = null) {
        if (userProfile != null) cachedUserProfile = userProfile
        if (!checkDefaultModeratorAccess(userProfile)) {
            _isModeratorMode.value = false
            _allReports.value = emptyList()
            return
        }
        val newMode = !_isModeratorMode.value
        _isModeratorMode.value = newMode
        if (newMode) {
            loadAllReports(userProfile)
        }
    }

    // ==========================================================================
    // 3. REAL-TIME TICKET STREAMS
    // ==========================================================================

    /**
     * Connects real-time Firestore stream of reports submitted by the active user
     */
    fun loadUserReports() {
        val currentUid = Firebase.auth.currentUser?.uid.orEmpty()
        if (currentUid.isBlank()) {
            _userReports.value = emptyList()
            localOptimisticReports.clear()
            return
        }
        _userReports.value = emptyList() // Resets state on user switch
        localOptimisticReports.clear()
        viewModelScope.launch {
            repository.getUserReportsFlow(currentUid).collect { remoteReports ->
                val remoteDescriptions = remoteReports.map { "${it.targetId}_${it.description}" }.toSet()
                val keptOptimistic = localOptimisticReports.filter {
                    "${it.targetId}_${it.description}" !in remoteDescriptions
                }
                _userReports.value = keptOptimistic + remoteReports
            }
        }
    }

    /**
     * Connects real-time Firestore stream of ALL campus reports for Platform Moderators
     */
    fun loadAllReports(userProfile: UserProfile? = null) {
        if (userProfile != null) cachedUserProfile = userProfile
        if (!checkDefaultModeratorAccess(userProfile ?: cachedUserProfile)) {
            _allReports.value = emptyList()
            return
        }
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

    // ==========================================================================
    // 4. STUDENT REPORT CRUD & OPTIMISTIC UPDATES
    // ==========================================================================

    /**
     * Submits a new problem ticket or safety report to Cloud Firestore
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
        // Instant local optimistic UI update
        _userReports.value = listOf(newReport) + _userReports.value
        _allReports.value = listOf(newReport) + _allReports.value

        viewModelScope.launch {
            val success = repository.submitReport(newReport.copy(reportId = ""))
            _isSubmitting.value = false
            onResult(success)
        }
    }

    /**
     * Updates an existing report description and category
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
     * Deletes an existing report ticket
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

    // ==========================================================================
    // 5. MODERATOR CONSOLE & ENFORCEMENT ACTIONS
    // ==========================================================================

    /**
     * Moderator action: Updates ticket status and attaches official moderator resolution note
     */
    fun moderateReport(
        reportId: String,
        newStatus: ReportStatus,
        moderatorNote: String,
        targetId: String = "",
        onResult: (Boolean) -> Unit = {}
    ) {
        val currentUser = Firebase.auth.currentUser
        val moderatorName = currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Campus Moderator"

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
            if (cleanNote.contains("paused", ignoreCase = true) || cleanNote.contains("pause", ignoreCase = true)) {
                if (targetId.isNotBlank()) {
                    repository.updateListingStatus(targetId, "INACTIVE")
                }
            }
            onResult(success)
        }
    }

    /**
     * Moderator action: Pauses ("INACTIVE"), restores ("ACTIVE"), or removes ("DELETED") a flagged listing
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

    // ==========================================================================
    // 6. MODERATOR EMAIL WHITELIST
    // ==========================================================================

    companion object {
        private val MODERATOR_EMAILS = setOf(
            "yonggk07@gmail.com",
            "b2501286@helplive.edu.my"
        )

        fun isModeratorEmail(email: String?): Boolean {
            val normalized = email?.trim()?.lowercase().orEmpty()
            if (normalized.isBlank()) return false
            return normalized in MODERATOR_EMAILS
        }
    }
}
