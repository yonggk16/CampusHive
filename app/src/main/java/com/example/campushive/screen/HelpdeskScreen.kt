package com.example.campushive.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Security
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
import com.example.campushive.components.ReportDialog
import com.example.campushive.model.ReportStatus
import com.example.campushive.model.ReportType
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.HelpdeskViewModel

private data class FaqItem(val question: String, val answer: String)

private val faqList = listOf(
    FaqItem(
        question = "How do payments work?",
        answer = "CampusHive doesn't process payments directly. Agree a price in chat, then pay your peer directly (cash or bank transfer) after the service is done."
    ),
    FaqItem(
        question = "How are ratings calculated?",
        answer = "Ratings are calculated from the average star score submitted by verified students after completing a tutoring or service session."
    ),
    FaqItem(
        question = "How do I report a user or listing?",
        answer = "Tap the 'Safety concern? Report a user' link or flag icon to report inappropriate behavior directly to our campus moderation team."
    ),
    FaqItem(
        question = "Editing or pausing my listing",
        answer = "Go to your Profile tab under 'My listings' and tap EDIT to modify title, pricing, or pause your active service."
    )
)

@Composable
fun HelpdeskScreen(
    helpdeskViewModel: HelpdeskViewModel = viewModel(),
    onBackClick: () -> Unit,
    onNavigateToMyReports: () -> Unit = {},
    onNavigateToModeratorQueue: () -> Unit = {}
) {
    val userReports by helpdeskViewModel.userReports.collectAsState()
    val allReports by helpdeskViewModel.allReports.collectAsState()
    val isModeratorMode by helpdeskViewModel.isModeratorMode.collectAsState()

    LaunchedEffect(Unit) {
        helpdeskViewModel.refreshModeratorAccess()
    }

    var showReportDialog by remember { mutableStateOf(false) }
    var reportDialogType by remember { mutableStateOf(ReportType.PROBLEM) }

    val openReportsCount = remember(allReports) {
        allReports.count { it.status == ReportStatus.OPEN || it.status == ReportStatus.UNDER_REVIEW }
    }
    val flaggedListingsCount = remember(allReports) {
        allReports.count { it.type == ReportType.LISTING_FLAG }
    }
    val safetyReportsCount = remember(allReports) {
        allReports.count { it.type == ReportType.USER_SAFETY }
    }

    if (showReportDialog) {
        ReportDialog(
            initialType = reportDialogType,
            helpdeskViewModel = helpdeskViewModel,
            onDismiss = { showReportDialog = false },
            onSubmitSuccess = { showReportDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
    ) {
        // Top Header with Moderator Mode Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Charcoal)
                }
                Text(
                    text = "Help & Support",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            }

            // Moderator Role Pill Toggle
            Surface(
                color = if (isModeratorMode) Charcoal else CardWhite,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (isModeratorMode) Charcoal else Hairline),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable { helpdeskViewModel.toggleModeratorMode() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = "Moderator Mode",
                        tint = if (isModeratorMode) Honey else MutedText,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (isModeratorMode) "Moderator ON" else "Moderator View",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isModeratorMode) CardWhite else Charcoal
                    )
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
            // Moderator Portal Card (Visible when Moderator Mode is active)
            AnimatedVisibility(visible = isModeratorMode) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToModeratorQueue() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Charcoal)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = Honey,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "CAMPUSHIVE MODERATOR PORTAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Honey,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            Surface(
                                color = Honey,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$openReportsCount PENDING",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Campus Incident & Report Queue",
                            style = MaterialTheme.typography.titleMedium,
                            color = CardWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Review student-reported listings, safety concerns, and technical tickets across campus.",
                            fontSize = 12.sp,
                            color = Color(0xFFD0D4DC),
                            lineHeight = 16.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        // Quick Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModeratorStatChip(
                                label = "Total",
                                count = allReports.size,
                                modifier = Modifier.weight(1f)
                            )
                            ModeratorStatChip(
                                label = "Listing Flags",
                                count = flaggedListingsCount,
                                modifier = Modifier.weight(1f)
                            )
                            ModeratorStatChip(
                                label = "User Safety",
                                count = safetyReportsCount,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = onNavigateToModeratorQueue,
                            colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = Charcoal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Moderator Queue (${allReports.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // CAMPUSHIVE HELPDESK Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = HoneyTint)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        text = "CAMPUSHIVE HELPDESK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8C6B10),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Need a hand?",
                        style = MaterialTheme.typography.titleMedium,
                        color = Charcoal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Report a problem and our team will get back to you within 24 hours.",
                        fontSize = 12.sp,
                        color = MutedText,
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            reportDialogType = ReportType.PROBLEM
                            showReportDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = Charcoal),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Report a problem", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // My Reports & Tickets Navigation Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMyReports() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, Hairline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = HoneyTint,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = Charcoal, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("My reports & tickets", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Charcoal)
                            Text("${userReports.size} submitted ticket(s)", fontSize = 12.sp, color = MutedText)
                        }
                    }

                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Charcoal, modifier = Modifier.size(16.dp))
                }
            }

            // Frequently Asked Questions
            Text("FREQUENTLY ASKED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedText, letterSpacing = 0.8.sp)

            faqList.forEach { faq ->
                FaqExpandableCard(faq = faq)
            }

            Spacer(Modifier.height(8.dp))

            // Safety Footer Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        reportDialogType = ReportType.USER_SAFETY
                        showReportDialog = true
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Safety concern? Report a user", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Charcoal)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Charcoal, modifier = Modifier.size(16.dp))
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ModeratorStatChip(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF2B2D33),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Honey
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFFD0D4DC)
            )
        }
    }
}

@Composable
private fun FaqExpandableCard(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        border = BorderStroke(1.dp, Hairline)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MutedText
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = faq.answer,
                        fontSize = 12.sp,
                        color = MutedText,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
