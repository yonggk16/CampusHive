package com.example.campushive.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.model.HelpReport
import com.example.campushive.model.ReportStatus
import com.example.campushive.model.ReportType
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.HelpdeskViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyReportsScreen(
    helpdeskViewModel: HelpdeskViewModel = viewModel(),
    initialModeratorTab: Boolean = false,
    onBackClick: () -> Unit,
    onReportClick: (String) -> Unit,
) {
    val userReports by helpdeskViewModel.userReports.collectAsState()
    val allReports by helpdeskViewModel.allReports.collectAsState()
    val isModeratorMode by helpdeskViewModel.isModeratorMode.collectAsState()

    var showAllCampusQueue by remember(initialModeratorTab) {
        mutableStateOf(initialModeratorTab)
    }
    var selectedFilter by remember { mutableStateOf("All") }

    val baseReports = if (showAllCampusQueue && isModeratorMode) allReports else userReports

    val filteredReports = remember(baseReports, selectedFilter, showAllCampusQueue, isModeratorMode) {
        if (!showAllCampusQueue || !isModeratorMode) {
            baseReports
        } else {
            when (selectedFilter) {
                "Open" -> baseReports.filter { it.status == ReportStatus.OPEN }
                "Under Review" -> baseReports.filter { it.status == ReportStatus.UNDER_REVIEW }
                "Listing Flags" -> baseReports.filter { it.type == ReportType.LISTING_FLAG }
                "User Safety" -> baseReports.filter { it.type == ReportType.USER_SAFETY }
                "Problems" -> baseReports.filter { it.type == ReportType.PROBLEM }
                "Resolved" -> baseReports.filter { it.status == ReportStatus.RESOLVED || it.status == ReportStatus.CLOSED }
                else -> baseReports
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Charcoal)
            }
            Text(
                text = if (showAllCampusQueue && isModeratorMode) "Moderator Incident Queue" else "My Reports & Tickets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )
        }

        HorizontalDivider(color = Hairline)

        // Moderator Segmented Switcher ("My Tickets" vs "All Campus Queue")
        if (isModeratorMode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        listOf(
                            false to "My Tickets (${userReports.size})",
                            true to "Moderator Queue (${allReports.size})"
                        ).forEach { (isQueueTab, label) ->
                            val isSelected = showAllCampusQueue == isQueueTab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Charcoal else Color.Transparent)
                                    .clickable { showAllCampusQueue = isQueueTab }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) CardWhite else Charcoal
                                )
                            }
                        }
                    }
                }

                // Filter Chips when in Moderator Queue
                if (showAllCampusQueue) {
                    val filters = listOf("All", "Open", "Under Review", "Listing Flags", "User Safety", "Problems", "Resolved")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEach { filter ->
                            val selected = selectedFilter == filter
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (selected) Honey else CardWhite,
                                border = BorderStroke(1.dp, if (selected) Honey else Hairline),
                                modifier = Modifier.clickable { selectedFilter = filter }
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = Charcoal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (filteredReports.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = CardWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Cream,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (showAllCampusQueue) Icons.Filled.AdminPanelSettings else Icons.Filled.SupportAgent,
                                    contentDescription = null,
                                    tint = MutedText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (showAllCampusQueue) "No matching reports in queue" else "No active reports",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Charcoal
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (showAllCampusQueue) "No campus reports match the '$selectedFilter' filter right now."
                                else "You haven't submitted any support or safety tickets yet.",
                                fontSize = 12.sp,
                                color = MutedText
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredReports) { report ->
                    ReportTicketItem(
                        report = report,
                        showReporterInfo = showAllCampusQueue && isModeratorMode,
                        onReportClick = onReportClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportTicketItem(
    report: HelpReport,
    showReporterInfo: Boolean = false,
    onReportClick: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = remember(report.createdAt) {
        dateFormat.format(report.createdAt.toDate())
    }

    val (typeLabel, typeBg, typeColor) = when (report.type) {
        ReportType.LISTING_FLAG -> Triple("LISTING FLAG", Color(0xFFFFEBEE), Color(0xFFC62828))
        ReportType.USER_SAFETY -> Triple("USER SAFETY", Color(0xFFFFF3E0), Color(0xFFE65100))
        ReportType.PROBLEM -> Triple("HELPDESK", Color(0xFFE8EAF6), Color(0xFF283593))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReportClick(report.reportId) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, Hairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(color = typeBg, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = typeLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }
                    Text(
                        text = report.category,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Charcoal
                    )
                }

                val (statusText, bgColor, textColor) = when (report.status) {
                    ReportStatus.OPEN -> Triple("OPEN", HoneyTint, Charcoal)
                    ReportStatus.UNDER_REVIEW -> Triple("UNDER REVIEW", Color(0xFFFFF3C4), Color(0xFF7A5800))
                    ReportStatus.RESOLVED -> Triple("RESOLVED", Color(0xFFE3FCEF), Color(0xFF006644))
                    ReportStatus.CLOSED -> Triple("CLOSED", Color(0xFFF1F2F4), MutedText)
                }

                Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = if (showReporterInfo) "Reported by ${report.reporterName.ifBlank { "Student" }} • $formattedDate" else "Submitted $formattedDate",
                fontSize = 11.sp,
                color = MutedText
            )

            if (report.targetName.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Target: ${report.targetName}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6C5CE7)
                )
            }

            if (report.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = report.description,
                    fontSize = 12.sp,
                    color = Charcoal,
                    maxLines = 2
                )
            }

            if (report.moderatorNote.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFEBFBF3),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Moderator Note: ${report.moderatorNote}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF006644),
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
