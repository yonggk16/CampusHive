package com.example.campushive.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Base64
import android.util.LruCache
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import com.example.campushive.ui.theme.bounceClick
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.ReceiveContentListener
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.GifBox
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ReportProblem
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campushive.components.CampusStickerCard
import com.example.campushive.components.CampusStickerPack
import com.example.campushive.components.ReportDialog
import com.example.campushive.components.ServiceCategoryIconBox
import com.example.campushive.components.ChatNotificationHelper
import com.example.campushive.components.ProviderProfileSheet
import com.example.campushive.model.ChatMessage
import com.example.campushive.model.ReportType
import com.example.campushive.ui.theme.*
import com.example.campushive.viewmodel.ChatViewModel
import com.example.campushive.viewmodel.HelpdeskViewModel
import com.example.campushive.viewmodel.ListingViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.graphics.scale

private data class PendingAttachment(
    val type: String, // "IMAGE" | "GIF" | "FILE"
    val name: String,
    val sizeLabel: String,
    val base64Data: String,
    val previewBitmap: ImageBitmap? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    studentName: String,
    serviceTitle: String,
    chatViewModel: ChatViewModel = viewModel(),
    listingViewModel: ListingViewModel = viewModel(),
    helpdeskViewModel: HelpdeskViewModel = viewModel(),
    onBackClick: () -> Unit,
    onViewServiceClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val messageFieldState = rememberTextFieldState()
    val messageText = messageFieldState.text.toString()
    var showAttachMenu by remember { mutableStateOf(false) }
    var showStickerTray by remember { mutableStateOf(false) }
    var pendingAttachment by remember { mutableStateOf<PendingAttachment?>(null) }
    var fullScreenImage by remember { mutableStateOf<ImageBitmap?>(null) }

    // 3-dots header menu and ReportDialog state
    var showMoreMenu by remember { mutableStateOf(false) }
    var showPartnerProfile by remember { mutableStateOf(false) }
    var activeReportType by remember { mutableStateOf<ReportType?>(null) }
    var activeReportTargetId by remember { mutableStateOf("") }
    var activeReportTargetName by remember { mutableStateOf("") }

    // Message being edited or replied to
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var highlightedMessageId by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(highlightedMessageId) {
        if (highlightedMessageId != null) {
            delay(1200L)
            highlightedMessageId = null
        }
    }

    val messagesFlow = remember(chatId) { chatViewModel.getMessagesFlow(chatId) }
    val liveMessages by messagesFlow.collectAsState(initial = emptyList())
    val conversations by chatViewModel.conversations.collectAsState()
    val userOnlineStatus by chatViewModel.userOnlineStatus.collectAsState()
    val userLastSeenMillis by chatViewModel.userLastSeenMillis.collectAsState()
    val listState = rememberLazyListState()

    // Track active open chat & mark conversation read automatically
    DisposableEffect(chatId) {
        chatViewModel.setActiveOpenChat(chatId)
        // Opening the chat makes its heads-up notification stale — dismiss it so it
        // doesn't linger in the shade after the user has already read the message.
        ChatNotificationHelper.clearChatNotification(context, chatId)
        onDispose {
            chatViewModel.setTypingStatus(chatId, false)
            chatViewModel.setActiveOpenChat("")
        }
    }

    // Debounced typing indicator broadcast
    LaunchedEffect(chatId, messageText) {
        if (chatId.isBlank()) return@LaunchedEffect
        if (messageText.isNotBlank()) {
            chatViewModel.setTypingStatus(chatId, true)
            delay(3000L)
            chatViewModel.setTypingStatus(chatId, false)
        } else {
            chatViewModel.setTypingStatus(chatId, false)
        }
    }

    val latestMsgId = liveMessages.lastOrNull()?.id
    val latestMsgMillis = liveMessages.lastOrNull()?.timestampMillis ?: 0L
    LaunchedEffect(chatId, liveMessages.size, latestMsgId, latestMsgMillis) {
        if (chatId.isNotBlank()) {
            chatViewModel.markConversationAsRead(chatId, latestMsgMillis)
        }
    }

    val currentUid = Firebase.auth.currentUser?.uid ?: ""
    val myName = run {
        val user = Firebase.auth.currentUser
        user?.displayName?.trim()?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.trim()
            ?: ""
    }

    val currentConversation = remember(conversations, chatId) {
        conversations.find { it.id == chatId }
    }

    val resolvedListingId = remember(currentConversation, chatId) {
        currentConversation?.listingId?.takeIf { it.isNotBlank() }
            ?: ChatViewModel.extractListingIdFromChatId(chatId)
    }

    val allListings by listingViewModel.listings.collectAsState()

    val matchedListing = remember(allListings, resolvedListingId, serviceTitle) {
        allListings.find { it.id == resolvedListingId }
            ?: allListings.find { it.title.equals(serviceTitle, ignoreCase = true) }
            ?: allListings.find { it.title.contains(serviceTitle, ignoreCase = true) || serviceTitle.contains(it.title, ignoreCase = true) }
    }

    val resolvedCategory = remember(matchedListing, serviceTitle) {
        matchedListing?.category?.takeIf { it.isNotBlank() }
            ?: when {
                serviceTitle.contains("tutor", ignoreCase = true) -> "Tutoring"
                serviceTitle.contains("design", ignoreCase = true) -> "Design"
                serviceTitle.contains("tech", ignoreCase = true) || serviceTitle.contains("laptop", ignoreCase = true) || serviceTitle.contains("code", ignoreCase = true) -> "Tech Help"
                serviceTitle.contains("errand", ignoreCase = true) -> "Errands"
                else -> "Errands"
            }
    }

    // Always resolve the counterpart's name from ChatViewModel so a Provider sees the Seeker's name
    val partnerName = remember(currentConversation, studentName, myName) {
        val convName = currentConversation?.studentName
        when {
            !convName.isNullOrBlank() && !convName.equals(myName, ignoreCase = true) -> convName
            studentName.isNotBlank() && !studentName.equals(myName, ignoreCase = true) -> studentName
            !convName.isNullOrBlank() -> convName
            else -> "Student"
        }
    }

    val partnerUid = remember(currentConversation, currentUid) {
        if (currentConversation == null) {
            ""
        } else if (currentConversation.isProvider) {
            currentConversation.seekerUid
        } else {
            currentConversation.providerUid
        }
    }

    val isPartnerOnline = remember(userOnlineStatus, currentConversation, partnerUid, partnerName, currentUid) {
        val nameKey = partnerName.trim().lowercase()
        when {
            currentConversation != null -> currentConversation.isOnline
            partnerUid.isNotBlank() && partnerUid != currentUid && userOnlineStatus.containsKey(partnerUid) ->
                userOnlineStatus[partnerUid] == true
            nameKey.isNotBlank() && !nameKey.equals(myName.trim(), ignoreCase = true) && userOnlineStatus.containsKey(nameKey) ->
                userOnlineStatus[nameKey] == true
            else -> false
        }
    }

    val partnerLastSeenMillis = remember(userLastSeenMillis, currentConversation, partnerUid, partnerName) {
        val nameKey = partnerName.trim().lowercase()
        when {
            currentConversation != null && currentConversation.lastSeenMillis > 0L ->
                currentConversation.lastSeenMillis
            partnerUid.isNotBlank() && (userLastSeenMillis[partnerUid] ?: 0L) > 0L ->
                userLastSeenMillis[partnerUid] ?: 0L
            nameKey.isNotBlank() && (userLastSeenMillis[nameKey] ?: 0L) > 0L ->
                userLastSeenMillis[nameKey] ?: 0L
            else -> 0L
        }
    }

    var typingExpired by remember(currentConversation?.partnerTypingUpdatedAtMillis, currentConversation?.isPartnerTyping) {
        mutableStateOf(false)
    }
    LaunchedEffect(currentConversation?.isPartnerTyping, currentConversation?.partnerTypingUpdatedAtMillis) {
        if (currentConversation?.isPartnerTyping == true) {
            val remaining = 6_000L - (System.currentTimeMillis() - currentConversation.partnerTypingUpdatedAtMillis)
            if (remaining > 0L) {
                delay(remaining)
            }
            typingExpired = true
        }
    }
    val isPartnerTyping = (currentConversation?.isPartnerTyping == true) && !typingExpired
    val partnerLastReadMillis = currentConversation?.partnerLastReadMillis ?: 0L
    val latestOutgoingMessageId = remember(liveMessages) {
        liveMessages.lastOrNull { it.isFromMe && !it.isDeleted }?.id
    }

    val statusLabel = remember(isPartnerTyping, isPartnerOnline, partnerLastSeenMillis) {
        if (isPartnerTyping) {
            "typing..."
        } else if (isPartnerOnline) {
            "Online now"
        } else if (partnerLastSeenMillis > 0L) {
            val formatted = formatLastSeenTimestamp(partnerLastSeenMillis)
            "Offline • Last seen $formatted"
        } else {
            "Offline"
        }
    }

    // Report Dialog triggered from the 3-dots menu
    // Partner profile preview — opened from the chat header (avatar or name)
    if (showPartnerProfile && partnerUid.isNotBlank()) {
        ProviderProfileSheet(
            uid = partnerUid,
            displayName = partnerName,
            isOnline = isPartnerOnline,
            serviceTitle = serviceTitle,
            onDismiss = { showPartnerProfile = false },
            onListingClick = { listing ->
                showPartnerProfile = false
                onViewServiceClick(listing.id)
            }
        )
    }

    if (activeReportType != null) {
        ReportDialog(
            initialType = activeReportType!!,
            targetId = activeReportTargetId,
            targetName = activeReportTargetName,
            helpdeskViewModel = helpdeskViewModel,
            onDismiss = { activeReportType = null },
            onSubmitSuccess = { activeReportType = null }
        )
    }

    fun sendQuickAttachment(
        type: String,
        name: String,
        sizeLabel: String = "",
        base64Data: String = ""
    ) {
        val currentReply = replyingToMessage
        replyingToMessage = null
        editingMessage = null
        val replySender = currentReply?.let { r ->
            if (r.isFromMe) myName.ifBlank { "You" } else r.senderName.ifBlank { partnerName }
        } ?: ""
        val replyPreview = currentReply?.let { formatReplyPreview(it) } ?: ""
        chatViewModel.sendMessage(
            conversationId = chatId,
            text = "",
            studentName = partnerName,
            serviceTitle = serviceTitle,
            attachmentType = type,
            attachmentName = name,
            attachmentSize = sizeLabel,
            attachmentBase64 = base64Data,
            listingIdHint = resolvedListingId,
            providerUidHint = matchedListing?.providerUid.orEmpty()
                .ifBlank { currentConversation?.providerUid.orEmpty() },
            replyToMessageId = currentReply?.id ?: "",
            replyToSenderName = replySender,
            replyToPreviewText = replyPreview,
            replyToAttachmentType = currentReply?.attachmentType ?: ""
        )
    }

    // Keyboard rich-content listener (Gboard / Samsung Keyboard GIFs & Stickers)
    val receiveContentListener = remember(
        chatId,
        partnerName,
        serviceTitle,
        resolvedListingId,
        matchedListing,
        currentConversation
    ) {
        ReceiveContentListener { transferableContent ->
            if (!transferableContent.hasMediaType(MediaType.Image)) {
                return@ReceiveContentListener transferableContent
            }
            val clipData = transferableContent.clipEntry.clipData
            var handledUri: Uri? = null
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i)?.uri
                if (uri != null) {
                    handledUri = uri
                    break
                }
            }
            if (handledUri != null) {
                val mime = context.contentResolver.getType(handledUri)?.lowercase(Locale.US) ?: ""
                val attachment = if (mime.contains("gif") || mime.contains("webp")) {
                    processGifUri(context, handledUri)
                } else {
                    processImageUri(context, handledUri)
                }
                if (attachment != null) {
                    sendQuickAttachment(
                        type = attachment.type,
                        name = attachment.name,
                        sizeLabel = attachment.sizeLabel,
                        base64Data = attachment.base64Data
                    )
                    null
                } else {
                    Toast.makeText(context, "Could not load selected GIF or sticker", Toast.LENGTH_SHORT).show()
                    transferableContent
                }
            } else {
                transferableContent
            }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        showAttachMenu = false
        if (uri != null) {
            val mime = context.contentResolver.getType(uri)?.lowercase(Locale.US) ?: ""
            val attachment = if (mime.contains("gif")) {
                processGifUri(context, uri)
            } else {
                processImageUri(context, uri)
            }
            if (attachment != null) {
                pendingAttachment = attachment
            } else {
                Toast.makeText(context, "Could not load selected image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // GIF file picker launcher
    val gifPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        showAttachMenu = false
        if (uri != null) {
            val attachment = processGifUri(context, uri)
            if (attachment != null) {
                pendingAttachment = attachment
            } else {
                Toast.makeText(context, "Could not load selected GIF", Toast.LENGTH_SHORT).show()
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

    // Scroll to bottom when new messages arrive or partner starts typing
    LaunchedEffect(liveMessages.size, isPartnerTyping) {
        val targetIndex = liveMessages.size + (if (isPartnerTyping) 1 else 0) - 1
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    // Full-screen image preview dialog when tapping an image in chat
    if (fullScreenImage != null) {
        Dialog(onDismissRequest = { fullScreenImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Radius.Card)
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
                        .clip(Radius.Inner),
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
            .background(HoneyBottomGradient)
            .imePadding()
    ) {
        // Unified Curved White Top Header (matches Home, Search, Chats & Profile silhouette)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular CreamTint Back Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CreamTint)
                            .bounceClick(scaleDown = 0.90f, onClick = onBackClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Charcoal,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    // Avatar + Name tap target (opens profile preview)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(Radius.Inner)
                            .bounceClick(scaleDown = 0.98f) {
                                if (partnerUid.isNotBlank()) showPartnerProfile = true
                            }
                            .padding(end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
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
                                    text = partnerName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal,
                                    fontSize = 15.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(if (isPartnerOnline) OnlineGreen else OfflineGrey)
                                    .border(2.dp, CardWhite, CircleShape)
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = partnerName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Charcoal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = statusLabel,
                                fontSize = 11.sp,
                                color = if (isPartnerTyping || isPartnerOnline) OnlineGreen else MutedText,
                                fontWeight = if (isPartnerTyping) FontWeight.SemiBold else FontWeight.Medium,
                                fontStyle = if (isPartnerTyping) FontStyle.Italic else FontStyle.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Circular CreamTint Overflow Menu Button
                    Box {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CreamTint)
                                .bounceClick(scaleDown = 0.90f) { showMoreMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Report options",
                                tint = Charcoal,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            containerColor = CardWhite,
                            shape = Radius.Inner
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "Report User / Safety Concern",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Charcoal
                                        )
                                        Text(
                                            text = "Harassment, spam, or unsafe behavior",
                                            fontSize = 11.sp,
                                            color = MutedText
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.ReportProblem,
                                        contentDescription = null,
                                        tint = DangerFgStrong,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    activeReportTargetId = partnerUid.ifBlank { chatId }
                                    activeReportTargetName = partnerName
                                    activeReportType = ReportType.USER_SAFETY
                                }
                            )

                            HorizontalDivider(color = Hairline, modifier = Modifier.padding(horizontal = 12.dp))

                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "Flag Service Listing",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Charcoal
                                        )
                                        Text(
                                            text = "Misleading or inappropriate service",
                                            fontSize = 11.sp,
                                            color = MutedText
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Flag,
                                        contentDescription = null,
                                        tint = BrandHoney,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    activeReportTargetId = resolvedListingId.ifBlank { chatId }
                                    activeReportTargetName = serviceTitle
                                    activeReportType = ReportType.LISTING_FLAG
                                }
                            )

                            HorizontalDivider(color = Hairline, modifier = Modifier.padding(horizontal = 12.dp))

                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "Report a Problem",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Charcoal
                                        )
                                        Text(
                                            text = "Technical issue or chat bug",
                                            fontSize = 11.sp,
                                            color = MutedText
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.BugReport,
                                        contentDescription = null,
                                        tint = Charcoal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    activeReportTargetId = chatId
                                    activeReportTargetName = "Chat with $partnerName ($serviceTitle)"
                                    activeReportType = ReportType.PROBLEM
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Compact Cream Service Context Pill inside the Curved Header
                Surface(
                    color = Cream,
                    border = BorderStroke(1.dp, Hairline),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick(scaleDown = 0.98f) {
                            if (resolvedListingId.isNotBlank()) onViewServiceClick(resolvedListingId)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ServiceCategoryIconBox(
                                category = resolvedCategory,
                                size = 34.dp,
                                shape = RoundedCornerShape(10.dp),
                                symbolFontSize = 14.sp,
                                iconSize = 16.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = serviceTitle,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Charcoal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Active Campus Service",
                                    fontSize = 10.sp,
                                    color = MutedText
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        Surface(
                            color = HoneyTint,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                0.8.dp,
                                if (CampusThemeState.isDark) Hairline else WarningBorderSoft
                            )
                        ) {
                            Text(
                                text = "View",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Charcoal
                            )
                        }
                    }
                }
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
                itemsIndexed(liveMessages, key = { _, msg -> msg.id }) { index, msg ->
                    val currentDayKey = messageDayKey(msg.timestampMillis)
                    val prevDayKey = if (index > 0) messageDayKey(liveMessages[index - 1].timestampMillis) else null
                    if (index == 0 || currentDayKey != prevDayKey) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = CreamTint,
                                shape = Radius.Tile
                            ) {
                                Text(
                                    text = formatChatDateHeader(msg.timestampMillis),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MutedText
                                )
                            }
                        }
                    }
                    val isMsgRead = msg.isFromMe &&
                        partnerLastReadMillis > 0L &&
                        msg.timestampMillis > 0L &&
                        msg.timestampMillis <= partnerLastReadMillis
                    MessageBubble(
                        message = msg,
                        currentUid = currentUid,
                        isRead = isMsgRead,
                        showSeenLabel = isMsgRead && msg.id == latestOutgoingMessageId,
                        isHighlighted = msg.id == highlightedMessageId,
                        onImageClick = { bitmap -> fullScreenImage = bitmap },
                        onFileClick = { fileMsg -> saveAndNotifyFile(context, fileMsg) },
                        onToggleReaction = { emoji ->
                            chatViewModel.toggleMessageReaction(chatId, msg.id, emoji)
                        },
                        onReplyMessage = { msgToReply ->
                            editingMessage = null
                            replyingToMessage = msgToReply
                        },
                        onQuotedReplyClick = { targetId ->
                            val targetIdx = liveMessages.indexOfFirst { it.id == targetId }
                            if (targetIdx >= 0) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(targetIdx)
                                    highlightedMessageId = targetId
                                }
                            }
                        },
                        onEditMessage = { msgToEdit ->
                            replyingToMessage = null
                            editingMessage = msgToEdit
                            messageFieldState.setTextAndPlaceCursorAtEnd(msgToEdit.text)
                        },
                        onDeleteMessage = { msgToDelete ->
                            if (replyingToMessage?.id == msgToDelete.id) replyingToMessage = null
                            chatViewModel.deleteMessage(chatId, msgToDelete.id)
                            Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            if (isPartnerTyping) {
                item(key = "typing_indicator") {
                    TypingIndicatorBubble()
                }
            }
        }

        // Unified Curved White Bottom Composer Dock (mirrors the curved top header silhouette)
        Surface(
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = Spacing.gutter, end = Spacing.gutter, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Inline Replying-To Preview Banner Bar
            replyingToMessage?.let { target ->
                val replySender = if (target.isFromMe) "You" else target.senderName.ifBlank { partnerName }
                val replyPreview = formatReplyPreview(target)
                Surface(
                    color = CardWhite,
                    border = BorderStroke(1.dp, Hairline),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.5.dp)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Honey)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Replying to $replySender",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal
                                )
                                Text(
                                    text = replyPreview,
                                    fontSize = 12.sp,
                                    color = MutedText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = { replyingToMessage = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cancel reply",
                                tint = Charcoal,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Inline Editing Message Banner Bar
            editingMessage?.let { editing ->
                Surface(
                    color = HoneyTint,
                    border = BorderStroke(1.dp, if (CampusThemeState.isDark) Hairline else WarningBorderSoft),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CardWhite,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = null,
                                        tint = Charcoal,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Editing Message",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Charcoal
                                )
                                Text(
                                    text = editing.text,
                                    fontSize = 12.sp,
                                    color = MutedText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                editingMessage = null
                                messageFieldState.clearText()
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

            // Expandable Upload Picker Menu (Picture, GIF, File)
            AnimatedVisibility(visible = showAttachMenu) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = HoneyTint,
                        border = BorderStroke(1.dp, Hairline),
                        modifier = Modifier
                            .weight(1f)
                            .bounceClick(scaleDown = 0.94f) { imagePickerLauncher.launch("image/*") }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = "Upload Picture",
                                tint = Charcoal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Picture",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Charcoal
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, Hairline),
                        modifier = Modifier
                            .weight(1f)
                            .bounceClick(scaleDown = 0.94f) { gifPickerLauncher.launch("image/gif") }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GifBox,
                                contentDescription = "Attach GIF",
                                tint = Charcoal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "GIF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Charcoal
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardWhite,
                        border = BorderStroke(1.dp, Hairline),
                        modifier = Modifier
                            .weight(1f)
                            .bounceClick(scaleDown = 0.94f) { filePickerLauncher.launch("*/*") }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = "Attach File",
                                tint = Charcoal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "File",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Charcoal
                            )
                        }
                    }
                }
            }

            // Expandable CampusHive Sticker Tray
            AnimatedVisibility(visible = showStickerTray) {
                Surface(
                    color = CardWhite,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Hairline),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CampusHive Stickers",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal
                            )
                            Text(
                                text = "Keyboard GIFs also supported",
                                fontSize = 10.sp,
                                color = MutedText
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            items(CampusStickerPack.stickers, key = { it.id }) { sticker ->
                                CampusStickerCard(
                                    sticker = sticker,
                                    compact = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bounceClick(scaleDown = 0.92f) {
                                            showStickerTray = false
                                            sendQuickAttachment(
                                                type = "STICKER",
                                                name = sticker.id
                                            )
                                        }
                                )
                            }
                        }
                    }
                }
            }

            // Staged Attachment Preview Bar before sending
            pendingAttachment?.let { staged ->
                Surface(
                    color = CardWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Hairline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if ((staged.type == "IMAGE" || staged.type == "GIF") && staged.previewBitmap != null) {
                            Image(
                                bitmap = staged.previewBitmap,
                                contentDescription = staged.name,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(Radius.Tile),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(Radius.Tile)
                                    .background(HoneyTint)
                                    .border(BorderStroke(1.5.dp, AvatarOutline), Radius.Tile),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = Charcoal,
                                    modifier = Modifier.size(20.dp)
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
                            val typeLabel = when (staged.type) {
                                "IMAGE" -> "Picture"
                                "GIF" -> "GIF"
                                else -> "File"
                            }
                            Text(
                                text = "$typeLabel • ${staged.sizeLabel}",
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

            // Pill Composer Input nestled inside the Curved White Dock
            Surface(
                shape = Radius.Pill,
                color = Cream,
                border = BorderStroke(1.dp, Hairline),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular CreamTint "+" button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (showAttachMenu) Honey else CreamTint)
                            .bounceClick(scaleDown = 0.90f) {
                                showAttachMenu = !showAttachMenu
                                if (showAttachMenu) showStickerTray = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (showAttachMenu) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = "Attach Picture, GIF, or File",
                            tint = if (showAttachMenu) OnHoneyAccent else Charcoal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    // Sleek borderless input inside the floating pill (supports Gboard GIFs & stickers)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (messageText.isEmpty()) {
                            Text(
                                text = when {
                                    editingMessage != null -> "Edit your message..."
                                    pendingAttachment != null -> "Add a caption (optional)..."
                                    else -> "Message ${partnerName.split(" ").firstOrNull() ?: partnerName}..."
                                },
                                fontSize = 14.sp,
                                color = MutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            state = messageFieldState,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = Charcoal
                            ),
                            cursorBrush = SolidColor(Charcoal),
                            lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .contentReceiver(receiveContentListener)
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    // Sticker Tray Toggle Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (showStickerTray) Honey else Color.Transparent)
                            .bounceClick(scaleDown = 0.90f) {
                                showStickerTray = !showStickerTray
                                if (showStickerTray) showAttachMenu = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EmojiEmotions,
                            contentDescription = "CampusHive Stickers",
                            tint = if (showStickerTray) OnHoneyAccent else MutedText,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    // Circular Send Button
                    val canSend = messageText.isNotBlank() || pendingAttachment != null
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (canSend) Honey else CreamTint)
                            .bounceClick(scaleDown = 0.90f) {
                                val currentStaged = pendingAttachment
                                val currentEditing = editingMessage
                                val currentReply = replyingToMessage
                                if (messageText.isNotBlank() || currentStaged != null) {
                                    val textToSend = messageText.trim()
                                    messageFieldState.clearText()
                                    pendingAttachment = null
                                    replyingToMessage = null
                                    showAttachMenu = false
                                    showStickerTray = false

                                    if (currentEditing != null) {
                                        editingMessage = null
                                        chatViewModel.editMessage(chatId, currentEditing.id, textToSend)
                                        Toast.makeText(context, "Message edited", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val replySender = currentReply?.let { r ->
                                            if (r.isFromMe) myName.ifBlank { "You" } else r.senderName.ifBlank { partnerName }
                                        } ?: ""
                                        val replyPreview = currentReply?.let { formatReplyPreview(it) } ?: ""
                                        chatViewModel.sendMessage(
                                            conversationId = chatId,
                                            text = textToSend,
                                            studentName = partnerName,
                                            serviceTitle = serviceTitle,
                                            attachmentType = currentStaged?.type ?: "",
                                            attachmentName = currentStaged?.name ?: "",
                                            attachmentSize = currentStaged?.sizeLabel ?: "",
                                            attachmentBase64 = currentStaged?.base64Data ?: "",
                                            listingIdHint = resolvedListingId,
                                            providerUidHint = matchedListing?.providerUid.orEmpty()
                                                .ifBlank { currentConversation?.providerUid.orEmpty() },
                                            replyToMessageId = currentReply?.id ?: "",
                                            replyToSenderName = replySender,
                                            replyToPreviewText = replyPreview,
                                            replyToAttachmentType = currentReply?.attachmentType ?: ""
                                        )
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (canSend) OnHoneyAccent else MutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun TypingIndicatorBubble() {
    val transition = rememberInfiniteTransition(label = "typing_dots")
    val dotAlphas = (0..2).map { index ->
        transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 550, delayMillis = index * 150),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_$index"
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = CardWhite,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = 4.dp,
                bottomEnd = 18.dp
            ),
            shadowElevation = 0.8.dp,
            border = BorderStroke(1.dp, Hairline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dotAlphas.forEach { alphaState ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MutedText.copy(alpha = alphaState.value))
                    )
                }
            }
        }
    }
}

private fun formatReplyPreview(message: ChatMessage): String {
    return when {
        message.text.isNotBlank() -> message.text.trim()
        message.attachmentType == "IMAGE" -> "Photo"
        message.attachmentType == "GIF" -> "GIF"
        message.attachmentType == "STICKER" -> "Sticker"
        message.attachmentType == "FILE" -> message.attachmentName.ifBlank { "File attachment" }
        else -> "Message"
    }
}

private val QUICK_REACT_EMOJIS = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: ChatMessage,
    currentUid: String = "",
    isRead: Boolean = false,
    showSeenLabel: Boolean = false,
    isHighlighted: Boolean = false,
    onImageClick: (ImageBitmap) -> Unit,
    onFileClick: (ChatMessage) -> Unit,
    onToggleReaction: (String) -> Unit,
    onReplyMessage: (ChatMessage) -> Unit,
    onQuotedReplyClick: (String) -> Unit,
    onEditMessage: (ChatMessage) -> Unit,
    onDeleteMessage: (ChatMessage) -> Unit
) {
    val isMe = message.isFromMe
    var showMenu by remember { mutableStateOf(false) }
    val dragOffsetX = remember(message.id) { Animatable(0f) }
    val dragScope = rememberCoroutineScope()

    val highlightBg by animateColorAsState(
        targetValue = if (isHighlighted) Honey.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "reply_highlight"
    )

    val decodedBitmap: ImageBitmap? by produceState<ImageBitmap?>(
        initialValue = if (!message.isDeleted && message.attachmentType == "IMAGE" && message.attachmentBase64.isNotBlank()) {
            chatImageCache.get(message.id.ifBlank { message.attachmentBase64.hashCode().toString() })
        } else null,
        key1 = message.id,
        key2 = message.attachmentBase64,
        key3 = message.isDeleted
    ) {
        if (message.isDeleted) {
            value = null
            return@produceState
        }
        if (message.attachmentType == "IMAGE" && message.attachmentBase64.isNotBlank() && value == null) {
            val cacheKey = message.id.ifBlank { message.attachmentBase64.hashCode().toString() }
            val decoded = withContext(Dispatchers.Default) {
                try {
                    val bytes = Base64.decode(message.attachmentBase64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                } catch (_: Exception) {
                    null
                }
            }
            if (decoded != null) {
                chatImageCache.put(cacheKey, decoded)
            }
            value = decoded
        }
    }
    val campusSticker = remember(message.attachmentType, message.attachmentName, message.isDeleted) {
        if (!message.isDeleted && message.attachmentType == "STICKER") CampusStickerPack.findById(message.attachmentName) else null
    }
    val isGif = !message.isDeleted && message.attachmentType == "GIF" && message.attachmentBase64.isNotBlank()
    val hasMediaContent = !message.isDeleted && (decodedBitmap != null || isGif || campusSticker != null)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(highlightBg, RoundedCornerShape(16.dp))
            .pointerInput(message.id, message.isDeleted) {
                if (message.isDeleted) return@pointerInput
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffsetX.value >= 80f) {
                            onReplyMessage(message)
                        }
                        dragScope.launch { dragOffsetX.animateTo(0f, tween(180)) }
                    },
                    onDragCancel = {
                        dragScope.launch { dragOffsetX.animateTo(0f, tween(180)) }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        val next = (dragOffsetX.value + dragAmount).coerceIn(0f, 140f)
                        dragScope.launch { dragOffsetX.snapTo(next) }
                    }
                )
            }
    ) {
        if (dragOffsetX.value > 12f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(HoneyTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = "Reply",
                    tint = Charcoal,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(dragOffsetX.value.roundToInt(), 0) },
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
        ) {
            Column(
                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
            ) {
                Box {
                    Surface(
                        color = if (isMe) (if (message.isDeleted) OnDarkStrong else Charcoal) else CardWhite,
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
                                    if (!message.isDeleted) {
                                        showMenu = true
                                    }
                                }
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                horizontal = if (hasMediaContent) 8.dp else 14.dp,
                                vertical = if (hasMediaContent) 8.dp else 10.dp
                            )
                        ) {
                            // 0. Quoted Reply Preview
                            if (message.replyToMessageId.isNotBlank() && !message.isDeleted) {
                                Surface(
                                    color = if (isMe) OnDarkSurface else Cream,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isMe) OnDarkBorder else Hairline),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                        .clickable { onQuotedReplyClick(message.replyToMessageId) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(28.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Honey)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = message.replyToSenderName.ifBlank { "Message" },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isMe) BrandHoney else Charcoal,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = message.replyToPreviewText.ifBlank { "Original message" },
                                                fontSize = 11.sp,
                                                color = if (isMe) OnDarkMuted else MutedText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            // 1. Bundled Sticker Attachment
                        if (!message.isDeleted && campusSticker != null) {
                            CampusStickerCard(
                                sticker = campusSticker,
                                compact = false,
                                size = 136.dp
                            )
                            if (message.text.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                            }
                        }

                        // 1b. Animated GIF Attachment
                        if (isGif) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp, max = 220.dp)
                                    .clip(Radius.Tile)
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = {
                                            if (!message.isDeleted) showMenu = true
                                        }
                                    )
                            ) {
                                AnimatedGifView(
                                    base64Data = message.attachmentBase64,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 120.dp, max = 220.dp)
                                )
                                Surface(
                                    color = Charcoal.copy(alpha = 0.72f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = "GIF",
                                        color = CardWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (message.text.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                            }
                        }

                        // 1c. Picture / Keyboard Sticker Attachment
                        if (!message.isDeleted) {
                            decodedBitmap?.let { bitmap ->
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = message.attachmentName.ifBlank { "Shared image" },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 120.dp, max = 220.dp)
                                        .clip(Radius.Tile)
                                        .combinedClickable(
                                            onClick = { onImageClick(bitmap) },
                                            onLongClick = { showMenu = true }
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                                if (message.text.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                }
                            }
                        }

                        // 2. File / Document Attachment Card
                        if (!message.isDeleted && message.attachmentType == "FILE") {
                            Surface(
                                color = if (isMe) OnDarkSurface else Cream,
                                shape = Radius.Tile,
                                border = BorderStroke(1.dp, if (isMe) OnDarkBorder else Hairline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { onFileClick(message) },
                                        onLongClick = { showMenu = true }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(Radius.Tag)
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
                                            color = if (isMe) OnDarkMuted else MutedText,
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
                                    horizontal = if (hasMediaContent) 6.dp else 0.dp,
                                    vertical = if (hasMediaContent) 2.dp else 0.dp
                                ),
                                color = if (isMe) CardWhite else Charcoal,
                                fontStyle = if (message.isDeleted) FontStyle.Italic else FontStyle.Normal,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Long-Press Message Context Menu (CampusHive Themed)
                MaterialTheme(
                    colorScheme = MaterialTheme.colorScheme.copy(
                        surface = CardWhite,
                        onSurface = Charcoal
                    )
                ) {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = Radius.Inner,
                        border = BorderStroke(1.dp, Hairline),
                        shadowElevation = 6.dp,
                        modifier = Modifier.background(CardWhite)
                    ) {
                        // Quick Emoji Reaction Bar
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QUICK_REACT_EMOJIS.forEach { emoji ->
                                val isSelected = currentUid.isNotBlank() &&
                                    message.reactions[emoji]?.contains(currentUid) == true
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) HoneyTint else Color.Transparent)
                                        .then(
                                            if (isSelected) Modifier.border(1.dp, BrandHoney, CircleShape)
                                            else Modifier
                                        )
                                        .bounceClick(scaleDown = 0.86f) {
                                            showMenu = false
                                            onToggleReaction(emoji)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 18.sp)
                                }
                            }
                        }
                        HorizontalDivider(color = Hairline)
                        DropdownMenuItem(
                            text = { Text("Reply", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Charcoal) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Reply,
                                    contentDescription = "Reply",
                                    tint = Charcoal,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onReplyMessage(message)
                            }
                        )
                        if (isMe) {
                            if (message.text.isNotBlank()) {
                                HorizontalDivider(color = Hairline)
                                DropdownMenuItem(
                                    text = { Text("Edit Message", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Charcoal) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Edit",
                                            tint = Charcoal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        onEditMessage(message)
                                    }
                                )
                            }
                            HorizontalDivider(color = Hairline)
                            DropdownMenuItem(
                                text = { Text("Delete Message", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DangerFgStrong) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete",
                                        tint = DangerFgStrong,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteMessage(message)
                                }
                            )
                        }
                    }
                }
            }

            // Reaction Pills below bubble
            if (message.reactions.isNotEmpty() && !message.isDeleted) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    message.reactions.entries.forEach { (emoji, uids) ->
                        val reactedByMe = currentUid.isNotBlank() && uids.contains(currentUid)
                        Surface(
                            shape = Radius.Pill,
                            color = if (reactedByMe) HoneyTint else CardWhite,
                            border = BorderStroke(1.dp, if (reactedByMe) BrandHoney else Hairline),
                            modifier = Modifier.bounceClick(scaleDown = 0.90f) {
                                onToggleReaction(emoji)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(text = emoji, fontSize = 12.sp)
                                if (uids.size > 1) {
                                    Text(
                                        text = uids.size.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Charcoal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            val timeLabel = buildString {
                append(message.time)
                if (message.isEdited) append(" · edited")
            }

            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = timeLabel,
                    fontSize = 10.sp,
                    color = MutedText
                )
                if (isMe && !message.isDeleted) {
                    Icon(
                        imageVector = if (isRead) Icons.Filled.DoneAll else Icons.Filled.Check,
                        contentDescription = if (isRead) "Read" else "Sent",
                        tint = if (isRead) ReadReceiptBlue else MutedText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        }
    }
}

private val chatImageCache = LruCache<String, ImageBitmap>(32)
private val chatGifBytesCache = LruCache<Int, ByteArray>(16)

/**
 * Renders a Base64-encoded animated GIF or WebP natively using Android's ImageDecoder
 * (API 28+) with off-main-thread decoding and tag-guarded updates to prevent flicker.
 */
@Composable
private fun AnimatedGifView(
    base64Data: String,
    modifier: Modifier = Modifier
) {
    val cacheKey = remember(base64Data) { base64Data.hashCode() }
    val decodedDrawable by produceState<Drawable?>(initialValue = null, key1 = cacheKey) {
        value = withContext(Dispatchers.Default) {
            try {
                val rawBytes = chatGifBytesCache.get(cacheKey) ?: Base64.decode(base64Data, Base64.DEFAULT).also {
                    if (it.isNotEmpty()) chatGifBytesCache.put(cacheKey, it)
                }
                if (rawBytes.isEmpty()) return@withContext null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(ByteBuffer.wrap(rawBytes))
                    ImageDecoder.decodeDrawable(source)
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
    val fallbackBitmap by produceState<Bitmap?>(initialValue = null, key1 = cacheKey) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            value = withContext(Dispatchers.Default) {
                try {
                    val rawBytes = chatGifBytesCache.get(cacheKey) ?: Base64.decode(base64Data, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                adjustViewBounds = true
                isClickable = false
                isLongClickable = false
            }
        },
        update = { imageView ->
            if (imageView.tag == cacheKey && imageView.drawable != null) return@AndroidView
            val drawable = decodedDrawable
            if (drawable != null) {
                imageView.tag = cacheKey
                imageView.setImageDrawable(drawable)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
                    drawable.start()
                }
            } else if (fallbackBitmap != null) {
                imageView.tag = cacheKey
                imageView.setImageBitmap(fallbackBitmap)
            }
        }
    )
}

/**
 * Reads a GIF/animated WebP Uri and preserves raw animated bytes (up to 700 KB) in Base64
 * for Firestore transfer, falling back to a compressed static image if larger.
 */
private fun processGifUri(context: Context, uri: Uri): PendingAttachment? {
    return try {
        val (fileName, _) = queryFileMeta(context, uri, defaultName = "animation_${System.currentTimeMillis()}.gif")
        val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        if (rawBytes.isEmpty()) return null

        val maxGifBytes = 700 * 1024
        if (rawBytes.size > maxGifBytes) {
            Toast.makeText(context, "GIF is over 700 KB — sending as static image", Toast.LENGTH_SHORT).show()
            return processImageUri(context, uri)
        }

        val base64Str = Base64.encodeToString(rawBytes, Base64.NO_WRAP)
        val previewBitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)?.asImageBitmap()
        PendingAttachment(
            type = "GIF",
            name = fileName,
            sizeLabel = formatFileSize(rawBytes.size.toLong()),
            base64Data = base64Str,
            previewBitmap = previewBitmap
        )
    } catch (_: Exception) {
        processImageUri(context, uri)
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
        val scaledBitmap = originalBitmap.scale(targetWidth, targetHeight)

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
    } catch (_: Exception) {
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
    } catch (_: Exception) {
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
    } catch (_: Exception) {
        Toast.makeText(context, "Could not save file: ${message.attachmentName}", Toast.LENGTH_SHORT).show()
    }
}

private fun messageDayKey(timestampMillis: Long): String {
    if (timestampMillis <= 0L) return "EARLIER"
    val cal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
}

private fun formatChatDateHeader(timestampMillis: Long): String {
    if (timestampMillis <= 0L) return "EARLIER"
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val sameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
    val dayDiff = now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR)
    return when {
        sameYear && dayDiff == 0 -> "TODAY"
        sameYear && dayDiff == 1 -> "YESTERDAY"
        sameYear -> SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date(timestampMillis)).uppercase()
        else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestampMillis)).uppercase()
    }
}

private fun formatLastSeenTimestamp(timestampMillis: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val sameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
    val dayDiff = now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR)
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestampMillis))
    return when {
        sameYear && dayDiff == 0 -> timeStr
        sameYear && dayDiff == 1 -> "Yesterday $timeStr"
        sameYear -> SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(timestampMillis))
        else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestampMillis))
    }
}
