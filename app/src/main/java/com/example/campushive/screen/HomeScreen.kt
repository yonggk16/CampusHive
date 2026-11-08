package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.campushive.ui.theme.bounceClick
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.model.ChatConversation
import com.example.campushive.model.Listing
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.AuthViewModel
import com.example.campushive.viewmodel.ChatViewModel
import com.example.campushive.viewmodel.ListingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private data class ServiceCategory(
    val id: String,
    val name: String,
    val backgroundColor: Color,
    val icon: ImageVector,
    val symbolText: String? = null
)

private val homeCategories = listOf(
    ServiceCategory("tutoring", "Tutoring", CardWhite, Icons.Outlined.School, "Σ"),
    ServiceCategory("design", "Design", CardWhite, Icons.Outlined.Palette, "✎"),
    ServiceCategory("tech", "Tech Help", CardWhite, Icons.Outlined.Code, "</>"),
    ServiceCategory("errands", "Errands", CardWhite, Icons.Filled.Check, "✓"),
    ServiceCategory("others", "Others", CardWhite, Icons.Outlined.Category, "✦")
)

private data class QuickHelpItem(
    val title: String,
    val categoryQuery: String
)

private val quickHelpItems = listOf(
    QuickHelpItem("Study for an exam", "Tutoring"),
    QuickHelpItem("Design a poster", "Design"),
    QuickHelpItem("Fix my code or laptop", "Tech Help")
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

    val totalUnreadCount = remember(conversations) {
        conversations.sumOf { it.unreadCount }
    }

    val notificationConversations = remember(conversations) {
        conversations
            .filter { it.lastMessage.isNotBlank() && !it.lastMessage.equals("Tap to start chatting", ignoreCase = true) }
            .sortedWith(compareByDescending<ChatConversation> { it.unreadCount > 0 }.thenByDescending { it.lastMessageMillis })
    }

    val upNextChat = remember(conversations) {
        conversations.firstOrNull { it.unreadCount > 0 || it.lastMessage.isNotBlank() }
    }

    val currentUserName = userProfile?.displayName.orEmpty().ifBlank {
        authViewModel.currentUser?.displayName.orEmpty().ifBlank {
            authViewModel.currentUser?.email?.substringBefore("@").orEmpty().ifBlank { "Student" }
        }
    }

    val firstName = currentUserName.split(" ").firstOrNull() ?: "Student"

    // In-App Notification Center Dialog (Redesigned CampusHive Theme)
    if (showNotificationsDialog) {
        Dialog(onDismissRequest = { showNotificationsDialog = false }) {
            Surface(
                shape = Radius.Card,
                color = CardWhite,
                border = BorderStroke(1.dp, Hairline),
                shadowElevation = 14.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Notifications",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                            if (totalUnreadCount > 0) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = HoneyTint,
                                    shape = Radius.Card,
                                    border = BorderStroke(1.dp, WarningBorderSoft)
                                ) {
                                    Text(
                                        text = "$totalUnreadCount new",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Charcoal
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (totalUnreadCount > 0) {
                                TextButton(
                                    onClick = { chatViewModel.markAllConversationsAsRead() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Mark all read",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningFgVivid
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showNotificationsDialog = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = Charcoal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Hairline)
                    Spacer(Modifier.height(14.dp))

                    if (notificationConversations.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(HoneyTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = Charcoal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "You're all caught up!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "New messages from seekers and providers will appear here in real time.",
                                fontSize = 12.sp,
                                color = MutedText,
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(notificationConversations) { chat ->
                                val isUnread = chat.unreadCount > 0
                                Surface(
                                    shape = Radius.Card,
                                    color = if (isUnread) HoneyTint.copy(alpha = 0.5f) else CardWhite,
                                    border = BorderStroke(1.dp, if (isUnread) Honey else Hairline),
                                    shadowElevation = if (isUnread) 2.dp else 0.5.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bounceClick(scaleDown = 0.97f) {
                                            showNotificationsDialog = false
                                            chatViewModel.markConversationAsRead(chat.id)
                                            onOpenChat(chat.id, chat.studentName, chat.serviceTitle)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Initials Avatar
                                        Box(
                                            modifier = Modifier.size(44.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .background(if (isUnread) Honey else HoneyTint),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = chat.studentName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifBlank { "S" },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Charcoal
                                                )
                                            }
                                        }

                                        Spacer(Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = chat.studentName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = Charcoal,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (chat.isVerifiedStudent) {
                                                        Spacer(Modifier.width(4.dp))
                                                        Icon(
                                                            imageVector = Icons.Filled.CheckCircle,
                                                            contentDescription = "Verified Student",
                                                            tint = VerifiedGreen,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = chat.timestamp,
                                                    fontSize = 11.sp,
                                                    color = MutedText
                                                )
                                            }

                                            Spacer(Modifier.height(2.dp))

                                            Text(
                                                text = "${if (chat.isProvider) "Seeker inquiry" else "Provider reply"} · ${chat.serviceTitle}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MutedText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(Modifier.height(4.dp))

                                            Text(
                                                text = chat.lastMessage,
                                                fontSize = 13.sp,
                                                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                                                color = Charcoal,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        if (isUnread) {
                                            Spacer(Modifier.width(10.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(DangerFgStrong),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = chat.unreadCount.toString(),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CardWhite
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
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HoneyBottomGradient)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = Spacing.bottomBarClearance)
        ) {
            Spacer(Modifier.height(28.dp))

            // 1. Top Greeting Row ("Hi, $firstName" + Notification Bell Button with Unread Badge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hi, $firstName",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    letterSpacing = (-0.8).sp
                )

                // Redesigned Modern Notification Bell Button
                Box(contentAlignment = Alignment.TopEnd) {
                    Surface(
                        shape = CircleShape,
                        color = CardWhite,
                        border = BorderStroke(1.dp, if (totalUnreadCount > 0) Honey else Hairline),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(46.dp)
                            .bounceClick(scaleDown = 0.90f) {
                                showNotificationsDialog = true
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Charcoal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    if (totalUnreadCount > 0) {
                        Surface(
                            color = DangerFgStrong,
                            shape = CircleShape,
                            border = BorderStroke(2.dp, CardWhite),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .offset(x = 4.dp, y = (-3).dp)
                                .size(20.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = if (totalUnreadCount > 9) "9+" else totalUnreadCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CardWhite
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 2. Search Services Bar
            Surface(
                shape = Radius.Pill,
                color = CardWhite,
                border = BorderStroke(1.dp, Hairline),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = Spacing.gutter)
                    .bounceClick(scaleDown = 0.98f) { onSearchBarClick() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MutedText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Search services",
                        fontSize = 15.sp,
                        color = MutedText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // 3. Up Next Card
            if (upNextChat != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.gutter)
                        .bounceClick(scaleDown = 0.98f) {
                            chatViewModel.markConversationAsRead(upNextChat.id)
                            onOpenChat(upNextChat.id, upNextChat.studentName, upNextChat.serviceTitle)
                        },
                    shape = Radius.Card,
                    colors = CardDefaults.cardColors(containerColor = Charcoal),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (upNextChat.unreadCount > 0)
                                    "NEW MESSAGE (${upNextChat.unreadCount}) · ${upNextChat.timestamp.uppercase()}"
                                else
                                    "UP NEXT · ${upNextChat.timestamp.uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (upNextChat.unreadCount > 0) Honey else CardWhite.copy(alpha = 0.7f),
                                letterSpacing = 0.6.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${upNextChat.serviceTitle} with ${upNextChat.studentName}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CardWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Button(
                            onClick = {
                                chatViewModel.markConversationAsRead(upNextChat.id)
                                onOpenChat(upNextChat.id, upNextChat.studentName, upNextChat.serviceTitle)
                            },
                            shape = Radius.Pill,
                            colors = ButtonDefaults.buttonColors(containerColor = CardWhite, contentColor = Charcoal),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.heightIn(min = 44.dp)
                        ) {
                            Text(
                                text = "Open",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Charcoal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
            }

            // 4. Section Header: "Browse services"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Browse services",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal,
                    letterSpacing = (-0.2).sp
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

            // 5. Browse Services Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = Spacing.gutter)
            ) {
                items(homeCategories) { cat ->
                    Card(
                        modifier = Modifier
                            .width(140.dp)
                            .height(120.dp)
                            .clip(Radius.Card)
                            .bounceClick(scaleDown = 0.94f) { onNavigateToSearch(cat.name) },
                        shape = Radius.Card,
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        border = BorderStroke(1.dp, Hairline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(Radius.Tile)
                                    .background(HoneyTint),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cat.symbolText != null) {
                                    Text(
                                        text = cat.symbolText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Charcoal
                                    )
                                } else {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.name,
                                        tint = Charcoal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            val count = liveListings.count {
                                it.category.equals(cat.name, ignoreCase = true) ||
                                    (cat.name.equals("Tech Help", ignoreCase = true) && (it.category.equals("Coding", ignoreCase = true) || it.category.equals("Tech", ignoreCase = true))) ||
                                    (cat.name.equals("Errands", ignoreCase = true) && it.category.equals("Campus Services", ignoreCase = true))
                            }
                            val countText = "$count ${if (count == 1) "listing" else "listings"}"

                            Column {
                                Text(
                                    text = cat.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Charcoal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = countText,
                                    fontSize = 12.sp,
                                    color = MutedText,
                                    letterSpacing = 0.2.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // 6. Section Header: "What do you need help with?"
            Column(modifier = Modifier.padding(horizontal = Spacing.gutter)) {
                Text(
                    text = "What do you need help with?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal,
                    letterSpacing = (-0.2).sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // 7. Grouped Help Action List
            Column(modifier = Modifier.padding(horizontal = Spacing.gutter)) {
                Surface(
                    shape = Radius.Card,
                    color = CardWhite,
                    border = BorderStroke(1.dp, Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        quickHelpItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClick { onNavigateToSearch(item.categoryQuery) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(Modifier.width(4.dp))

                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Charcoal,
                                    modifier = Modifier.weight(1f)
                                )

                                Surface(
                                    shape = Radius.Pill,
                                    color = Charcoal,
                                    border = BorderStroke(1.dp, WarningFgStrong),
                                    modifier = Modifier
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .heightIn(min = 44.dp)
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Find",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CardWhite
                                        )
                                    }
                                }
                            }

                            if (index < quickHelpItems.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 64.dp),
                                    color = Hairline,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 8. "WHAT'S NEW" Callout Offer Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter)
                    .bounceClick(scaleDown = 0.98f) { onNavigateToCreateService() },
                shape = Radius.Card,
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                border = BorderStroke(1.dp, Hairline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "WHAT'S NEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedText,
                        letterSpacing = 0.6.sp
                    )

                    Spacer(Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Got a skill? List it in 2 minutes.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Charcoal,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "Offer →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )
                    }
                }
            }
        }
    }
}
