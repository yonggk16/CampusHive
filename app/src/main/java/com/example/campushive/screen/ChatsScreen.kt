package com.example.campushive.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.components.CampusEmptyState
import com.example.campushive.model.ChatConversation
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.ChatViewModel
import com.example.campushive.viewmodel.ReviewViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val roleFilters = listOf("All", "As seeker", "As provider")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    chatViewModel: ChatViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel(),
    onChatClick: (ChatConversation) -> Unit = {},
    onReviewClick: (ChatConversation) -> Unit = {}
) {
    val context = LocalContext.current
    val conversations by chatViewModel.conversations.collectAsState()
    val providerReviews by reviewViewModel.providerReviews.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("All") }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filteredChats = remember(searchQuery, selectedRoleFilter, conversations) {
        conversations.filter { chat ->
            val matchesRole = when (selectedRoleFilter) {
                "As seeker" -> !chat.isProvider
                "As provider" -> chat.isProvider
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                chat.studentName.contains(searchQuery, ignoreCase = true) ||
                chat.serviceTitle.contains(searchQuery, ignoreCase = true) ||
                chat.lastMessage.contains(searchQuery, ignoreCase = true)

            matchesRole && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HoneyBottomGradient)
    ) {
        // =====================================================================
        // 1. STICKY CURVED WHITE TOP HEADER (Unified 26.sp Title + Search + Filters)
        // =====================================================================
        Surface(
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.gutter, vertical = 12.dp)
            ) {
                // Header Row: Unified 26.sp Title + Active Count Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chats",
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        letterSpacing = (-0.5).sp
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = HoneyTint,
                        border = BorderStroke(
                            1.dp,
                            if (CampusThemeState.isDark) Hairline else WarningBorderSoft
                        )
                    ) {
                        Text(
                            text = "${conversations.size} active",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Cream-filled Search Bar (matches SearchScreen)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    textStyle = LocalTextStyle.current.copy(color = Charcoal, fontSize = 14.sp),
                    placeholder = {
                        Text(
                            text = "Search people or services...",
                            fontSize = 13.sp,
                            color = MutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = MutedText,
                            modifier = Modifier.size(19.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.bounceClick(scaleDown = 0.85f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear search",
                                    tint = MutedText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Cream,
                        unfocusedContainerColor = Cream,
                        focusedBorderColor = Charcoal,
                        unfocusedBorderColor = Hairline,
                        focusedTextColor = Charcoal,
                        unfocusedTextColor = Charcoal,
                        cursorColor = Charcoal
                    )
                )

                Spacer(Modifier.height(10.dp))

                // Role Filter Pills (All | As seeker | As provider)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(roleFilters) { role ->
                        val isSelected = selectedRoleFilter == role
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) Honey else Cream,
                            contentColor = if (isSelected) OnHoneyAccent else Charcoal,
                            border = BorderStroke(1.dp, if (isSelected) Honey else Hairline),
                            modifier = Modifier.bounceClick(scaleDown = 0.93f) { selectedRoleFilter = role }
                        ) {
                            Text(
                                text = role,
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 7.dp),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) OnHoneyAccent else Charcoal
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 2. STANDARD CHAT-APP CONVERSATION LIST (Clean Rows + Inset Dividers)
        // =====================================================================
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                scope.launch {
                    chatViewModel.refreshConversations()
                    reviewViewModel.loadReviewsForProvider()
                    delay(800)
                    isRefreshing = false
                    Toast.makeText(context, "Conversations refreshed", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (filteredChats.isEmpty()) {
                val hasFilter = searchQuery.isNotBlank() || selectedRoleFilter != "All"
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 28.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CampusEmptyState(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = if (conversations.isEmpty()) "No chats yet" else "No matching chats",
                        subtitle = if (conversations.isEmpty())
                            "Chats start from a service listing. Find one you like, tap Chat, and message a peer."
                        else
                            "Try another student name, service title, or switch your role filter.",
                        actionLabel = if (hasFilter && conversations.isNotEmpty()) "Reset Filters" else null,
                        onAction = if (hasFilter && conversations.isNotEmpty()) {
                            {
                                searchQuery = ""
                                selectedRoleFilter = "All"
                            }
                        } else null
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 6.dp,
                        bottom = rememberBottomBarClearance()
                    )
                ) {
                    items(filteredChats, key = { it.id }) { chat ->
                        val isReviewed = remember(providerReviews, chat) {
                            providerReviews.any { r ->
                                (r.listingId.isNotBlank() && chat.listingId.isNotBlank() && r.listingId == chat.listingId) ||
                                    (r.serviceTitle.equals(chat.serviceTitle, ignoreCase = true) && r.providerName.equals(chat.studentName, ignoreCase = true))
                            }
                        }
                        ChatListRow(
                            chat = chat,
                            isReviewed = isReviewed,
                            onClick = {
                                chatViewModel.markConversationAsRead(chat.id)
                                onChatClick(chat)
                            },
                            onReviewClick = { onReviewClick(chat) }
                        )
                        HorizontalDivider(
                            color = Hairline,
                            thickness = 0.7.dp,
                            modifier = Modifier.padding(start = 78.dp, end = Spacing.gutter)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListRow(
    chat: ChatConversation,
    isReviewed: Boolean = false,
    onClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    val isUnread = chat.unreadCount > 0
    val formattedPreview = remember(chat.lastMessage) {
        val raw = chat.lastMessage.trim()
        when {
            raw.isEmpty() -> "Tap to start chatting"
            raw.equals("[Photo]", ignoreCase = true) -> "Photo attachment"
            raw.equals("[GIF]", ignoreCase = true) -> "GIF"
            raw.equals("[Sticker]", ignoreCase = true) -> "Sticker"
            else -> raw
        }
    }
    val initials = remember(chat.studentName) {
        chat.studentName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifBlank { "S" }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.99f, onClick = onClick)
            .padding(horizontal = Spacing.gutter, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 50dp Circular Avatar + Online/Offline Presence Dot
        Box(
            modifier = Modifier.size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(HoneyTint)
                    .border(BorderStroke(1.5.dp, AvatarOutline), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Charcoal
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(if (chat.isOnline) OnlineGreen else OfflineGrey)
                    .border(2.dp, AppMainBackground, CircleShape)
            )
        }

        Spacer(Modifier.width(12.dp))

        // Clean Chat App Row: Name + Timestamp, Last Message + Unread, Subtle Service + Review
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.studentName.ifBlank { "Campus Student" },
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Charcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (chat.isVerifiedStudent) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Verified Student",
                            tint = VerifiedGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = chat.timestamp,
                    fontSize = 11.sp,
                    maxLines = 1,
                    fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isUnread) BrandHoney else MutedText
                )
            }

            Spacer(Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isLastRead = chat.isLastMessageFromMe &&
                    chat.lastMessageMillis > 0L &&
                    chat.partnerLastReadMillis >= chat.lastMessageMillis

                if (chat.isPartnerTyping) {
                    Text(
                        text = "typing...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnlineGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    if (chat.isLastMessageFromMe && chat.lastMessage.isNotBlank()) {
                        Icon(
                            imageVector = if (isLastRead) Icons.Filled.DoneAll else Icons.Filled.Check,
                            contentDescription = if (isLastRead) "Read" else "Sent",
                            tint = if (isLastRead) ReadReceiptBlue else MutedText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = formattedPreview,
                        fontSize = 13.sp,
                        fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isUnread) Charcoal else MutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isUnread) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(BrandHoney),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (chat.unreadCount > 9) "9+" else chat.unreadCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF09090B)
                        )
                    }
                }
            }

            Spacer(Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = chat.serviceTitle.ifBlank { "Campus Service" },
                    fontSize = 11.sp,
                    color = MutedText.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (!chat.isProvider) {
                    Spacer(Modifier.width(10.dp))
                    if (isReviewed) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = SuccessFg,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Reviewed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SuccessFg
                            )
                        }
                    } else {
                        Surface(
                            color = HoneyTint,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                0.8.dp,
                                if (CampusThemeState.isDark) BrandHoney else WarningBorderSoft
                            ),
                            modifier = Modifier.bounceClick(scaleDown = 0.94f, onClick = onReviewClick)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = if (CampusThemeState.isDark) BrandHoney else Charcoal,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "Review",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Charcoal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
