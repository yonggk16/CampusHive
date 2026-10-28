package com.example.campushive.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.model.ChatConversation
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.ChatViewModel
import com.example.campushive.viewmodel.ReviewViewModel

private val roleFilters = listOf("All", "As seeker", "As provider")

@Composable
fun ChatsScreen(
    chatViewModel: ChatViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel(),
    onChatClick: (ChatConversation) -> Unit = {},
    onReviewClick: (ChatConversation) -> Unit = {}
) {
    val conversations by chatViewModel.conversations.collectAsState()
    val providerReviews by reviewViewModel.providerReviews.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("All") }

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
            .statusBarsPadding()
    ) {
        // Header, Search Field & Role Filter Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp)
        ) {
            Text(
                text = "Chats",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )

            Spacer(Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search conversations", fontSize = 14.sp, color = MutedText) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Charcoal) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardWhite,
                    unfocusedContainerColor = CardWhite,
                    focusedBorderColor = Honey,
                    unfocusedBorderColor = Hairline
                )
            )

            Spacer(Modifier.height(12.dp))

            // Role Filter Chips (All | As seeker | As provider)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(roleFilters) { role ->
                    val isSelected = selectedRoleFilter == role
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Charcoal else CardWhite,
                        contentColor = if (isSelected) CardWhite else Charcoal,
                        border = if (isSelected) null else BorderStroke(1.dp, Hairline),
                        modifier = Modifier.bounceClick(scaleDown = 0.93f) { selectedRoleFilter = role }
                    ) {
                        Text(
                            text = role,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CardWhite else Charcoal
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Chats List or Clean Centered Empty State
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (filteredChats.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp, start = 32.dp, end = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (conversations.isEmpty()) "No chats yet" else "No chats matched your search",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (conversations.isEmpty())
                            "Chats start from a service listing. Find one you like, tap Chat, and message a peer or ask a question."
                        else
                            "Try searching for another student name, service title, or changing the role filter.",
                        fontSize = 14.sp,
                        color = MutedText,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filteredChats) { chat ->
                        val isReviewed = remember(providerReviews, chat) {
                            providerReviews.any { r ->
                                (r.listingId.isNotBlank() && chat.listingId.isNotBlank() && r.listingId == chat.listingId) ||
                                        (r.serviceTitle.equals(chat.serviceTitle, ignoreCase = true) && r.providerName.equals(chat.studentName, ignoreCase = true))
                            }
                        }
                        ChatItem(
                            chat = chat,
                            isReviewed = isReviewed,
                            onClick = { onChatClick(chat) },
                            onReviewClick = { onReviewClick(chat) }
                        )
                        HorizontalDivider(color = Hairline, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatItem(
    chat: ChatConversation,
    isReviewed: Boolean = false,
    onClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Initials Avatar with Online/Offline Presence Badge
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(HoneyTint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.studentName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifBlank { "S" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Charcoal
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (chat.isOnline) OnlineGreen else OfflineGrey)
                    .border(2.dp, CardWhite, CircleShape)
            )
        }

        Spacer(Modifier.width(12.dp))

        // Counterpart Student Info & Last Message
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.studentName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Charcoal
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
                text = "Re: ${chat.serviceTitle}",
                fontSize = 11.sp,
                color = MutedText,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Re: ${chat.lastMessage}",
                fontSize = 12.sp,
                color = Charcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        // Timestamp & Action Button
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = chat.timestamp,
                fontSize = 11.sp,
                color = MutedText
            )

            if (!chat.isProvider) {
                Spacer(Modifier.height(6.dp))

                if (isReviewed) {
                    Surface(
                        color = CreamTint,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Hairline)
                    ) {
                        Text(
                            text = "REVIEWED ✓",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText
                        )
                    }
                } else {
                    Button(
                        onClick = onReviewClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = CardWhite),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("REVIEW", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CardWhite)
                    }
                }
            } else if (chat.unreadCount > 0) {
                Spacer(Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Honey),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chat.unreadCount.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                }
            }
        }
    }
}
