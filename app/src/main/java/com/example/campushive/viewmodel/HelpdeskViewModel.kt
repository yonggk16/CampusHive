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

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val localOptimisticReports = mutableListOf<HelpReport>()

    init {
        loadUserReports()
    }

    /**
     * Connect real-time stream of reports submitted by the active user
     */
    fun loadUserReports() {
        viewModelScope.launch {
            repository.getUserReportsFlow().collect { remoteReports ->
                // Merge remote reports with local optimistic reports to prevent race conditions
                val remoteDescriptions = remoteReports.map { "${it.targetId}_${it.description}" }.toSet()
                val keptOptimistic = localOptimisticReports.filter {
                    "${it.targetId}_${it.description}" !in remoteDescriptions
                }
                _userReports.value = keptOptimistic + remoteReports
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
        val reporterName = currentUser?.displayName.orEmpty().ifBlank { "Student User" }

        val newReport = HelpReport(
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

        viewModelScope.launch {
            val success = repository.submitReport(newReport)
            _isSubmitting.value = false
            onResult(success)
        }
    }

    /**
     * Update an existing report
     */
    fun updateReport(reportId: String, category: String, description: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.updateReport(reportId, category, description)
            if (success) {
                _userReports.value = _userReports.value.map {
                    if (it.reportId == reportId) it.copy(category = category, description = description) else it
                }
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
                localOptimisticReports.removeIf { it.reportId == reportId }
            }
            onResult(success)
        }
    }
}
