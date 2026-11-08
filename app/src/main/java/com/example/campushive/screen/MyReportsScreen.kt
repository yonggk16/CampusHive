package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.example.campushive.ui.theme.bounceClick
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.model.HelpReport
import com.example.campushive.model.ReportStatus
import com.example.campushive.model.ReportType
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.AuthViewModel
import com.example.campushive.viewmodel.HelpdeskViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    helpdeskViewModel: HelpdeskViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    initialModeratorTab: Boolean = false,
    onBackClick: () -> Unit,
    onReportClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val userReports by helpdeskViewModel.userReports.collectAsState()
    val allReports by helpdeskViewModel.allReports.collectAsState()
    val isModeratorMode by helpdeskViewModel.isModeratorMode.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isDynamicModerator = userProfile?.isModerator == true || HelpdeskViewModel.isModeratorEmail(Firebase.auth.currentUser?.email)

    LaunchedEffect(userProfile) {
        helpdeskViewModel.refreshModeratorAccess(userProfile)
        helpdeskViewModel.loadAllReports(userProfile)
        helpdeskViewModel.loadUserReports()
    }

    var showAllCampusQueue by remember(initialModeratorTab, isDynamicModerator) {
        mutableStateOf(initialModeratorTab && isDynamicModerator)
    }
    var selectedFilter by remember { mutableStateOf("All") }

    val baseReports = if (showAllCampusQueue && isDynamicModerator && isModeratorMode) allReports else userReports

    val filteredReports = remember(baseReports, selectedFilter, showAllCampusQueue, isModeratorMode, isDynamicModerator) {
        if (!showAllCampusQueue || !isModeratorMode || !isDynamicModerator) {
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

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                authViewModel.fetchUserProfile()
                helpdeskViewModel.refreshModeratorAccess(userProfile)
                helpdeskViewModel.loadAllReports(userProfile)
                helpdeskViewModel.loadUserReports()
                delay(800)
                isRefreshing = false
                Toast.makeText(context, "Tickets refreshed", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HoneyBottomGradient)
                .statusBarsPadding()
        ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spacing.gutter, end = Spacing.gutter, top = Spacing.topInset, bottom = Spacing.sm),
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

        // Moderator Segmented Switcher ("My Tickets" vs "All Campus Queue") - ONLY visible for Platform Moderators
        if (isDynamicModerator && isModeratorMode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = Radius.Inner,
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
                                    .clip(Radius.Tile)
                                    .background(if (isSelected) Charcoal else Color.Transparent)
                                    .bounceClick { showAllCampusQueue = isQueueTab }
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
                                shape = Radius.Card,
                                color = if (selected) Charcoal else CardWhite,
                                border = BorderStroke(1.dp, if (selected) Charcoal else Hairline),
                                modifier = Modifier.bounceClick { selectedFilter = filter }
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) CardWhite else Charcoal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (filteredReports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp, start = 32.dp, end = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (showAllCampusQueue) "No matching reports in queue" else "No active reports",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (showAllCampusQueue) "No campus reports match the '$selectedFilter' filter right now."
                        else "You haven't submitted any support or safety tickets yet.",
                        fontSize = 14.sp,
                        color = MutedText,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
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
                        showReporterInfo = showAllCampusQueue && isModeratorMode && isDynamicModerator,
                        onReportClick = onReportClick
                    )
                }
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
        ReportType.LISTING_FLAG -> Triple("LISTING FLAG", DangerBg, DangerFg)
        ReportType.USER_SAFETY -> Triple("USER SAFETY", WarningBgSoft, DangerFg)
        ReportType.PROBLEM -> Triple("HELPDESK", NeutralBg, Charcoal)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick { onReportClick(report.reportId) },
        shape = Radius.Inner,
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
                    Surface(color = typeBg, shape = Radius.Tag) {
                        Text(
                            text = typeLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
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
                    ReportStatus.UNDER_REVIEW -> Triple("UNDER REVIEW", WarningBg, WarningFg)
                    ReportStatus.RESOLVED -> Triple("RESOLVED", SuccessBgSoft, SuccessFg)
                    ReportStatus.CLOSED -> Triple("CLOSED", NeutralBg, MutedText)
                }

                Surface(color = bgColor, shape = Radius.Tag) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
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
                    color = Charcoal
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
                    color = SuccessBg,
                    shape = Radius.Tile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Moderator Note: ${report.moderatorNote}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SuccessFg,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
