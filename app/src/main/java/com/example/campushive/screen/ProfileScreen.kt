package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.components.EditListingDialog
import com.example.campushive.components.EditProfileDialog
import com.example.campushive.components.ServiceCategoryIconBox
import com.example.campushive.model.Listing
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.AuthUiState
import com.example.campushive.viewmodel.AuthViewModel
import com.example.campushive.viewmodel.ListingViewModel
import com.example.campushive.viewmodel.ReviewViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    listingViewModel: ListingViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel(),
    onHelpdeskClick: () -> Unit = {},
    onCreateServiceClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("My listings") }
    val currentUser = authViewModel.currentUser
    val userProfile by authViewModel.userProfile.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()

    val displayName = userProfile?.displayName?.ifBlank { currentUser?.displayName ?: "Student User" } ?: "Student User"
    val email = userProfile?.email?.ifBlank { currentUser?.email ?: "you@student.uni.edu.my" } ?: "you@student.uni.edu.my"
    val bio = userProfile?.bio ?: "CampusHive Member"
    val campus = userProfile?.campusLocation ?: "Subang 2 Campus"
    val major = userProfile?.facultyOrMajor ?: "School of Computing & IT"
    val phone = userProfile?.phoneNumber ?: ""

    val isVerified = userProfile?.isVerifiedStudent == true

    val providerReviews by reviewViewModel.providerReviews.collectAsState()
    val myListings by listingViewModel.myListings.collectAsState()

    var selectedReviewType by remember { mutableStateOf("Received") }
    val currentUid = currentUser?.uid ?: ""

    val reviewsReceived = remember(providerReviews, currentUid, displayName) {
        providerReviews.filter {
            (currentUid.isNotBlank() && it.providerUID == currentUid) ||
            (displayName.isNotBlank() && it.providerName.equals(displayName, ignoreCase = true)) ||
            it.providerUID.isBlank()
        }
    }

    val reviewsGiven = remember(providerReviews, currentUid, displayName) {
        providerReviews.filter {
            (currentUid.isNotBlank() && it.reviewerUID == currentUid) ||
            (displayName.isNotBlank() && it.reviewerName.equals(displayName, ignoreCase = true))
        }
    }

    var editingListing by remember { mutableStateOf<Listing?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Fetch latest user profile, user's own listings, and user's own reviews on load/login
    LaunchedEffect(currentUser?.uid) {
        authViewModel.fetchUserProfile()
        listingViewModel.refreshUserListings()
        reviewViewModel.loadReviewsForProvider(currentUser?.uid.orEmpty())
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

    val jobsDoneCount = remember(myListings, reviewsReceived) {
        myListings.size + reviewsReceived.size
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
            .ifEmpty { "CH" }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )

            // Edit Profile Quick Button
            IconButton(
                onClick = { showEditProfileDialog = true },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CardWhite)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit Profile",
                    tint = Charcoal,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Profile Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            border = BorderStroke(1.dp, Hairline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Honey),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        fontSize = 24.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Name & Verified Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    if (isVerified) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Campus Verified",
                            tint = VerifiedGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = "Unverified Email",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = email,
                    fontSize = 12.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Bio / Tagline
                if (bio.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = HoneyTint,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = bio,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Charcoal
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Detailed Info Chips: Campus, Major, Phone
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(campus, fontSize = 11.sp, color = Charcoal, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(12.dp))
                        Icon(Icons.Outlined.School, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(major, fontSize = 11.sp, color = Charcoal, fontWeight = FontWeight.SemiBold)
                    }

                    if (phone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Phone, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(phone, fontSize = 11.sp, color = Charcoal, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Stats Row (Rating, Jobs Done, Reviews)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Honey, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = if (reviewsReceived.isNotEmpty()) "%.1f".format(averageRating) else "0.0",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Charcoal
                            )
                        }
                        Text("RATING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
                    }

                    HorizontalDivider(modifier = Modifier.height(28.dp).width(1.dp), color = Hairline)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(jobsDoneCount.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Charcoal)
                        Text("JOBS DONE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
                    }

                    HorizontalDivider(modifier = Modifier.height(28.dp).width(1.dp), color = Hairline)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(totalReviewCount.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Charcoal)
                        Text("REVIEWS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedText)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Edit Profile Button inside Header Card
                OutlinedButton(
                    onClick = { showEditProfileDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = Charcoal, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Edit Profile Details", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                }
            }
        }

        // Email Verification Alert Banner (shown when email is unverified)
        if (!isVerified) {
            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                border = BorderStroke(1.dp, Color(0xFFFFB74D))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.MarkEmailUnread,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Unverified Student Email",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFE65100)
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100), contentColor = CardWhite),
                            shape = RoundedCornerShape(10.dp),
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
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE65100)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Check Status", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE65100))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Segmented Tabs: My listings | Reviews
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                listOf("My listings", "Reviews").forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Charcoal else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CardWhite else Charcoal
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tab Content
        if (selectedTab == "My listings") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Service Listings (${myListings.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )

                TextButton(
                    onClick = onCreateServiceClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Charcoal, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add New", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                }
            }

            if (myListings.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, Hairline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No active service listings", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Charcoal)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Offer your skills (Tutoring, Design, Tech Help, Errands) to make income on campus!",
                            fontSize = 12.sp,
                            color = MutedText,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onCreateServiceClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = Charcoal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ Create a Service", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    myListings.forEach { listing ->
                        val isActive = listing.status == "ACTIVE"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            border = BorderStroke(1.dp, Hairline)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ServiceCategoryIconBox(
                                        category = listing.category,
                                        size = 42.dp,
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
                                            Surface(color = HoneyTint, shape = RoundedCornerShape(6.dp)) {
                                                Text(
                                                    text = listing.category.uppercase(),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Charcoal
                                                )
                                            }

                                            Text(
                                                text = if (isActive) "• Active" else "• Paused",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isActive) VerifiedGreen else Color(0xFFE65100)
                                            )
                                        }

                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            text = listing.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Charcoal
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "RM ${"%.0f".format(listing.price)} ${listing.pricingType.shortLabel}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Charcoal
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Pause / Resume Toggle
                                        IconButton(
                                            onClick = { listingViewModel.toggleListingStatus(listing.id) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Cream)
                                        ) {
                                            Icon(
                                                imageVector = if (isActive) Icons.Outlined.PauseCircle else Icons.Outlined.PlayCircle,
                                                contentDescription = if (isActive) "Pause" else "Activate",
                                                tint = Charcoal,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Edit Button
                                        Button(
                                            onClick = { editingListing = listing },
                                            colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = Charcoal),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Outlined.Edit, contentDescription = "Edit", modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("EDIT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
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
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedReviewType == "Received") Charcoal else CreamTint,
                        modifier = Modifier.clickable { selectedReviewType = "Received" }
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
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedReviewType == "Given") Charcoal else CreamTint,
                        modifier = Modifier.clickable { selectedReviewType = "Given" }
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, Hairline)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = if (selectedReviewType == "Received")
                                    "No reviews received yet. Reviews left by students for your services will appear here!"
                                else
                                    "No reviews submitted yet. Rate a service provider after completing a session to see your review history here!",
                                fontSize = 12.sp,
                                color = MutedText,
                                lineHeight = 17.sp
                            )
                        }
                    }
                } else {
                    activeReviews.forEach { review ->
                        val isReceived = selectedReviewType == "Received"
                        val displayNameInReview = if (isReceived) review.reviewerName else review.providerName

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
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
                                            modifier = Modifier.size(28.dp).clip(CircleShape).background(HoneyTint),
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
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = Honey, modifier = Modifier.size(13.dp))
                                        Spacer(Modifier.width(2.dp))
                                        Text("%.1f".format(review.rating), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Charcoal)
                                    }
                                }

                                if (review.tags.isNotEmpty()) {
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        review.tags.forEach { tag ->
                                            Surface(
                                                color = HoneyTint,
                                                shape = RoundedCornerShape(10.dp)
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

        // Helpdesk & Support Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onHelpdeskClick),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Security, contentDescription = null, tint = Charcoal, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Helpdesk & FAQ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Charcoal)
                        Text("Campus safety, reports & guidelines", fontSize = 11.sp, color = MutedText)
                    }
                }
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MutedText)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Log Out Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    authViewModel.signOut {
                        onSignOutClick()
                    }
                },
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFD32F2F))
                }
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MutedText)
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}
