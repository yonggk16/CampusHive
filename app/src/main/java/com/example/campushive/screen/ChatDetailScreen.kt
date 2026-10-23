package com.example.campushive.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CropSquare
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
import com.example.campushive.components.ReviewDialog
import com.example.campushive.model.ChatMessage
import com.example.campushive.model.ReportType
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.ChatViewModel
import com.example.campushive.viewmodel.HelpdeskViewModel
import com.example.campushive.viewmodel.ReviewViewModel

@Composable
fun ChatDetailScreen(
    chatId: String,
    studentName: String,
    serviceTitle: String,
    chatViewModel: ChatViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel(),
    helpdeskViewModel: HelpdeskViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val liveMessages by chatViewModel.getMessagesFlow(chatId).collectAsState(initial = emptyList())
    val listState = rememberLazyListState()

    // Extract listingId if available from chatId (e.g., chat_listing123 -> listing123)
    val extractedListingId = remember(chatId) {
        if (chatId.startsWith("chat_")) chatId.removePrefix("chat_") else chatId
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(liveMessages.size) {
        if (liveMessages.isNotEmpty()) {
            listState.animateScrollToItem(liveMessages.lastIndex)
        }
    }

    if (showReviewDialog) {
        ReviewDialog(
            listingId = extractedListingId,
            providerName = studentName,
            serviceTitle = serviceTitle,
            reviewViewModel = reviewViewModel,
            onDismiss = { showReviewDialog = false },
            onSubmit = { showReviewDialog = false }
        )
    }

    if (showReportDialog) {
        ReportDialog(
            initialType = ReportType.USER_SAFETY,
            targetId = chatId,
            targetName = studentName,
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
        // Top Header Bar
        Surface(
            color = CardWhite,
            shadowElevation = 1.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Charcoal)
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Honey),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = studentName.take(1),
                            fontWeight = FontWeight.Bold,
                            color = Charcoal,
                            fontSize = 14.sp
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(VerifiedGreen)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = studentName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Charcoal
                        )
                        Text(
                            text = "Online now",
                            fontSize = 11.sp,
                            color = VerifiedGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Leave Review Star Button in Top Bar
                    IconButton(onClick = { showReviewDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Leave Review",
                            tint = Color(0xFFFFB800)
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = Charcoal)
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(CardWhite)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Report user safety concern", color = Charcoal, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    showMenu = false
                                    showReportDialog = true
                                }
                            )
                        }
                    }
                }

                // Service Context Banner
                Surface(
                    color = CardWhite,
                    border = BorderStroke(1.dp, Hairline),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEFEBFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.CropSquare, contentDescription = null, tint = Color(0xFF6C5CE7), modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(serviceTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Charcoal)
                                Text("Active Campus Service", fontSize = 10.sp, color = MutedText)
                            }
                        }

                        Surface(
                            color = Color(0xFFFFF7DB),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { showReviewDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF8C6B10),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "LEAVE REVIEW",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8C6B10)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
            }
        }

        // Messages Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Date Divider "TODAY"
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = Color(0xFFF6F3EE),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "TODAY",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText
                        )
                    }
                }
            }

            if (liveMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Start your conversation with $studentName",
                            color = MutedText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(liveMessages) { msg ->
                    MessageBubble(message = msg)
                }
            }
        }

        // Bottom Input Row
        Surface(
            color = CardWhite,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Plus button on left
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Cream)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = Charcoal, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.width(8.dp))

                // White pill input box
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Message ${studentName.split(" ").firstOrNull() ?: studentName}...", fontSize = 13.sp, color = MutedText) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = Honey,
                        unfocusedBorderColor = Hairline
                    )
                )

                Spacer(Modifier.width(8.dp))

                // Send FAB
                FloatingActionButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            val textToSend = messageText.trim()
                            messageText = ""
                            chatViewModel.sendMessage(
                                conversationId = chatId,
                                text = textToSend,
                                studentName = studentName,
                                serviceTitle = serviceTitle
                            )
                        }
                    },
                    containerColor = Honey,
                    contentColor = Charcoal,
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isMe = message.isFromMe
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = if (isMe) Charcoal else CardWhite,
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMe) 18.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 18.dp
                ),
                shadowElevation = 0.8.dp,
                border = if (isMe) null else BorderStroke(1.dp, Hairline)
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (isMe) CardWhite else Charcoal,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = message.time,
                fontSize = 10.sp,
                color = MutedText
            )
        }
    }
}
