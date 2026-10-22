package com.example.campushive.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.model.ChatConversation
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.ChatViewModel

@Composable
fun ChatsScreen(
    chatViewModel: ChatViewModel = viewModel(),
    onChatClick: (ChatConversation) -> Unit = {},
    onReviewClick: (ChatConversation) -> Unit = {}
) {
    val conversations by chatViewModel.conversations.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredChats = remember(searchQuery, conversations) {
        if (searchQuery.isBlank()) conversations
        else conversations.filter {
            it.studentName.contains(searchQuery, ignoreCase = true) ||
                    it.serviceTitle.contains(searchQuery, ignoreCase = true) ||
                    it.lastMessage.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .statusBarsPadding()
    ) {
        // Header
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Chats",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )

            Spacer(Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search conversations...", fontSize = 13.sp, color = MutedText) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Charcoal) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardWhite,
                    unfocusedContainerColor = CardWhite,
                    focusedBorderColor = Honey,
                    unfocusedBorderColor = Hairline
                )
            )
        }

        HorizontalDivider(color = Hairline)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            if (filteredChats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (conversations.isEmpty()) "No student chats yet. Tap 'Chat' on any listing to start!" else "No chats matched your search.",
                            color = MutedText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(filteredChats) { chat ->
                    ChatItem(
                        chat = chat,
                        onClick = { onChatClick(chat) },
                        onReviewClick = { onReviewClick(chat) }
                    )
                    HorizontalDivider(color = Hairline, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }

        // Bottom Info Banner
        Surface(
            color = CreamTint,
            border = BorderStroke(1.dp, Hairline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = MutedText, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Chats start from a listing • Tap 'Chat' on any service to open a 1-on-1 conversation.",
                    fontSize = 11.sp,
                    color = MutedText,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun ChatItem(
    chat: ChatConversation,
    onClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    val isCompleted = chat.id == "chat_3"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Initials Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Honey),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.studentName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString(""),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Charcoal
            )
        }

        Spacer(Modifier.width(12.dp))

        // Student Info & Message
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
                text = chat.lastMessage,
                fontSize = 12.sp,
                color = Charcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        // Timestamp, Unread Badge, or Review Button
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = chat.timestamp,
                fontSize = 11.sp,
                color = MutedText
            )

            Spacer(Modifier.height(4.dp))

            if (isCompleted) {
                Button(
                    onClick = onReviewClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Honey, contentColor = Charcoal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("REVIEW", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            } else if (chat.unreadCount > 0) {
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