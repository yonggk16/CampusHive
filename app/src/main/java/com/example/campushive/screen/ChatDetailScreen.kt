package com.example.campushive.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.model.ChatMessage
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.ChatViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale

private data class PendingAttachment(
    val type: String, // "IMAGE" | "FILE"
    val name: String,
    val sizeLabel: String,
    val base64Data: String,
    val previewBitmap: ImageBitmap? = null
)

@Composable
fun ChatDetailScreen(
    chatId: String,
    studentName: String,
    serviceTitle: String,
    chatViewModel: ChatViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    var pendingAttachment by remember { mutableStateOf<PendingAttachment?>(null) }
    var fullScreenImage by remember { mutableStateOf<ImageBitmap?>(null) }

    // Message being edited
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }

    val liveMessages by chatViewModel.getMessagesFlow(chatId).collectAsState(initial = emptyList())
    val conversations by chatViewModel.conversations.collectAsState()
    val listState = rememberLazyListState()

    val myName = remember {
        val user = Firebase.auth.currentUser
        user?.displayName?.trim()?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.trim()
            ?: ""
    }

    // Always resolve the counterpart's name from ChatViewModel so a Provider sees the Seeker's name
    val partnerName = remember(conversations, chatId, studentName, myName) {
        val convName = conversations.find { it.id == chatId }?.studentName
        when {
            !convName.isNullOrBlank() && !convName.equals(myName, ignoreCase = true) -> convName
            studentName.isNotBlank() && !studentName.equals(myName, ignoreCase = true) -> studentName
            !convName.isNullOrBlank() -> convName
            else -> "Student"
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        showAttachMenu = false
        if (uri != null) {
            val attachment = processImageUri(context, uri)
            if (attachment != null) {
                pendingAttachment = attachment
            } else {
                Toast.makeText(context, "Could not load selected image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // General file/document picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        showAttachMenu = false
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val attachment = if (mimeType.startsWith("image/")) {
                processImageUri(context, uri)
            } else {
                processFileUri(context, uri)
            }
            if (attachment != null) {
                pendingAttachment = attachment
            } else {
                Toast.makeText(context, "Could not attach selected file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(liveMessages.size) {
        if (liveMessages.isNotEmpty()) {
            listState.animateScrollToItem(liveMessages.lastIndex)
        }
    }

    // Full-screen image preview dialog when tapping an image in chat
    if (fullScreenImage != null) {
        Dialog(onDismissRequest = { fullScreenImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Charcoal)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = fullScreenImage!!,
                    contentDescription = "Full Image Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullScreenImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CardWhite.copy(alpha = 0.85f))
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Charcoal, modifier = Modifier.size(18.dp))
                }
            }
        }
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
                            text = partnerName.take(1).uppercase(),
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
                            text = partnerName,
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

                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = Charcoal)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "VIEW",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8C6B10)
                            )
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
                            text = "Start your conversation with $partnerName",
                            color = MutedText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(liveMessages) { msg ->
                    MessageBubble(
                        message = msg,
                        onImageClick = { bitmap -> fullScreenImage = bitmap },
                        onFileClick = { fileMsg -> saveAndNotifyFile(context, fileMsg) },
                        onEditMessage = { msgToEdit ->
                            editingMessage = msgToEdit
                            messageText = msgToEdit.text
                        },
                        onDeleteMessage = { msgToDelete ->
                            chatViewModel.deleteMessage(chatId, msgToDelete.id)
                            Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Bottom Input Area (Inline Editing Bar + Attachment Menu + Staged Attachment Preview + Input Row)
        Surface(
            color = CardWhite,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Inline Editing Message Banner Bar
                editingMessage?.let { editing ->
                    Surface(
                        color = Color(0xFFFFF7DB),
                        border = BorderStroke(1.dp, Honey),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = Charcoal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Editing message",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8C6B10)
                                    )
                                    Text(
                                        text = editing.text,
                                        fontSize = 12.sp,
                                        color = Charcoal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    editingMessage = null
                                    messageText = ""
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Cancel edit",
                                    tint = Charcoal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Expandable Upload Picker Menu (Picture vs File)
                AnimatedVisibility(visible = showAttachMenu) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFF7DB),
                            border = BorderStroke(1.dp, Honey),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { imagePickerLauncher.launch("image/*") }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = "Upload Picture",
                                    tint = Charcoal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Upload Picture",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEFEBFF),
                            border = BorderStroke(1.dp, Color(0xFFD0C6FF)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { filePickerLauncher.launch("*/*") }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = "Attach File",
                                    tint = Color(0xFF4834D4),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Attach File",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal
                                )
                            }
                        }
                    }
                }

                // Staged Attachment Preview Bar before sending
                pendingAttachment?.let { staged ->
                    Surface(
                        color = Cream,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Hairline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (staged.type == "IMAGE" && staged.previewBitmap != null) {
                                Image(
                                    bitmap = staged.previewBitmap,
                                    contentDescription = staged.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEFEBFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF6C5CE7),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = staged.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${if (staged.type == "IMAGE") "Picture" else "File"} • ${staged.sizeLabel}",
                                    fontSize = 11.sp,
                                    color = MutedText
                                )
                            }

                            IconButton(
                                onClick = { pendingAttachment = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove attachment",
                                    tint = MutedText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Plus button on left to toggle Picture / File upload options
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (showAttachMenu) Honey else Cream)
                            .clickable { showAttachMenu = !showAttachMenu },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (showAttachMenu) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = "Attach Picture or File",
                            tint = Charcoal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // White pill input box
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = {
                            Text(
                                text = when {
                                    editingMessage != null -> "Edit your message..."
                                    pendingAttachment != null -> "Add a caption (optional)..."
                                    else -> "Message ${partnerName.split(" ").firstOrNull() ?: partnerName}..."
                                },
                                fontSize = 13.sp,
                                color = MutedText
                            )
                        },
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

                    // Send FAB (sends new text, edits existing text, or sends attachment)
                    FloatingActionButton(
                        onClick = {
                            val currentStaged = pendingAttachment
                            val currentEditing = editingMessage
                            if (messageText.isNotBlank() || currentStaged != null) {
                                val textToSend = messageText.trim()
                                messageText = ""
                                pendingAttachment = null
                                showAttachMenu = false

                                if (currentEditing != null) {
                                    editingMessage = null
                                    chatViewModel.editMessage(chatId, currentEditing.id, textToSend)
                                    Toast.makeText(context, "Message edited", Toast.LENGTH_SHORT).show()
                                } else {
                                    chatViewModel.sendMessage(
                                        conversationId = chatId,
                                        text = textToSend,
                                        studentName = partnerName,
                                        serviceTitle = serviceTitle,
                                        attachmentType = currentStaged?.type ?: "",
                                        attachmentName = currentStaged?.name ?: "",
                                        attachmentSize = currentStaged?.sizeLabel ?: "",
                                        attachmentBase64 = currentStaged?.base64Data ?: ""
                                    )
                                }
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: ChatMessage,
    onImageClick: (ImageBitmap) -> Unit,
    onFileClick: (ChatMessage) -> Unit,
    onEditMessage: (ChatMessage) -> Unit,
    onDeleteMessage: (ChatMessage) -> Unit
) {
    val isMe = message.isFromMe
    var showMenu by remember { mutableStateOf(false) }

    val decodedBitmap: ImageBitmap? = remember(message.attachmentType, message.attachmentBase64) {
        if (message.attachmentType == "IMAGE" && message.attachmentBase64.isNotBlank()) {
            try {
                val bytes = Base64.decode(message.attachmentBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Box {
                Surface(
                    color = if (isMe) (if (message.isDeleted) Color(0xFF333333) else Charcoal) else CardWhite,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isMe) 18.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 18.dp
                    ),
                    shadowElevation = 0.8.dp,
                    border = if (isMe) null else BorderStroke(1.dp, Hairline),
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                if (isMe && !message.isDeleted) {
                                    showMenu = true
                                }
                            }
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = if (decodedBitmap != null) 8.dp else 14.dp,
                            vertical = if (decodedBitmap != null) 8.dp else 10.dp
                        )
                    ) {
                        // 1. Picture Attachment
                        if (decodedBitmap != null) {
                            Image(
                                bitmap = decodedBitmap,
                                contentDescription = message.attachmentName.ifBlank { "Shared image" },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp, max = 220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onImageClick(decodedBitmap) },
                                contentScale = ContentScale.Crop
                            )
                            if (message.text.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                            }
                        }

                        // 2. File / Document Attachment Card
                        if (message.attachmentType == "FILE") {
                            Surface(
                                color = if (isMe) Color(0xFF2E2E2E) else Cream,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isMe) Color(0xFF444444) else Hairline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onFileClick(message) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Honey),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Description,
                                            contentDescription = "File",
                                            tint = Charcoal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = message.attachmentName.ifBlank { "Attached Document" },
                                            color = if (isMe) CardWhite else Charcoal,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${message.attachmentSize.ifBlank { "File" }} • Tap to save",
                                            color = if (isMe) Color(0xFFBDBDBD) else MutedText,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                            if (message.text.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                            }
                        }

                        // 3. Text or Caption
                        if (message.text.isNotBlank()) {
                            Text(
                                text = message.text,
                                modifier = Modifier.padding(
                                    horizontal = if (decodedBitmap != null) 6.dp else 0.dp,
                                    vertical = if (decodedBitmap != null) 2.dp else 0.dp
                                ),
                                color = if (isMe) CardWhite else Charcoal,
                                fontStyle = if (message.isDeleted) FontStyle.Italic else FontStyle.Normal,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Long-Press Options Context Menu
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Message", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Charcoal) },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onEditMessage(message)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Message", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showMenu = false
                            onDeleteMessage(message)
                        }
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            val timeLabel = buildString {
                append(message.time)
                if (message.isEdited) append(" · edited")
            }

            Text(
                text = timeLabel,
                fontSize = 10.sp,
                color = MutedText
            )
        }
    }
}

/**
 * Reads an image Uri, scales it down so it fits comfortably in a real-time Firestore document,
 * and returns a PendingAttachment with a preview Bitmap and Base64 payload.
 */
private fun processImageUri(context: Context, uri: Uri): PendingAttachment? {
    return try {
        val (fileName, _) = queryFileMeta(context, uri, defaultName = "photo_${System.currentTimeMillis()}.jpg")
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (originalBitmap == null) return null

        val maxDimension = 640
        val ratio = minOf(
            maxDimension.toFloat() / originalBitmap.width.toFloat(),
            maxDimension.toFloat() / originalBitmap.height.toFloat(),
            1.0f
        )
        val targetWidth = (originalBitmap.width * ratio).toInt().coerceAtLeast(1)
        val targetHeight = (originalBitmap.height * ratio).toInt().coerceAtLeast(1)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 65, outputStream)
        val compressedBytes = outputStream.toByteArray()
        val base64Str = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)

        PendingAttachment(
            type = "IMAGE",
            name = fileName,
            sizeLabel = formatFileSize(compressedBytes.size.toLong()),
            base64Data = base64Str,
            previewBitmap = scaledBitmap.asImageBitmap()
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Reads a general document/file Uri and encodes up to 450 KB in Base64 for Firestore transfer
 * (or attaches metadata for larger files).
 */
private fun processFileUri(context: Context, uri: Uri): PendingAttachment? {
    return try {
        val (fileName, rawSize) = queryFileMeta(context, uri, defaultName = "document_${System.currentTimeMillis()}")
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.use { stream ->
            val buffer = ByteArray(450 * 1024)
            val read = stream.read(buffer)
            if (read > 0) buffer.copyOf(read) else ByteArray(0)
        } ?: ByteArray(0)

        val actualSize = if (rawSize > 0L) rawSize else bytes.size.toLong()
        val base64Str = if (bytes.isNotEmpty() && actualSize <= 450 * 1024) {
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } else {
            ""
        }

        PendingAttachment(
            type = "FILE",
            name = fileName,
            sizeLabel = formatFileSize(actualSize),
            base64Data = base64Str,
            previewBitmap = null
        )
    } catch (e: Exception) {
        null
    }
}

private fun queryFileMeta(context: Context, uri: Uri, defaultName: String): Pair<String, Long> {
    var name = defaultName
    var size = 0L
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1 && !cursor.isNull(nameIndex)) {
                    name = cursor.getString(nameIndex)
                }
                if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
    } catch (_: Exception) {
    }
    return name to size
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
        bytes >= 1024 -> "${(bytes / 1024).coerceAtLeast(1)} KB"
        bytes > 0 -> "$bytes B"
        else -> "File"
    }
}

private fun saveAndNotifyFile(context: Context, message: ChatMessage) {
    try {
        val safeName = message.attachmentName.ifBlank { "attachment_${message.id}.bin" }
        if (message.attachmentBase64.isNotBlank()) {
            val bytes = Base64.decode(message.attachmentBase64, Base64.DEFAULT)
            val outFile = File(context.cacheDir, safeName)
            outFile.writeBytes(bytes)
            Toast.makeText(context, "Saved $safeName (${message.attachmentSize})", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Attachment: $safeName (${message.attachmentSize})", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Could not save file: ${message.attachmentName}", Toast.LENGTH_SHORT).show()
    }
}
