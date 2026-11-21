package com.example.campushive.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.components.NotificationCenterDialog
import com.example.campushive.components.ServiceCategoryIconBox
import com.example.campushive.model.ChatConversation
import com.example.campushive.model.Listing
import com.example.campushive.model.getCategoryVisual
import com.example.campushive.ui.theme.*
import com.example.campushive.util.FavoritesStore
import com.example.campushive.util.ListingRecommender
import com.example.campushive.viewmodel.AuthViewModel
import com.example.campushive.viewmodel.ChatViewModel
import com.example.campushive.viewmodel.ListingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private data class ServiceCategory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val symbolText: String? = null
)

private val homeCategories = listOf(
    ServiceCategory("tutoring", "Tutoring", Icons.Outlined.School, "Σ"),
    ServiceCategory("design", "Design", Icons.Outlined.Palette, "✎"),
    ServiceCategory("tech", "Tech Help", Icons.Outlined.Code, "</>"),
    ServiceCategory("errands", "Errands", Icons.Filled.Check, "✓"),
    ServiceCategory("others", "Others", Icons.Outlined.Category, "✦")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    listingViewModel: ListingViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    onListingClick: (Listing) -> Unit = {},
    onNavigateToSearch: (String) -> Unit = {},
    onSearchBarClick: () -> Unit = {},
    onNavigateToCreateService: () -> Unit = {},
    onOpenChat: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val liveListings by listingViewModel.listings.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val conversations by chatViewModel.conversations.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val currentUid = authViewModel.currentUser?.uid.orEmpty()
    val savedIds by FavoritesStore.savedIds.collectAsState()
    LaunchedEffect(currentUid) {
        FavoritesStore.load(context, currentUid)
    }

    val totalUnreadCount = remember(conversations) {
        conversations.sumOf { it.unreadCount }
    }

    val notificationConversations = remember(conversations) {
        conversations
            .filter { it.lastMessage.isNotBlank() && !it.lastMessage.equals("Tap to start chatting", ignoreCase = true) }
            .sortedWith(compareByDescending<ChatConversation> { it.unreadCount > 0 }.thenByDescending { it.lastMessageMillis })
    }

    val upNextChat = remember(conversations) {
        conversations.firstOrNull { it.unreadCount > 0 }
            ?: conversations.firstOrNull { it.lastMessage.isNotBlank() }
    }

    val currentUserName = userProfile?.displayName.orEmpty().ifBlank {
        authViewModel.currentUser?.displayName.orEmpty().ifBlank {
            authViewModel.currentUser?.email?.substringBefore("@").orEmpty().ifBlank { "Student" }
        }
    }
    val firstName = currentUserName.split(" ").firstOrNull() ?: "Student"
    val initials = remember(currentUserName) {
        currentUserName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { "S" }
    }
    val campusName = userProfile?.campusLocation?.takeIf { it.isNotBlank() } ?: "HELP University S2"

    // Scenario-based greeting: Welcome (first visit) / Welcome back (returning) / Hello (recent session)
    val heroGreeting = remember(currentUid) {
        val prefs = context.getSharedPreferences("campushive_prefs", Context.MODE_PRIVATE)
        val visitKey = "last_home_visit_${currentUid.ifBlank { "guest" }}"
        val lastVisit = prefs.getLong(visitKey, 0L)
        val now = System.currentTimeMillis()
        val fourHoursMs = 4L * 60L * 60L * 1000L
        when {
            lastVisit == 0L -> "Welcome"
            now - lastVisit >= fourHoursMs -> "Welcome back"
            else -> "Hello"
        }
    }
    val heroSubtitle = remember(heroGreeting) {
        when (heroGreeting) {
            "Welcome" -> "Find or offer services across campus"
            "Welcome back" -> "Ready to explore campus today?"
            else -> "What are you looking for today?"
        }
    }
    LaunchedEffect(currentUid) {
        val prefs = context.getSharedPreferences("campushive_prefs", Context.MODE_PRIVATE)
        val visitKey = "last_home_visit_${currentUid.ifBlank { "guest" }}"
        prefs.edit().putLong(visitKey, System.currentTimeMillis()).apply()
    }

    // Dismissible CampusHive 2.0 release announcement banner (persisted per user by announcement ID)
    val currentAnnouncementId = "campushive_2_0"
    val announcementPrefKey = "dismissed_announcement_${currentUid.ifBlank { "guest" }}"
    var showAnnouncementBanner by remember(currentUid) {
        val prefs = context.getSharedPreferences("campushive_prefs", Context.MODE_PRIVATE)
        mutableStateOf(prefs.getString(announcementPrefKey, null) != currentAnnouncementId)
    }

    val recommendedListings = remember(liveListings, currentUid, currentUserName, userProfile, conversations, savedIds) {
        ListingRecommender.recommend(
            allListings = liveListings,
            currentUid = currentUid,
            currentUserName = currentUserName,
            userProfile = userProfile,
            conversations = conversations,
            savedIds = savedIds,
            limit = 6
        )
    }

    if (showNotificationsDialog) {
        NotificationCenterDialog(
            conversations = notificationConversations,
            onDismiss = { showNotificationsDialog = false },
            onMarkAllRead = { chatViewModel.markAllConversationsAsRead() },
            onOpenChat = { chat ->
                showNotificationsDialog = false
                chatViewModel.markConversationAsRead(chat.id)
                onOpenChat(chat.id, chat.studentName, chat.serviceTitle)
            }
        )
    }

    val subtleHoney = BrandHoney
    val heroSearchSurface = if (CampusThemeState.isDark) Color(0xFF09090B) else Cream
    val heroBellSurface = if (CampusThemeState.isDark) Color(0xFF202024) else Cream
    val scrollState = rememberScrollState()
    var isScrolled by remember { mutableStateOf(false) }
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }.collect { offset ->
            if (!isScrolled && offset > 72) {
                isScrolled = true
            } else if (isScrolled && offset < 12) {
                isScrolled = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HoneyBottomGradient)
    ) {
        // =====================================================================
        // 1. STICKY COLLAPSIBLE HERO HEADER (compact when scrolled, never clips camera)
        // =====================================================================
        Surface(
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp),
            shadowElevation = if (isScrolled) 4.dp else 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        start = Spacing.gutter,
                        end = Spacing.gutter,
                        top = 12.dp,
                        bottom = if (isScrolled) 12.dp else 16.dp
                    )
            ) {
                // Always-Pinned Top Row: Avatar + Name/Campus + Notification Bell
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(HoneyTint)
                                .border(BorderStroke(1.5.dp, TileOutline), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Column {
                            Text(
                                text = firstName,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                            Text(
                                text = campusName,
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }

                    // Notification Bell Button with Unread Badge
                    Box(contentAlignment = Alignment.TopEnd) {
                        Surface(
                            shape = CircleShape,
                            color = heroBellSurface,
                            border = BorderStroke(
                                1.dp,
                                if (totalUnreadCount > 0) subtleHoney else Hairline
                            ),
                            modifier = Modifier
                                .size(40.dp)
                                .bounceClick(scaleDown = 0.90f) {
                                    showNotificationsDialog = true
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Charcoal,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        if (totalUnreadCount > 0) {
                            Surface(
                                color = subtleHoney,
                                shape = CircleShape,
                                border = BorderStroke(2.dp, CardWhite),
                                modifier = Modifier
                                    .offset(x = 3.dp, y = (-2).dp)
                                    .size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (totalUnreadCount > 9) "9+" else totalUnreadCount.toString(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF09090B)
                                    )
                                }
                            }
                        }
                    }
                }

                // Collapsible Center Hero Greeting (hides smoothly on scroll with hysteresis)
                AnimatedVisibility(
                    visible = !isScrolled,
                    enter = expandVertically(tween(180, easing = FastOutSlowInEasing)) + fadeIn(tween(150)),
                    exit = shrinkVertically(tween(160, easing = FastOutSlowInEasing)) + fadeOut(tween(120))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp, bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = heroGreeting,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal,
                            letterSpacing = (-0.7).sp
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = heroSubtitle,
                            fontSize = 13.sp,
                            color = MutedText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(if (isScrolled) 10.dp else 14.dp))

                // Always-Sticky Search Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = heroSearchSurface,
                    border = BorderStroke(1.dp, Hairline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick(scaleDown = 0.98f) { onSearchBarClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = MutedText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Search tutors, design, tech...",
                                fontSize = 13.sp,
                                color = MutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Honey
                        ) {
                            Text(
                                text = "Explore",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnHoneyAccent
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 2. SCROLLABLE FEED BELOW STICKY HEADER
        // =====================================================================
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                scope.launch {
                    authViewModel.fetchUserProfile()
                    listingViewModel.refreshUserListings()
                    chatViewModel.refreshConversations()
                    delay(800.milliseconds)
                    isRefreshing = false
                    Toast.makeText(context, "Campus feeds updated", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = 14.dp)
            ) {
                // Dismissible CampusHive 2.0 Announcement Banner
                AnimatedVisibility(
                    visible = showAnnouncementBanner,
                    exit = shrinkVertically(tween(200, easing = FastOutSlowInEasing)) + fadeOut(tween(150))
                ) {
                    Column {
                        CampusAnnouncementBanner(
                            onDismiss = {
                                val prefs = context.getSharedPreferences("campushive_prefs", Context.MODE_PRIVATE)
                                prefs.edit().putString(announcementPrefKey, currentAnnouncementId).apply()
                                showAnnouncementBanner = false
                            }
                        )
                        Spacer(Modifier.height(14.dp))
                    }
                }

                // Quick 3-Action Dock Card (scrolls cleanly with content)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.gutter),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, Hairline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickDockAction(
                            label = "Post Gig",
                            icon = Icons.Filled.Add,
                            isPrimary = true,
                            onClick = onNavigateToCreateService
                        )
                        QuickDockAction(
                            label = "Explore",
                            icon = Icons.Outlined.Search,
                            isPrimary = false,
                            onClick = onSearchBarClick
                        )
                        QuickDockAction(
                            label = if (totalUnreadCount > 0) "Inbox ($totalUnreadCount)" else "Inbox",
                            icon = Icons.Outlined.ChatBubbleOutline,
                            isPrimary = false,
                            badgeCount = totalUnreadCount,
                            onClick = {
                                if (upNextChat != null) {
                                    chatViewModel.markConversationAsRead(upNextChat.id)
                                    onOpenChat(upNextChat.id, upNextChat.studentName, upNextChat.serviceTitle)
                                } else {
                                    showNotificationsDialog = true
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

            // =================================================================
            // 2. FEATURED HIGHLIGHT BANNER (Top Recommendation or Offer Skill)
            // =================================================================
            val featuredRec = recommendedListings.firstOrNull()
            if (featuredRec != null) {
                val featured = featuredRec.listing
                val featuredVisual = getCategoryVisual(featured.category)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.gutter)
                        .bounceClick(scaleDown = 0.98f) { onListingClick(featured) },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = if (CampusThemeState.isDark) CardWhite else HoneyTint),
                    border = BorderStroke(1.dp, if (CampusThemeState.isDark) Hairline else WarningBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = featuredRec.reason.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (CampusThemeState.isDark) subtleHoney else WarningFg,
                                letterSpacing = 0.7.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = featured.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${featured.providerName} • RM ${"%.0f".format(featured.price)} ${featured.pricingType.shortLabel}",
                                fontSize = 12.sp,
                                color = MutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = Radius.Pill,
                                color = Honey
                            ) {
                                Text(
                                    text = "View Gig",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnHoneyAccent
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (CampusThemeState.isDark) HoneyTint else CardWhite.copy(alpha = 0.8f))
                                .border(BorderStroke(1.dp, TileOutline), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val glyphTint = if (CampusThemeState.isDark) subtleHoney else Charcoal
                            if (featuredVisual.symbol != null) {
                                Text(
                                    text = featuredVisual.symbol,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = glyphTint
                                )
                            } else {
                                Icon(
                                    imageVector = featuredVisual.icon,
                                    contentDescription = featured.category,
                                    tint = glyphTint,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
            }

            // =================================================================
            // 3. COMPACT CATEGORY PILLS ("Browse services")
            // =================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Browse categories",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
                Text(
                    text = "See all",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    modifier = Modifier.bounceClick { onNavigateToSearch("") }
                )
            }

            Spacer(Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = Spacing.gutter)
            ) {
                items(homeCategories) { cat ->
                    val count = liveListings.count {
                        it.category.equals(cat.name, ignoreCase = true) ||
                            (cat.name.equals("Tech Help", ignoreCase = true) && (it.category.equals("Coding", ignoreCase = true) || it.category.equals("Tech", ignoreCase = true))) ||
                            (cat.name.equals("Errands", ignoreCase = true) && it.category.equals("Campus Services", ignoreCase = true))
                    }
                    Surface(
                        shape = Radius.Card,
                        color = CardWhite,
                        border = BorderStroke(1.dp, Hairline),
                        modifier = Modifier.bounceClick(scaleDown = 0.94f) { onNavigateToSearch(cat.name) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HoneyTint)
                                    .border(BorderStroke(1.dp, TileOutline), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val glyphColor = if (CampusThemeState.isDark) subtleHoney else Charcoal
                                if (cat.symbolText != null) {
                                    Text(
                                        text = cat.symbolText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = glyphColor
                                    )
                                } else {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.name,
                                        tint = glyphColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = cat.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Charcoal
                                )
                                Text(
                                    text = "$count ${if (count == 1) "gig" else "gigs"}",
                                    fontSize = 10.sp,
                                    color = MutedText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // =================================================================
            // 4. GROUPED RECOMMENDED FEED CARD (mirrors Recent Activity list)
            // =================================================================
            val feedItems = remember(recommendedListings, liveListings) {
                if (recommendedListings.size > 1) {
                    recommendedListings.drop(1)
                } else if (recommendedListings.isNotEmpty()) {
                    recommendedListings
                } else {
                    emptyList()
                }
            }

            if (feedItems.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.gutter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recommended for you",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                    Text(
                        text = "See all",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
                        modifier = Modifier.bounceClick { onNavigateToSearch("") }
                    )
                }

                Spacer(Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.gutter),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    border = BorderStroke(1.dp, Hairline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        feedItems.forEachIndexed { index, rec ->
                            val item = rec.listing
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClick(scaleDown = 0.98f) { onListingClick(item) }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ServiceCategoryIconBox(
                                    category = item.category,
                                    size = 42.dp,
                                    shape = CircleShape,
                                    symbolFontSize = 16.sp,
                                    iconSize = 18.dp
                                )

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Charcoal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${item.providerName} • ${rec.reason}",
                                            fontSize = 11.sp,
                                            color = MutedText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(Modifier.width(10.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "RM ${"%.0f".format(item.price)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Charcoal
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = if (item.reviewCount > 0) StarGold else MutedText,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            text = if (item.reviewCount > 0) "%.1f".format(item.rating) else "New",
                                            fontSize = 11.sp,
                                            color = MutedText
                                        )
                                    }
                                }
                            }

                            if (index < feedItems.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    color = Hairline,
                                    thickness = 0.8.dp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

                Spacer(Modifier.height(rememberBottomBarClearance()))
            }
        }
    }
}

@Composable
private fun CampusAnnouncementBanner(
    onDismiss: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val highlights = remember {
        listOf(
            "Refreshed UI & Dark Mode" to "Cleaner cards, sticky headers, and cohesive colors.",
            "Fluid Drag Navigation" to "Glide across tabs or drop right onto + to post.",
            "Upgraded Chat & Listings" to "Sleeker composer, inline validation, and faster flows."
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (CampusThemeState.isDark) CardWhite else HoneyTint
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (CampusThemeState.isDark) BrandHoney.copy(alpha = 0.45f) else WarningBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = Radius.Pill,
                    color = Honey
                ) {
                    Text(
                        text = "2.0",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnHoneyAccent
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss announcement",
                        tint = Charcoal,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "CampusHive 2.0 is here",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                letterSpacing = (-0.3).sp
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = "Refreshed design, smoother navigation & faster chats",
                fontSize = 13.sp,
                color = MutedText,
                lineHeight = 18.sp
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(180, easing = FastOutSlowInEasing)) + fadeIn(tween(150)),
                exit = shrinkVertically(tween(160, easing = FastOutSlowInEasing)) + fadeOut(tween(120))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(
                        color = if (CampusThemeState.isDark) Hairline else WarningBorder.copy(alpha = 0.6f),
                        thickness = 0.8.dp
                    )
                    highlights.forEach { (title, subtitle) ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (CampusThemeState.isDark) BrandHoney else Charcoal)
                            )
                            Column {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal
                                )
                                Text(
                                    text = subtitle,
                                    fontSize = 11.sp,
                                    color = MutedText,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isExpanded) "Hide details" else "What's new",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
                if (!isExpanded) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Charcoal,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickDockAction(
    label: String,
    icon: ImageVector,
    isPrimary: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val circleBg = if (isPrimary) Honey else HoneyTint
    val iconTint = if (isPrimary) OnHoneyAccent else Charcoal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(Radius.Inner)
            .bounceClick(scaleDown = 0.92f, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(circleBg)
                    .then(
                        if (CampusThemeState.isDark && !isPrimary) {
                            Modifier.border(BorderStroke(1.dp, TileOutline), CircleShape)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(21.dp)
                )
            }

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = 2.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(DangerFgStrong)
                        .border(2.dp, CardWhite, CircleShape)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Charcoal,
            maxLines = 1
        )
    }
}
