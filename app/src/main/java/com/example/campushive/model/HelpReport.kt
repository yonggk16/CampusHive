package com.example.campushive.model

import com.google.firebase.Timestamp

enum class ReportType {
    PROBLEM,
    USER_SAFETY,
    LISTING_FLAG
}

enum class ReportStatus {
    OPEN,
    UNDER_REVIEW,
    RESOLVED,
    CLOSED
}

data class HelpReport(
    val reportId: String = "",
    val reporterUid: String = "",
    val reporterName: String = "",
    val type: ReportType = ReportType.PROBLEM,
    val category: String = "",
    val targetId: String = "",
    val targetName: String = "",
    val description: String = "",
    val status: ReportStatus = ReportStatus.OPEN,
    val createdAt: Timestamp = Timestamp.now()
)
