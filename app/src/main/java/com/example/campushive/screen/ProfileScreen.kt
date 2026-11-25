package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.campushive.ui.theme.bounceClick
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import com.example.campushive.components.CampusConfirmDialog
import com.example.campushive.components.CampusEmptyState
import com.example.campushive.components.DeleteListingConfirmDialog
import com.example.campushive.components.EditListingDialog
import com.example.campushive.components.EditProfileDialog
import com.example.campushive.components.ServiceCategoryIconBox
import com.example.campushive.model.Listing
import com.example.campushive.ui.theme.*
import com.example.campushive.util.FavoritesStore
import com.example.campushive.viewmodel.AuthUiState
import com.example.campushive.viewmodel.AuthViewModel
import com.example.campushive.viewmodel.ListingViewModel
import com.example.campushive.viewmodel.ReviewViewModel
import kotlin.time.Duration.Companion.milliseconds

private val emptyStateCategories = listOf("Tutoring", "Design", "Tech Help", "Errands")

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    listingViewModel: ListingViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel(),
    onHelpdeskClick: () -> Unit = {},
    onReplayGuideClick: () -> Unit = {},
    onCreateServiceClick: () -> Unit = {},
    onListingClick: (Listing) -> Unit = {},
    onSignOutClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("My listings") }
    val currentUser = authViewModel.currentUser
    val userProfile by authViewModel.userProfile.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()

    val displayName = userProfile?.displayName?.ifBlank { currentUser?.displayName ?: "ken" } ?: "ken"
    val email = userProfile?.email?.ifBlank { currentUser?.email ?: "yonggk07@gmail.com" } ?: "yonggk07@gmail.com"
    val campus = userProfile?.campusLocation ?: "HELP University S2"
    val major = userProfile?.facultyOrMajor ?: "General Student"
    val phone = userProfile?.phoneNumber ?: ""
    val bio = userProfile?.bio?.trim().orEmpty()

    val isGoogleUser = currentUser?.providerData?.any { it.providerId == "google.com" } == true
    val isVerified = isGoogleUser || (authViewModel.isEmailVerified && userProfile?.isVerifiedStudent == true)

    val providerReviews by reviewViewModel.providerReviews.collectAsState()
    val myListings by listingViewModel.myListings.collectAsState()
    val allLiveListings by listingViewModel.listings.collectAsState()
    val savedIds by FavoritesStore.savedIds.collectAsState()

    var selectedReviewType by remember { mutableStateOf("Received") }
    val currentUid = currentUser?.uid ?: ""

    LaunchedEffect(currentUid) {
        FavoritesStore.load(context, currentUid)
    }

    val savedListings = remember(allLiveListings, savedIds) {
        allLiveListings.filter { savedIds.contains(it.id) }
    }

    val reviewsReceived = remember(providerReviews, currentUid, displayName) {
        providerReviews.filter {
            (currentUid.isNotBlank() && it.providerUid == currentUid) ||
            (displayName.isNotBlank() && it.providerName.equals(displayName, ignoreCase = true)) ||
            it.providerUid.isBlank()
        }
    }

    val reviewsGiven = remember(providerReviews, currentUid, displayName) {
        providerReviews.filter {
            (currentUid.isNotBlank() && it.reviewerUid == currentUid) ||
            (displayName.isNotBlank() && it.reviewerName.equals(displayName, ignoreCase = true))
        }
    }

    var editingListing by remember { mutableStateOf<Listing?>(null) }
    var listingPendingDelete by remember { mutableStateOf<Listing?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }

    // Fetch user profile, listings, and reviews only if not yet populated (pull-to-refresh forces reload)
    LaunchedEffect(currentUser?.uid) {
        if (userProfile == null) {
            authViewModel.fetchUserProfile()
        }
        if (myListings.isEmpty()) {
            listingViewModel.refreshUserListings()
        }
        if (providerReviews.isEmpty()) {
            reviewViewModel.loadReviewsForProvider(currentUser?.uid.orEmpty())
        }
    }

    // Toast listener for AuthUiState
    LaunchedEffect(authUiState) {
        when (val state = authUiState) {
            is AuthUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.clearUiState()
            }
            is AuthUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                authViewModel.clearUiState()
            }
            else -> {}
        }
    }

    val averageRating = remember(reviewsReceived) {
        if (reviewsReceived.isNotEmpty()) {
            reviewsReceived.map { it.rating }.average()
        } else {
            0.0
        }
    }

    val jobsDoneCount = remember(myListings) {
        myListings.size
    }

    val totalReviewCount = remember(reviewsReceived) {
        reviewsReceived.size
    }

    val initials = remember(displayName) {
        displayName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "K" }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            userProfile = userProfile,
            currentDisplayName = displayName,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updatedName, updatedBio, updatedCampus, updatedMajor, updatedPhone ->
                authViewModel.updateUserProfile(
                    displayName = updatedName,
                    bio = updatedBio,
                    campusLocation = updatedCampus,
                    facultyOrMajor = updatedMajor,
                    phoneNumber = updatedPhone,
                    onComplete = { success ->
                        if (success) {
                            showEditProfileDialog = false
                        }
                    }
                )
            }
        )
    }

    if (editingListing != null) {
        EditListingDialog(
            listing = editingListing!!,
            onDismiss = { editingListing = null },
            onSave = { updated ->
                listingViewModel.updateListing(updated)
                editingListing = null
            },
            onDelete = { listingId ->
                listingViewModel.deleteListing(listingId)
                editingListing = null
            }
        )
    }

    listingPendingDelete?.let { pending ->
        DeleteListingConfirmDialog(
            listingTitle = pending.title,
            onDismiss = { listingPendingDelete = null },
            onConfirmDelete = {
                listingViewModel.deleteListing(pending.id)
                listingPendingDelete = null
                Toast.makeText(context, "Listing deleted", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showSignOutConfirmDialog) {
        CampusConfirmDialog(
            title = "Confirm Log Out",
            message = "Are you sure you want to log out of your CampusHive account?",
            confirmLabel = "Log Out",
            onConfirm = {
                showSignOutConfirmDialog = false
                authViewModel.signOut {
                    onSignOutClick()
                }
            },
            onDismiss = { showSignOutConfirmDialog = false }
        )
    }

    if (showAppearanceDialog) {
        AlertDialog(
            onDismissRequest = { showAppearanceDialog = false },
            title = {
                Text(
                    text = "Appearance",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose how CampusHive looks on your device.",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                    Spacer(Modifier.height(4.dp))
                    ThemePreference.entries.forEach { option ->
                        val selected = CampusThemeState.preference == option
                        Surface(
                            shape = Radius.Inner,
                            color = if (selected) HoneyTint else CreamTint,
                            border = BorderStroke(1.dp, if (selected) Charcoal else Hairline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick {
                                    CampusThemeState.setPreference(context, option)
                                    showAppearanceDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Charcoal
                                )
                                RadioButton(
                                    selected = selected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = Charcoal, unselectedColor = MutedText)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppearanceDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = Charcoal)
                }
            },
            containerColor = CardWhite,
            shape = Radius.Card
        )
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                authViewModel.reloadUserVerificationStatus { _, _ -> }
                authViewModel.fetchUserProfile()
                listingViewModel.refreshUserListings()
                reviewViewModel.loadReviewsForProvider()
                delay(800.milliseconds)
                isRefreshing = false
                Toast.makeText(context, "Profile data refreshed", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HoneyBottomGradient)
        ) {
            // Unified Curved White Top Header (26.sp Title + Settings)
            Surface(
                color = CardWhite,
                border = BorderStroke(1.dp, Hairline),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = Spacing.gutter, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile",
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        letterSpacing = (-0.5).sp
                    )

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CreamTint)
                            .bounceClick(scaleDown = 0.90f) { showAppearanceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Charcoal,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = Spacing.gutter,
                        end = Spacing.gutter,
                        top = 16.dp
                    )
            ) {
                // Polished Profile Identity Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, Hairline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Avatar + Name / Verified / Email
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(HoneyTint)
                                    .border(BorderStroke(1.5.dp, AvatarOutline), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = displayName,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Charcoal
                                    )
                                    if (isVerified) {
                                        Spacer(Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Campus Verified",
                                            tint = Charcoal,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = email,
                                    fontSize = 12.sp,
                                    color = MutedText,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        if (bio.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = bio,
                                fontSize = 13.sp,
                                color = Charcoal.copy(alpha = 0.85f),
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Location, Major & Phone Pills (wrapped cleanly side-by-side)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Cream,
                                border = BorderStroke(1.dp, Hairline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = MutedText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        campus,
                                        fontSize = 12.sp,
                                        color = Charcoal,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Cream,
                                border = BorderStroke(1.dp, Hairline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.School,
                                        contentDescription = null,
                                        tint = MutedText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        major,
                                        fontSize = 12.sp,
                                        color = Charcoal,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (phone.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = HoneyTint,
                                    border = BorderStroke(1.dp, Hairline)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Outlined.Phone,
                                            contentDescription = null,
                                            tint = Charcoal,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(5.dp))
                                        Text(
                                            phone,
                                            fontSize = 12.sp,
                                            color = Charcoal,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Cream-tinted Inner Stats Bar
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Cream,
                            border = BorderStroke(1.dp, Hairline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = if (reviewsReceived.isNotEmpty()) StarGold else MutedText,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            text = if (reviewsReceived.isNotEmpty()) "%.1f".format(averageRating) else "0.0",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = Charcoal
                                        )
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "RATING",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MutedText,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                VerticalDivider(
                                    modifier = Modifier.height(28.dp),
                                    thickness = 1.dp,
                                    color = Hairline
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        jobsDoneCount.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Charcoal
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "JOBS LISTED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MutedText,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                VerticalDivider(
                                    modifier = Modifier.height(28.dp),
                                    thickness = 1.dp,
                                    color = Hairline
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        totalReviewCount.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Charcoal
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "REVIEWS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MutedText,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Edit Profile Button
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CreamTint,
                            border = BorderStroke(1.dp, Hairline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .bounceClick(scaleDown = 0.97f) { showEditProfileDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = Charcoal,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Edit profile",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal
                                )
                            }
                        }
                    }
                }

        // Email Verification Alert Banner (shown when email is unverified)
        if (!isVerified) {
            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = Radius.Card,
                colors = CardDefaults.cardColors(containerColor = WarningBgSoft),
                border = BorderStroke(1.dp, WarningBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.MarkEmailUnread,
                            contentDescription = null,
                            tint = DangerFg,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Unverified Student Email",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DangerFg
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Verify your university email address to get the 'Campus Verified' badge and build trust with peers.",
                        fontSize = 12.sp,
                        color = Charcoal,
                        lineHeight = 17.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                authViewModel.sendEmailVerification { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerFg, contentColor = CardWhite),
                            shape = Radius.Tile,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Send Link", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                authViewModel.reloadUserVerificationStatus { _, isNowVerified ->
                                    if (isNowVerified) {
                                        Toast.makeText(context, "Email verified! You are now Campus Verified.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Email is still unverified. Please check your inbox link.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            shape = Radius.Tile,
                            border = BorderStroke(1.dp, DangerFg),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Check Status",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DangerFg
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Segmented Tabs: My listings | Saved | Reviews
        Surface(
            shape = Radius.Card,
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                listOf("My listings", "Saved", "Reviews").forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabLabel = if (tab == "Saved" && savedListings.isNotEmpty()) "Saved (${savedListings.size})" else tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(Radius.Inner)
                            .background(if (isSelected) Honey else Color.Transparent)
                            .bounceClick { selectedTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabLabel,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnHoneyAccent else Charcoal
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tab Content
        if (selectedTab == "My listings") {
            if (myListings.isEmpty()) {
                CampusEmptyState(
                    icon = Icons.Outlined.WorkOutline,
                    title = "No active service listings",
                    subtitle = "Offer your skills to earn income on campus.\nPick a category to get started.",
                    actionLabel = "+ Add a service",
                    onAction = onCreateServiceClick,
                    extraContent = {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(emptyStateCategories) { cat ->
                                Surface(
                                    shape = Radius.Card,
                                    color = CardWhite,
                                    border = BorderStroke(1.dp, Hairline),
                                    modifier = Modifier.bounceClick(scaleDown = 0.93f) { onCreateServiceClick() }
                                ) {
                                    Text(
                                        text = cat,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Charcoal
                                    )
                                }
                            }
                        }
                    }
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    myListings.forEach { listing ->
                        key(listing.id) {
                            val isActive = listing.status == "ACTIVE"
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        listingPendingDelete = listing
                                    }
                                    false
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    val isSwiping = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart ||
                                        dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(if (isSwiping) DangerFgStrong else DangerBg)
                                            .border(BorderStroke(1.dp, DangerBorder), RoundedCornerShape(18.dp))
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Delete listing",
                                                tint = if (isSwiping) CardWhite else DangerFgStrong,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Delete",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSwiping) CardWhite else DangerFgStrong
                                            )
                                        }
                                    }
                                }
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                                    border = BorderStroke(1.dp, Hairline),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ServiceCategoryIconBox(
                                        category = listing.category,
                                        size = 44.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        symbolFontSize = 16.sp,
                                        iconSize = 20.dp
                                    )

                                    Spacer(Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = HoneyTint,
                                                shape = Radius.Tag
                                            ) {
                                                Text(
                                                    text = listing.category.uppercase(),
                                                    modifier = Modifier.padding(
                                                        horizontal = 7.dp,
                                                        vertical = 2.dp
                                                    ),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Charcoal,
                                                    letterSpacing = 0.4.sp
                                                )
                                            }

                                            // Semantic Status Pill (Active / Paused)
                                            Surface(
                                                color = if (isActive) SuccessBg else NeutralBg,
                                                shape = RoundedCornerShape(50),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isActive) SuccessBorder else Hairline
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(
                                                        horizontal = 8.dp,
                                                        vertical = 3.dp
                                                    ),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (isActive) OnlineGreen else MutedText
                                                            )
                                                    )
                                                    Text(
                                                        text = if (isActive) "Active" else "Paused",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isActive) SuccessFg else MutedText
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(5.dp))

                                        Text(
                                            text = listing.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isActive) Charcoal else MutedText
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(thickness = 0.8.dp, color = Hairline)
                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "RM ${"%.0f".format(listing.price)} ${listing.pricingType.shortLabel}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isActive) Charcoal else MutedText
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Clean Pause / Resume Action Button
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isActive) Cream else SuccessBg,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isActive) Hairline else SuccessBorder
                                            ),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .bounceClick(scaleDown = 0.94f) {
                                                    listingViewModel.toggleListingStatus(listing.id)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 11.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isActive) Icons.Outlined.PauseCircle else Icons.Outlined.PlayCircle,
                                                    contentDescription = if (isActive) "Pause listing" else "Resume listing",
                                                    tint = if (isActive) Charcoal else SuccessFg,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = if (isActive) "Pause" else "Resume",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = if (isActive) Charcoal else SuccessFg
                                                )
                                            }
                                        }

                                        // Matching Primary Edit Action Button
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Honey,
                                            modifier = Modifier
                                                .height(34.dp)
                                                .bounceClick(scaleDown = 0.94f) {
                                                    editingListing = listing
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Edit,
                                                    contentDescription = "Edit listing",
                                                    tint = OnHoneyAccent,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Edit",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = OnHoneyAccent
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == "Saved") {
            if (savedListings.isEmpty()) {
                CampusEmptyState(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = "No saved services yet",
                    subtitle = "Tap the heart icon on any service listing to bookmark it for later.",
                    compact = true
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    savedListings.forEach { listing ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(Radius.Inner)
                                .bounceClick(scaleDown = 0.97f) { onListingClick(listing) },
                            shape = Radius.Inner,
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, Hairline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ServiceCategoryIconBox(
                                    category = listing.category,
                                    size = 44.dp,
                                    shape = Radius.Tile,
                                    symbolFontSize = 16.sp,
                                    iconSize = 20.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = listing.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Charcoal,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "${listing.providerName} • RM ${"%.0f".format(listing.price)} ${listing.pricingType.shortLabel}",
                                        fontSize = 12.sp,
                                        color = MutedText,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = if (listing.reviewCount > 0) StarGold else MutedText,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            text = if (listing.reviewCount > 0) "%.1f (${listing.reviewCount})".format(listing.rating) else "New",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Charcoal
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        FavoritesStore.toggle(context, currentUid, listing.id)
                                        Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(DangerBg)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Favorite,
                                        contentDescription = "Remove from saved",
                                        tint = DangerFgStrong,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Reviews Tab: Sub-pills (Received vs Given)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = Radius.Inner,
                        color = if (selectedReviewType == "Received") Charcoal else CreamTint,
                        modifier = Modifier.bounceClick { selectedReviewType = "Received" }
                    ) {
                        Text(
                            text = "Received (${reviewsReceived.size})",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedReviewType == "Received") CardWhite else Charcoal
                        )
                    }

                    Surface(
                        shape = Radius.Inner,
                        color = if (selectedReviewType == "Given") Charcoal else CreamTint,
                        modifier = Modifier.bounceClick { selectedReviewType = "Given" }
                    ) {
                        Text(
                            text = "Given (${reviewsGiven.size})",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedReviewType == "Given") CardWhite else Charcoal
                        )
                    }
                }

                val activeReviews = if (selectedReviewType == "Received") reviewsReceived else reviewsGiven

                if (activeReviews.isEmpty()) {
                    CampusEmptyState(
                        icon = Icons.Filled.Star,
                        title = if (selectedReviewType == "Received") "No reviews received yet" else "No reviews submitted yet",
                        subtitle = if (selectedReviewType == "Received")
                            "Reviews left by students for your services will appear here."
                        else
                            "Rate a service provider after completing a session to see your review history here.",
                        compact = true
                    )
                } else {
                    activeReviews.forEach { review ->
                        val isReceived = selectedReviewType == "Received"
                        val displayNameInReview = if (isReceived) review.reviewerName else review.providerName

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = Radius.Inner,
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, Hairline)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(28.dp).clip(CircleShape).background(HoneyTint).border(BorderStroke(1.5.dp, AvatarOutline), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = displayNameInReview.take(2).uppercase().ifBlank { "ST" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Charcoal
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = displayNameInReview.ifBlank { "Student Peer" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Charcoal
                                            )
                                            if (review.serviceTitle.isNotBlank()) {
                                                Text(
                                                    text = review.serviceTitle,
                                                    fontSize = 10.sp,
                                                    color = MutedText
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = Charcoal,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            "%.1f".format(review.rating),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Charcoal
                                        )
                                    }
                                }

                                if (review.tags.isNotEmpty()) {
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        review.tags.forEach { tag ->
                                            Surface(
                                                color = HoneyTint,
                                                shape = Radius.Tile
                                            ) {
                                                Text(
                                                    text = tag,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Charcoal
                                                )
                                            }
                                        }
                                    }
                                }

                                if (review.comment.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "\"${review.comment}\"",
                                        fontSize = 12.sp,
                                        color = Charcoal,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Unified Bottom Menu Card (Dark Mode + Helpdesk & FAQ + Log out)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = Radius.Inner,
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, Hairline)
        ) {
            Column {
                // Dark Mode Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick { showAppearanceDialog = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (CampusThemeState.isDark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                            contentDescription = null,
                            tint = Charcoal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Dark Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Charcoal
                            )
                            Text(
                                "Mode: ${CampusThemeState.preference.label} • Tap for options",
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }
                    Switch(
                        checked = CampusThemeState.isDark,
                        onCheckedChange = { enabled ->
                            CampusThemeState.setPreference(
                                context,
                                if (enabled) ThemePreference.DARK else ThemePreference.LIGHT
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CardWhite,
                            checkedTrackColor = Charcoal,
                            uncheckedThumbColor = CardWhite,
                            uncheckedTrackColor = Hairline,
                            uncheckedBorderColor = MutedText
                        )
                    )
                }

                HorizontalDivider(color = Hairline)

                // In-App New User Guide Replay Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick(onClick = onReplayGuideClick)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Explore,
                            contentDescription = null,
                            tint = Charcoal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "CampusHive Guide",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Charcoal
                            )
                            Text(
                                "Replay the interactive app walkthrough",
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MutedText)
                }

                HorizontalDivider(color = Hairline)

                // Helpdesk Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick(onClick = onHelpdeskClick)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Security,
                            contentDescription = null,
                            tint = Charcoal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Helpdesk & FAQ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Charcoal
                            )
                            Text(
                                "Campus safety, reports & guidelines",
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MutedText)
                }

                HorizontalDivider(color = Hairline)

                // Log out Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick { showSignOutConfirmDialog = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = null,
                            tint = DangerFgStrong,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Log out",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DangerFgStrong
                        )
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MutedText)
                }
            }
        }

        Spacer(Modifier.height(rememberBottomBarClearance()))
            }
        }
    }
}
