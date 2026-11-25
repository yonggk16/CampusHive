package com.example.campushive.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.campushive.model.ChatConversation
import com.example.campushive.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * In-app notification centre, shown from the Home bell.
 *
 * Drops down smoothly from the top edge of the screen with swipe-up-to-dismiss,
 * keeping CampusHive's unified form-sheet styling: 28dp rounded bottom corners,
 * 42dp HoneyTint bell icon header, Cream-toned cards, anchored action bar, and
 * a bottom drag handle.
 */
@Composable
fun NotificationCenterDialog(
    conversations: List<ChatConversation>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit,
    onClearOne: (ChatConversation) -> Unit,
    onOpenChat: (ChatConversation) -> Unit,
) {
    val unreadTotal = remember(conversations) { conversations.sumOf { it.unreadCount } }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 0f = hidden above top edge, 1f = fully dropped down
    val revealProgress = remember { Animatable(0f) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var sheetHeightPx by remember { mutableIntStateOf(0) }
    var isDismissing by remember { mutableStateOf(false) }

    val dismissThresholdPx = with(density) { 72.dp.toPx() }

    LaunchedEffect(Unit) {
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    fun dismissAnimated() {
        if (isDismissing) return
        isDismissing = true
        scope.launch {
            revealProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 200)
            )
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { dismissAnimated() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Dimmed scrim behind the top sheet
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f * revealProgress.value.coerceIn(0f, 1f)))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { dismissAnimated() }
                    )
            )

            // Top drop-down sheet
            Surface(
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                color = CardWhite,
                contentColor = Charcoal,
                border = BorderStroke(1.dp, Hairline),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .onSizeChanged { sheetHeightPx = it.height }
                    .graphicsLayer {
                        val height = if (sheetHeightPx > 0) sheetHeightPx.toFloat() else 1200f
                        translationY = -height * (1f - revealProgress.value)
                    }
                    .offset { IntOffset(0, dragOffsetPx.roundToInt()) }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffsetPx < -dismissThresholdPx) {
                                    dismissAnimated()
                                } else {
                                    dragOffsetPx = 0f
                                }
                            },
                            onDragCancel = { dragOffsetPx = 0f },
                            onVerticalDrag = { _, dragAmount ->
                                // Allow dragging upward to dismiss (resist downward pull)
                                dragOffsetPx = (dragOffsetPx + dragAmount).coerceAtMost(0f)
                            }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 14.dp)
                ) {
                    // Header — matches form sheet header
                    NotificationSheetHeader(
                        unreadTotal = unreadTotal,
                        onDismiss = { dismissAnimated() }
                    )

                    Spacer(Modifier.height(16.dp))

                    // Body — scrollable list or Cream-toned empty card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        if (conversations.isEmpty()) {
                            CampusEmptyState(
                                icon = Icons.Outlined.Notifications,
                                title = "Nothing new yet",
                                subtitle = "Message replies from seekers and providers will appear here.",
                                compact = true,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 380.dp),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(conversations, key = { it.id }) { chat ->
                                    NotificationRow(
                                        chat = chat,
                                        onClear = { onClearOne(chat) },
                                        onClick = { onOpenChat(chat) }
                                    )
                                }
                            }
                        }
                    }

                    // Anchored action bar (shown only when there are notifications to act on)
                    if (conversations.isNotEmpty()) {
                        HorizontalDivider(thickness = 0.8.dp, color = Hairline)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 10.dp)
                        ) {
                            if (unreadTotal > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = onClearAll,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CreamTint,
                                            contentColor = Charcoal
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Clear all",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Button(
                                        onClick = onMarkAllRead,
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(50.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Honey,
                                            contentColor = OnHoneyAccent
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = "Mark all read",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = onClearAll,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CreamTint,
                                        contentColor = Charcoal
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Clear all",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // Bottom swipe-up handle pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Hairline)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationSheetHeader(
    unreadTotal: Int,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(HoneyTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = if (CampusThemeState.isDark) BrandHoney else Charcoal,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Notifications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = Charcoal
                )
                Text(
                    text = if (unreadTotal > 0) {
                        "$unreadTotal unread ${if (unreadTotal == 1) "message" else "messages"}"
                    } else {
                        "You're all caught up"
                    },
                    fontSize = 13.sp,
                    color = MutedText
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(CreamTint)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close notifications",
                tint = Charcoal,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun NotificationRow(
    chat: ChatConversation,
    onClear: () -> Unit,
    onClick: () -> Unit
) {
    val isUnread = chat.unreadCount > 0
    val previewText = remember(chat.lastMessage) {
        val raw = chat.lastMessage.trim()
        when {
            raw.isEmpty() -> "Sent you a message"
            raw.equals("[Photo]", ignoreCase = true) -> "Photo attachment"
            raw.equals("[GIF]", ignoreCase = true) -> "GIF"
            raw.equals("[Sticker]", ignoreCase = true) -> "Sticker"
            else -> raw
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isUnread) HoneyTint.copy(alpha = 0.55f) else Cream,
        border = BorderStroke(1.dp, if (isUnread) WarningBorderSoft else Hairline),
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.98f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(30.dp)
                        .clip(CircleShape)
                        .background(Charcoal)
                )
                Spacer(Modifier.width(10.dp))
            }

            AvatarTile(
                name = chat.studentName,
                size = 40.dp,
                fontSize = 13.sp
            )

            Spacer(Modifier.width(12.dp))

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
                            fontSize = 14.sp,
                            color = Charcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (chat.isVerifiedStudent) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified student",
                                tint = Charcoal,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = chat.timestamp,
                        fontSize = 11.sp,
                        color = MutedText,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = chat.serviceTitle.ifBlank { "Campus Service" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    text = previewText,
                    fontSize = 13.sp,
                    fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isUnread) Charcoal else Charcoal.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isUnread) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(DangerFgStrong),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (chat.unreadCount > 9) "9+" else chat.unreadCount.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CardWhite
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CreamTint)
                    .border(1.dp, Hairline, CircleShape)
                    .clickable(onClick = onClear),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clear notification",
                    tint = Charcoal,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

