package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campushive.model.ReportStatus
import com.example.campushive.model.ReportType
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.HelpdeskViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportDetailScreen(
    reportId: String,
    helpdeskViewModel: HelpdeskViewModel,
    onBackClick: () -> Unit,
    onDeleteOrUpdate: () -> Unit,
) {
    val context = LocalContext.current
    val userReports by helpdeskViewModel.userReports.collectAsState()
    val report = remember(userReports, reportId) {
        userReports.find { it.reportId == reportId }
    }

    var isEditing by remember { mutableStateOf(false) }
    val editedCategory = remember(report) { mutableStateOf(report?.category ?: "General") }
    val editedDescription = remember(report) { mutableStateOf(report?.description ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(report?.createdAt) {
        report?.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Just now"
    }

    if (report == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Report ticket not found or deleted.", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Charcoal)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = Charcoal)) {
                Text("Go Back", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    val categories = when (report.type) {
        ReportType.PROBLEM -> listOf("Technical issue", "App bug", "Payment / Pricing question", "Account issue", "Other")
        ReportType.USER_SAFETY -> listOf("Harassment / Spam", "Inappropriate behavior", "Fraud / Scam", "Unsafe campus meetup", "Other")
        ReportType.LISTING_FLAG -> listOf("Misleading information", "Inappropriate content", "Duplicate listing", "Incorrect pricing", "Other")
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
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Charcoal)
                }
                Text(
                    text = "Report Ticket Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            }

            Row {
                IconButton(onClick = { isEditing = !isEditing }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Report", tint = if (isEditing) Honey else Charcoal)
                }
                IconButton(
                    onClick = {
                        isDeleting = true
                        helpdeskViewModel.deleteReport(report.reportId) { success ->
                            isDeleting = false
                            if (success) {
                                Toast.makeText(context, "Report deleted successfully", Toast.LENGTH_SHORT).show()
                                onDeleteOrUpdate()
                            } else {
                                Toast.makeText(context, "Failed to delete report", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isDeleting
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Report", tint = Color.Red)
                }
            }
        }

        HorizontalDivider(color = Hairline)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status & Date Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, Hairline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = report.type.name.replace("_", " "),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedText,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Ticket #${report.reportId.take(8)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Charcoal
                            )
                        }

                        val (statusText, bgColor, textColor) = when (report.status) {
                            ReportStatus.OPEN -> Triple("OPEN", HoneyTint, Charcoal)
                            ReportStatus.UNDER_REVIEW -> Triple("UNDER REVIEW", Color(0xFFFFF3C4), Color(0xFF7A5800))
                            ReportStatus.RESOLVED -> Triple("RESOLVED", Color(0xFFE3FCEF), Color(0xFF006644))
                            ReportStatus.CLOSED -> Triple("CLOSED", Color(0xFFF1F2F4), MutedText)
                        }

                        Surface(color = bgColor, shape = RoundedCornerShape(10.dp)) {
                            Text(
                                text = statusText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Hairline)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Submitted on", fontSize = 12.sp, color = MutedText)
                        Text(formattedDate, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Charcoal)
                    }

                    if (report.targetName.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Target Subject", fontSize = 12.sp, color = MutedText)
                            Text(report.targetName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6C5CE7))
                        }
                    }
                }
            }

            // Category & Description Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, Hairline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CATEGORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    if (isEditing) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { cat ->
                                val isSelected = editedCategory.value == cat
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) Honey else Cream,
                                    border = BorderStroke(1.dp, if (isSelected) Honey else Hairline),
                                    modifier = Modifier.clickable { editedCategory.value = cat }
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = Charcoal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            color = Cream,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Hairline)
                        ) {
                            Text(
                                text = report.category,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "DESCRIPTION & DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = editedDescription.value,
                            onValueChange = { editedDescription.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Honey,
                                unfocusedBorderColor = Hairline,
                                focusedContainerColor = Cream,
                                unfocusedContainerColor = Cream
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (editedDescription.value.isBlank()) {
                                    Toast.makeText(context, "Description cannot be empty", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSaving = true
                                helpdeskViewModel.updateReport(
                                    reportId = report.reportId,
                                    category = editedCategory.value,
                                    description = editedDescription.value
                                ) { success ->
                                    isSaving = false
                                    isEditing = false
                                    if (success) {
                                        Toast.makeText(context, "Report updated successfully", Toast.LENGTH_SHORT).show()
                                        onDeleteOrUpdate()
                                    } else {
                                        Toast.makeText(context, "Failed to update report", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = Charcoal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Charcoal, strokeWidth = 2.dp)
                            } else {
                                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    } else {
                        Surface(
                            color = Cream,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Hairline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = report.description.ifBlank { "No additional details provided." },
                                fontSize = 13.sp,
                                color = Charcoal,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
