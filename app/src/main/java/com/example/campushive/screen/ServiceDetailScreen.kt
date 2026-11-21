package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.campushive.ui.theme.bounceClick
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.components.ReportDialog
import com.example.campushive.components.ReviewDialog
import com.example.campushive.components.ServiceCategoryIconBox
import com.example.campushive.model.LISTING_TIME_SLOTS
import com.example.campushive.model.Listing
import com.example.campushive.model.PricingType
import com.example.campushive.components.ProviderProfileSheet
import com.example.campushive.model.ReportStatus
import com.example.campushive.model.ReportType
import com.example.campushive.ui.theme.*
import com.example.campushive.util.FavoritesStore
import com.example.campushive.viewmodel.ChatViewModel
import com.example.campushive.viewmodel.HelpdeskViewModel
import com.example.campushive.viewmodel.ListingViewModel
import com.example.campushive.viewmodel.ReviewViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    listingId: String,
    listingViewModel: ListingViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel(),
    helpdeskViewModel: HelpdeskViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    onBackClick: () -> Unit,
    onChatClick: (providerUid: String, providerName: String, serviceTitle: String) -> Unit
) {
    val context = LocalContext.current
    val liveListings by listingViewModel.listings.collectAsState()
    val listingReviews by reviewViewModel.listingReviews.collectAsState()
    var showReviewDialog by remember { mutableStateOf(false) }
    var showProviderProfile by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportSubmittedLocally by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedBookingDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedBookingSlot by remember { mutableStateOf("") }

    LaunchedEffect(listingId) {
        reviewViewModel.loadReviewsForListing(listingId)
    }

    val listing = liveListings.find { it.id == listingId }
        ?: Listing(
            id = listingId,
            title = "Campus Service Listing",
            category = "Tutoring",
            price = 20.0,
            pricingType = PricingType.PER_HOUR,
            providerName = "Daniel Wong",
            rating = 5.0,
            reviewCount = 1,
            campusLocation = "HELP University S2",
            isVerifiedStudent = true,
            description = "Campus service listing details."
        )
    val currentUser = Firebase.auth.currentUser
    val currentUid = currentUser?.uid ?: ""
    val savedIds by FavoritesStore.savedIds.collectAsState()
    LaunchedEffect(currentUid) {
        FavoritesStore.load(context, currentUid)
    }
    val isFavorite = savedIds.contains(listing.id)
    val currentUserName = currentUser?.displayName?.trim()?.takeIf { it.isNotBlank() }
        ?: currentUser?.email?.substringBefore("@")?.trim()
        ?: ""

    val isOwnListing = (currentUid.isNotBlank() && listing.providerUid.isNotBlank() && currentUid == listing.providerUid) ||
        (currentUserName.isNotBlank() && listing.providerName.isNotBlank() && currentUserName.equals(listing.providerName, ignoreCase = true))

    val initials = listing.providerName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("").ifBlank { "ST" }

    if (showReviewDialog && !isOwnListing) {
        ReviewDialog(
            listingId = listing.id,
            providerName = listing.providerName,
            serviceTitle = listing.title,
            reviewViewModel = reviewViewModel,
            onDismiss = { showReviewDialog = false },
            onSubmit = { showReviewDialog = false }
        )
    }

    // Provider profile preview — opened from the provider card
    if (showProviderProfile) {
        ProviderProfileSheet(
            uid = listing.providerUid,
            displayName = listing.providerName,
            serviceTitle = listing.title,
            onDismiss = { showProviderProfile = false },
            onMessageClick = {
                showProviderProfile = false
                onChatClick(listing.providerUid, listing.providerName, listing.title)
            }
        )
    }

    if (showReportDialog) {
        ReportDialog(
            initialType = ReportType.LISTING_FLAG,
            targetId = listing.id,
            targetName = listing.title,
            helpdeskViewModel = helpdeskViewModel,
            onDismiss = { showReportDialog = false },
            onSubmitSuccess = {
                showReportDialog = false
                reportSubmittedLocally = true
            }
        )
    }

    val userReports by helpdeskViewModel.userReports.collectAsState()
    val allReports by helpdeskViewModel.allReports.collectAsState()

    val listingReport = remember(userReports, allReports, listing.id, listing.title) {
        (allReports + userReports).find { report ->
            report.status != ReportStatus.RESOLVED && report.status != ReportStatus.CLOSED && (
                report.targetId == listing.id ||
                (listing.id.isNotBlank() && report.targetId.contains(listing.id)) ||
                (report.targetName.isNotBlank() && report.targetName.equals(listing.title, ignoreCase = true))
            )
        }
    }

    var showReportNoticeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(reportSubmittedLocally) {
        if (reportSubmittedLocally) {
            showReportNoticeDialog = true
        }
    }

    // Pop-up Notice Dialog when a report is submitted or tapped
    if (showReportNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showReportNoticeDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.WarningAmber,
                        contentDescription = null,
                        tint = DangerFg,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Report Submitted",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Charcoal
                    )
                }
            },
            text = {
                Text(
                    text = "Category: ${listingReport?.category ?: "Flag"}\n\nOur campus moderation team is investigating this listing. Thank you for keeping CampusHive safe!",
                    fontSize = 13.sp,
                    color = Charcoal,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showReportNoticeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite),
                    shape = Radius.Tile
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            containerColor = CardWhite,
            shape = Radius.Card
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedBookingDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedBookingDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold, color = Charcoal)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MutedText)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = CardWhite)
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HoneyBottomGradient)
    ) {
        // Sticky Top Hero Banner (Back, Category Icon Showcase, Favorite)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(HoneyTint, Cream)
                    )
                )
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 14.dp)
        ) {
            // Center Category Vector Icon Showcase matching Home Page Style
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ServiceCategoryIconBox(
                    category = listing.category,
                    size = 64.dp,
                    shape = Radius.Card,
                    symbolFontSize = 26.sp,
                    iconSize = 30.dp
                )

                Spacer(Modifier.height(8.dp))

                Surface(
                    color = CardWhite,
                    shape = Radius.Card,
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Text(
                        text = listing.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                }
            }

            // Floating Top Navigation Row (Back & Heart Buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CardWhite)
                        .bounceClick(scaleDown = 0.90f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Charcoal,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val nowSaved = FavoritesStore.toggle(context, currentUid, listing.id)
                        Toast.makeText(context, if (nowSaved) "Saved to favorites!" else "Removed from favorites", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CardWhite)
                        .bounceClick(scaleDown = 0.88f)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) DangerFgStrong else Charcoal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Detail Body Content (Clean Editorial Layout)
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Report Pending Banner (only if flagged)
                if (listingReport != null || reportSubmittedLocally) {
                    Surface(
                        color = WarningBg,
                        shape = Radius.Inner,
                        border = BorderStroke(1.dp, WarningBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick { showReportNoticeDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = WarningFg,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Report pending (${listingReport?.status?.name ?: "OPEN"})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WarningFg
                            )
                        }
                    }
                }

                // 1. Title + Unified Metadata Line (no duplicate category pill)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = listing.title,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        lineHeight = 29.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = StarGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = if (listing.reviewCount > 0) "%.1f".format(listing.rating) else "New",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Charcoal
                            )
                            if (listing.reviewCount > 0) {
                                Text(
                                    text = " (${listing.reviewCount})",
                                    fontSize = 12.sp,
                                    color = MutedText
                                )
                            }
                        }

                        Text("·", color = MutedText, fontSize = 13.sp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MutedText,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = listing.campusLocation,
                                fontSize = 12.sp,
                                color = MutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (listing.isVerifiedStudent) {
                            Surface(
                                color = SuccessBg,
                                shape = Radius.Pill
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Security,
                                        contentDescription = null,
                                        tint = SuccessFg,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        text = "Verified",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SuccessFg
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Clean Provider Row (soft avatar, minimal border)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick(scaleDown = 0.98f) {
                            if (!isOwnListing) showProviderProfile = true
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = CardWhite,
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(HoneyTint)
                                    .border(BorderStroke(1.dp, Hairline), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Charcoal
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = listing.providerName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Charcoal
                                )
                                Text(
                                    text = "Usually responds in ~10 min",
                                    fontSize = 12.sp,
                                    color = MutedText
                                )
                            }
                        }

                        if (!isOwnListing) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = "View provider profile",
                                tint = MutedText,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Surface(
                                color = CreamTint,
                                shape = Radius.Pill
                            ) {
                                Text(
                                    text = "You",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MutedText
                                )
                            }
                        }
                    }
                }

                // 3. About This Service
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "About this service",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                    Text(
                        text = listing.description.ifBlank {
                            "1-on-1 tutoring and student service. High quality assistance and past year study notes included."
                        },
                        fontSize = 14.sp,
                        color = Charcoal.copy(alpha = 0.88f),
                        lineHeight = 21.sp
                    )
                }

                HorizontalDivider(color = Hairline, thickness = 0.8.dp)

                // 4. Clean Availability & Timing Section (no nested heavy box)
                val slotOptions = listing.availableTimeSlots.ifEmpty { LISTING_TIME_SLOTS }
                val formattedSelectedDate = remember(selectedBookingDateMillis) {
                    selectedBookingDateMillis?.let {
                        SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date(it))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Availability",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                            Text(
                                text = listing.availabilitySummary(),
                                fontSize = 12.sp,
                                color = MutedText
                            )
                        }

                        if (!isOwnListing) {
                            Surface(
                                color = CardWhite,
                                shape = Radius.Pill,
                                border = BorderStroke(1.dp, Hairline),
                                modifier = Modifier.bounceClick(scaleDown = 0.94f) { showDatePicker = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = Charcoal,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = if (formattedSelectedDate != null) "Change date" else "Pick date",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Charcoal
                                    )
                                }
                            }
                        }
                    }

                    if (!isOwnListing) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(slotOptions) { slot ->
                                val isSelected = selectedBookingSlot == slot
                                Surface(
                                    shape = Radius.Pill,
                                    color = if (isSelected) Honey else CardWhite,
                                    border = BorderStroke(1.dp, if (isSelected) Honey else Hairline),
                                    modifier = Modifier.bounceClick(scaleDown = 0.94f) {
                                        selectedBookingSlot = if (isSelected) "" else slot
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Schedule,
                                            contentDescription = null,
                                            tint = if (isSelected) OnHoneyAccent else MutedText,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        Text(
                                            text = slot,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                            color = if (isSelected) OnHoneyAccent else Charcoal
                                        )
                                    }
                                }
                            }
                        }

                        if (formattedSelectedDate != null || selectedBookingSlot.isNotBlank()) {
                            val summaryTiming = listOfNotNull(
                                formattedSelectedDate,
                                selectedBookingSlot.takeIf { it.isNotBlank() }
                            ).joinToString(" · ")
                            Button(
                                onClick = {
                                    val uniqueChatId = ChatViewModel.buildConversationId(listing.id, currentUid)
                                    chatViewModel.sendMessage(
                                        conversationId = uniqueChatId,
                                        text = "Hi ${listing.providerName}, I'd like to request a session for \"${listing.title}\" on $summaryTiming. Does this timing work for you?",
                                        studentName = listing.providerName,
                                        serviceTitle = listing.title,
                                        listingIdHint = listing.id,
                                        providerUidHint = listing.providerUid
                                    )
                                    onChatClick(listing.providerUid, listing.providerName, listing.title)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = OnHoneyAccent),
                                shape = Radius.Pill,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Request $summaryTiming",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Hairline, thickness = 0.8.dp)

                // 5. Unified Reviews Section (grouped card + calm neutral tags)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (listingReviews.isNotEmpty()) "Reviews (${listingReviews.size})" else "Reviews",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )

                        if (!isOwnListing) {
                            Surface(
                                color = CardWhite,
                                shape = Radius.Pill,
                                border = BorderStroke(1.dp, Hairline),
                                modifier = Modifier.bounceClick(scaleDown = 0.95f) { showReviewDialog = true }
                            ) {
                                Text(
                                    text = "Write review",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Charcoal
                                )
                            }
                        }
                    }

                    if (listingReviews.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, Hairline)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No reviews yet",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Charcoal
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (isOwnListing) "Reviews from students will appear here."
                                    else "Be the first student to share feedback after your session.",
                                    fontSize = 12.sp,
                                    color = MutedText
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = CardWhite,
                            border = BorderStroke(1.dp, Hairline)
                        ) {
                            Column {
                                listingReviews.forEachIndexed { index, review ->
                                    Column(Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .clip(CircleShape)
                                                        .background(CreamTint),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = review.reviewerName.take(2).uppercase(),
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 11.sp,
                                                        color = Charcoal
                                                    )
                                                }
                                                Spacer(Modifier.width(10.dp))
                                                Text(
                                                    text = review.reviewerName,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = Charcoal
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = StarGold,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(Modifier.width(3.dp))
                                                Text(
                                                    text = "%.1f".format(review.rating),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = Charcoal
                                                )
                                            }
                                        }

                                        if (review.comment.isNotBlank()) {
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = review.comment,
                                                fontSize = 13.sp,
                                                color = Charcoal.copy(alpha = 0.88f),
                                                lineHeight = 19.sp
                                            )
                                        }

                                        if (review.tags.isNotEmpty()) {
                                            Spacer(Modifier.height(8.dp))
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                items(review.tags) { tag ->
                                                    Surface(
                                                        color = CreamTint,
                                                        shape = Radius.Pill
                                                    ) {
                                                        Text(
                                                            text = tag,
                                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MutedText
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (index < listingReviews.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 14.dp),
                                            color = Hairline,
                                            thickness = 0.8.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Subtle Report Action (Seekers only)
                if (!isOwnListing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick { showReportDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WarningAmber,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Report this listing",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedText
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        // Sticky Bottom Booking Bar
        Surface(
            color = CardWhite,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "RM ${"%.0f".format(listing.price)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                    Text(
                        text = listing.pricingType.shortLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedText,
                        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                    )
                }

                if (isOwnListing) {
                    Button(
                        onClick = onBackClick,
                        shape = Radius.Pill,
                        colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = OnHoneyAccent),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                        modifier = Modifier.bounceClick(scaleDown = 0.95f)
                    ) {
                        Text(
                            text = "Back to Listings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = Honey,
                        modifier = Modifier
                            .size(50.dp)
                            .bounceClick(scaleDown = 0.92f) {
                                onChatClick(listing.providerUid, listing.providerName, listing.title)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Chat Provider",
                                tint = OnHoneyAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
