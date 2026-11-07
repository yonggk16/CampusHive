package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import com.example.campushive.components.ReportDialog
import com.example.campushive.model.ReportStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.campushive.model.ReportType
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.AuthViewModel
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
    authViewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit,
    onNavigateToMyReports: () -> Unit = {},
    onNavigateToModeratorQueue: () -> Unit = {}
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

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                authViewModel.fetchUserProfile()
                helpdeskViewModel.refreshModeratorAccess(userProfile)
                helpdeskViewModel.loadAllReports(userProfile)
                helpdeskViewModel.loadUserReports()
                delay(800.milliseconds)
                isRefreshing = false
                Toast.makeText(context, "Helpdesk data refreshed", Toast.LENGTH_SHORT).show()
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
        // Top Header with Moderator Mode Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 26.dp, bottom = 8.dp),
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

            // Moderator Role Pill Toggle (Visible for whitelisted or Firestore-marked moderators)
            if (isDynamicModerator) {
                Surface(
                    color = if (isModeratorMode) Charcoal else CardWhite,
                    contentColor = if (isModeratorMode) CardWhite else Charcoal,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isModeratorMode) Charcoal else Hairline),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable { helpdeskViewModel.toggleModeratorMode(userProfile) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = "Moderator Mode",
                            tint = if (isModeratorMode) CardWhite else Charcoal,
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
        }

        HorizontalDivider(color = Hairline)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Moderator Portal Card (Visible for whitelisted or Firestore-marked moderators)
            // Moderator Portal Card (Redesigned CampusHive Theme)
            AnimatedVisibility(visible = isModeratorMode && isDynamicModerator) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick(scaleDown = 0.98f) { onNavigateToModeratorQueue() },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, Hairline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(HoneyTint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Security,
                                        contentDescription = null,
                                        tint = Charcoal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "MODERATOR PORTAL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            Surface(
                                color = if (openReportsCount > 0) Color(0xFFFFF3C4) else HoneyTint,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, if (openReportsCount > 0) Color(0xFFE5C158) else Color(0xFFFFE082))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (openReportsCount > 0) Color(0xFFD32F2F) else VerifiedGreen)
                                    )
                                    Text(
                                        text = "$openReportsCount PENDING",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Charcoal
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Campus Incident & Report Queue",
                            style = MaterialTheme.typography.titleMedium,
                            color = Charcoal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Review student-reported listings, safety concerns, and technical tickets across campus.",
                            fontSize = 12.sp,
                            color = MutedText,
                            lineHeight = 16.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        // Quick Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModeratorStatChip(
                                label = "Total",
                                count = allReports.size,
                                containerColor = CardWhite,
                                borderColor = Hairline,
                                countColor = Charcoal,
                                modifier = Modifier.weight(1f)
                            )
                            ModeratorStatChip(
                                label = "Listing Flags",
                                count = flaggedListingsCount,
                                containerColor = Color(0xFFFFF7DB),
                                borderColor = Color(0xFFFFE8A3),
                                countColor = Color(0xFFD97706),
                                modifier = Modifier.weight(1f)
                            )
                            ModeratorStatChip(
                                label = "User Safety",
                                count = safetyReportsCount,
                                containerColor = Color(0xFFFFF0F0),
                                borderColor = Color(0xFFFFCDD2),
                                countColor = Color(0xFFD32F2F),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = onNavigateToModeratorQueue,
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .bounceClick(scaleDown = 0.95f)
                        ) {
                            Text("Open Moderator Queue (${allReports.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CardWhite)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CardWhite, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // CAMPUSHIVE HELPDESK Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, Hairline)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        text = "CAMPUSHIVE HELPDESK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
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
                        colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Report a problem", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CardWhite)
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
}

@Composable
private fun ModeratorStatChip(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = CardWhite,
    borderColor: Color = Hairline,
    countColor: Color = Charcoal
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = countColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
