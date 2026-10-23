package com.example.campushive.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.model.HelpReport
import com.example.campushive.model.ReportStatus
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.HelpdeskViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MyReportsScreen(
    helpdeskViewModel: HelpdeskViewModel = viewModel(),
    onBackClick: () -> Unit,
    onReportClick: (String) -> Unit,
) {
    val userReports by helpdeskViewModel.userReports.collectAsState()

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
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Charcoal)
            }
            Text(
                text = "My Reports & Tickets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )
        }

        HorizontalDivider(color = Hairline)

        if (userReports.isEmpty()) {
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
                                Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = MutedText, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("No active reports", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Charcoal)
                            Spacer(Modifier.height(2.dp))
                            Text("You haven't submitted any support or safety tickets yet.", fontSize = 12.sp, color = MutedText)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(userReports) { report ->
                    ReportTicketItem(report = report, onReportClick = onReportClick)
                }
            }
        }
    }
}

@Composable
private fun ReportTicketItem(
    report: HelpReport,
    onReportClick: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = remember(report.createdAt) {
        dateFormat.format(report.createdAt.toDate())
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
                    Text(
                        text = report.category,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Charcoal
                    )
                    Text(
                        text = "• $formattedDate",
                        fontSize = 11.sp,
                        color = MutedText
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
                    color = MutedText,
                    maxLines = 2
                )
            }
        }
    }
}
