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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
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
    onViewListing: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val userReports by helpdeskViewModel.userReports.collectAsState()
    val allReports by helpdeskViewModel.allReports.collectAsState()
    val isModeratorMode by helpdeskViewModel.isModeratorMode.collectAsState()

    val report = remember(userReports, allReports, reportId) {
        (allReports + userReports).find { it.reportId == reportId }
    }

    var isEditing by remember { mutableStateOf(false) }
    val editedCategory = remember(report) { mutableStateOf(report?.category ?: "General") }
    val editedDescription = remember(report) { mutableStateOf(report?.description ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // Moderator Controls State
    var selectedModStatus by remember(report) { mutableStateOf(report?.status ?: ReportStatus.OPEN) }
    var moderatorNoteInput by remember(report) { mutableStateOf(report?.moderatorNote ?: "") }
    var isModerating by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(report?.createdAt) {
        report?.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Just now"
    }

    if (report == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HoneyBottomGradient)
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Report ticket not found or deleted.", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Charcoal)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBackClick, colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite)) {
                Text("Go Back", fontWeight = FontWeight.Bold, color = CardWhite)
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
            .background(HoneyBottomGradient)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 18.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Charcoal)
                }
                Text(
                    text = if (isModeratorMode) "Moderate Ticket" else "Report Ticket Details",
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
                        Text("Reported by", fontSize = 12.sp, color = MutedText)
                        Text(report.reporterName.ifBlank { "Student User" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                    }

                    Spacer(Modifier.height(6.dp))

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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Target Subject", fontSize = 12.sp, color = MutedText)
                            Text(
                                text = report.targetName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6C5CE7)
                            )
                        }
                    }
                }
            }

            // Official Moderator Resolution Card (Shown to Students & Moderators when a note exists)
            if (report.moderatorNote.isNotBlank() || report.resolvedBy.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEBFBF3)),
                    border = BorderStroke(1.dp, Color(0xFFA6E9C5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF006644),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "OFFICIAL MODERATOR RESPONSE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006644),
                                letterSpacing = 0.8.sp
                            )
                        }
                        if (report.moderatorNote.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = report.moderatorNote,
                                fontSize = 13.sp,
                                color = Charcoal,
                                lineHeight = 18.sp
                            )
                        }
                        if (report.resolvedBy.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Handled by: ${report.resolvedBy}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF006644)
                            )
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
                                    color = if (isSelected) Charcoal else CardWhite,
                                    contentColor = if (isSelected) CardWhite else Charcoal,
                                    border = BorderStroke(1.dp, if (isSelected) Charcoal else Hairline),
                                    modifier = Modifier.clickable { editedCategory.value = cat }
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CardWhite else Charcoal,
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
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CardWhite, strokeWidth = 2.dp)
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

            // MODERATOR CONSOLE & ENFORCEMENT CARD (Visible in Moderator Mode)
            if (isModeratorMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.5.dp, Charcoal)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Charcoal,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Honey,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "MODERATOR CONSOLE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MutedText,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Review & Enforcement Actions",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 1. Update Status Selector
                        Text(
                            text = "SET TICKET STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ReportStatus.entries.forEach { statusOption ->
                                val isSelected = selectedModStatus == statusOption
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) Charcoal else CardWhite,
                                    contentColor = if (isSelected) CardWhite else Charcoal,
                                    border = BorderStroke(1.dp, if (isSelected) Charcoal else Hairline),
                                    modifier = Modifier.clickable { selectedModStatus = statusOption }
                                ) {
                                    Text(
                                        text = statusOption.name.replace("_", " "),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) CardWhite else Charcoal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 2. Quick Resolution Templates
                        Text(
                            text = "QUICK RESOLUTION TEMPLATES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(Modifier.height(8.dp))

                        val quickTemplates = listOf(
                            "Under investigation by CampusHive moderators." to ReportStatus.UNDER_REVIEW,
                            "Flagged listing has been paused pending provider verification." to ReportStatus.RESOLVED,
                            "Warning issued to reported user per campus safety guidelines." to ReportStatus.RESOLVED,
                            "Issue resolved. Thank you for helping keep CampusHive safe!" to ReportStatus.RESOLVED
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            quickTemplates.forEach { (templateText, suggestedStatus) ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Cream,
                                    border = BorderStroke(1.dp, Hairline),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            moderatorNoteInput = templateText
                                            selectedModStatus = suggestedStatus
                                        }
                                ) {
                                    Text(
                                        text = "• $templateText",
                                        fontSize = 11.sp,
                                        color = Charcoal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 3. Official Moderator Response Input
                        Text(
                            text = "OFFICIAL MODERATOR NOTE / RESOLUTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(Modifier.height(6.dp))

                        OutlinedTextField(
                            value = moderatorNoteInput,
                            onValueChange = { moderatorNoteInput = it },
                            placeholder = {
                                Text(
                                    text = "Add a resolution note or action summary visible to the student...",
                                    fontSize = 12.sp,
                                    color = MutedText
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(105.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Charcoal,
                                unfocusedBorderColor = Hairline,
                                focusedContainerColor = Cream,
                                unfocusedContainerColor = Cream
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = {
                                isModerating = true
                                helpdeskViewModel.moderateReport(
                                    reportId = report.reportId,
                                    newStatus = selectedModStatus,
                                    moderatorNote = moderatorNoteInput
                                ) { success ->
                                    isModerating = false
                                    if (success) {
                                        Toast.makeText(context, "Ticket updated to ${selectedModStatus.name}", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Updated locally (check Firestore rules/connection)", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isModerating,
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = Honey),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            if (isModerating) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Honey, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Save Moderation Decision", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        // 4. Target Listing Enforcement Actions (when report targets a listing)
                        if (report.type == ReportType.LISTING_FLAG || report.targetId.isNotBlank()) {
                            Spacer(Modifier.height(18.dp))
                            HorizontalDivider(color = Hairline)
                            Spacer(Modifier.height(14.dp))

                            Text(
                                text = "LISTING ENFORCEMENT ACTIONS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedText,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(Modifier.height(8.dp))

                            if (report.targetId.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { onViewListing(report.targetId) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Charcoal),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = Charcoal, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Inspect Target Listing", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Charcoal)
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Pause Listing
                                    Button(
                                        onClick = {
                                            helpdeskViewModel.moderateListingStatus(report.targetId, "INACTIVE") {
                                                selectedModStatus = ReportStatus.RESOLVED
                                                if (moderatorNoteInput.isBlank()) {
                                                    moderatorNoteInput = "Listing '${report.targetName}' has been paused by a moderator pending review."
                                                }
                                                helpdeskViewModel.moderateReport(
                                                    reportId = report.reportId,
                                                    newStatus = ReportStatus.RESOLVED,
                                                    moderatorNote = moderatorNoteInput
                                                )
                                                Toast.makeText(context, "Listing paused & ticket marked RESOLVED", Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF3E0), contentColor = Color(0xFFE65100)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.PauseCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Pause Listing", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }

                                    // Restore Listing
                                    Button(
                                        onClick = {
                                            helpdeskViewModel.moderateListingStatus(report.targetId, "ACTIVE") {
                                                Toast.makeText(context, "Listing restored to ACTIVE", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3FCEF), contentColor = Color(0xFF006644)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.PlayCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Restore Active", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Remove Listing
                                Button(
                                    onClick = {
                                        helpdeskViewModel.moderateListingStatus(report.targetId, "DELETED") {
                                            selectedModStatus = ReportStatus.RESOLVED
                                            moderatorNoteInput = "Listing '${report.targetName}' was removed for violating CampusHive guidelines."
                                            helpdeskViewModel.moderateReport(
                                                reportId = report.reportId,
                                                newStatus = ReportStatus.RESOLVED,
                                                moderatorNote = moderatorNoteInput
                                            )
                                            Toast.makeText(context, "Listing removed from CampusHive & ticket resolved", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFC62828)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Remove Listing From Platform", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
