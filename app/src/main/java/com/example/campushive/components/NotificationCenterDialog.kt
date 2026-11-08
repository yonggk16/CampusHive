package com.example.campushive.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.campushive.model.ChatConversation
import com.example.campushive.ui.theme.*

/**
 * In-app notification centre, shown from the Home bell.
 *
 * Redesigned for density. The previous version stacked three loose text rows per
 * item inside a 520dp dialog, which pushed the card to near full-screen and read
 * as a wall of text. Changes here:
 *
 * - **Capped at 400dp.** A notification peek should not take over the screen; the
 *   list scrolls inside instead of the dialog growing.
 * - **Tighter row rhythm** (10dp padding, 36dp avatar, 2dp gaps) so roughly one
 *   more item is visible in the same space.
 * - **No black border on unread rows.** Unread is now signalled by a tinted
 *   surface plus a slim left accent bar — the old 1dp Charcoal border around the
 *   whole card was the heaviest thing on screen.
 * - **Compact header.** Title + unread subtitle on one block, actions right
 *   aligned, so the header costs ~48dp instead of ~70dp.
 * - **Slim empty state** instead of a 36dp-padded centred block.
 */
@Composable
fun NotificationCenterDialog(
    conversations: List<ChatConversation>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onOpenChat: (ChatConversation) -> Unit,
) {
    val unreadTotal = remember(conversations) { conversations.sumOf { it.unreadCount } }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = Radius.Card,
            color = CardWhite,
            border = BorderStroke(1.dp, Hairline),
            shadowElevation = 18.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            Column {
                DialogHeader(
                    unreadTotal = unreadTotal,
                    showMarkAllRead = unreadTotal > 0,
                    onMarkAllRead = onMarkAllRead,
                    onDismiss = onDismiss
                )

                HorizontalDivider(color = Hairline, thickness = 0.5.dp)

                if (conversations.isEmpty()) {
                    EmptyNotifications()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = Spacing.md, end = Spacing.md,
                            top = Spacing.sm, bottom = Spacing.md
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        items(conversations, key = { it.id }) { chat ->
                            NotificationRow(chat) { onOpenChat(chat) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(
    unreadTotal: Int,
    showMarkAllRead: Boolean,
    onMarkAllRead: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 10.dp, top = 14.dp, bottom = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Notifications",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                letterSpacing = (-0.3).sp
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = if (unreadTotal > 0) {
                    "$unreadTotal unread"
                } else {
                    "You're all caught up"
                },
                fontSize = 12.sp,
                color = MutedText
            )
        }

        if (showMarkAllRead) {
            Surface(
                shape = Radius.Pill,
                color = HoneyTint,
                modifier = Modifier.bounceClick(scaleDown = 0.94f, onClick = onMarkAllRead)
            ) {
                Text(
                    text = "Mark read",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(CardWhite)
                .border(1.dp, Hairline, CircleShape)
                .bounceClick(scaleDown = 0.92f, onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close notifications",
                tint = Charcoal,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun NotificationRow(chat: ChatConversation, onClick: () -> Unit) {
    val isUnread = chat.unreadCount > 0

    Surface(
        shape = Radius.Inner,
        color = if (isUnread) HoneyTint.copy(alpha = 0.5f) else CardWhite,
        border = if (isUnread) null else BorderStroke(1.dp, Hairline),
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.98f, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Slim unread accent — replaces the old full black border.
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(26.dp)
                    .clip(Radius.Pill)
                    .background(if (isUnread) Charcoal else Color.Transparent)
            )

            Spacer(Modifier.width(9.dp))

            AvatarTile(
                name = chat.studentName,
                size = 36.dp,
                fontSize = 12.sp
            )

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chat.studentName.ifBlank { "Campus Student" },
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 13.sp,
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
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chat.timestamp,
                        fontSize = 10.sp,
                        color = MutedText,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(1.dp))

                Text(
                    text = chat.serviceTitle.ifBlank { "Campus Service" },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = chat.lastMessage.ifBlank { "Sent you a message" },
                    fontSize = 12.sp,
                    fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isUnread) Charcoal else Charcoal.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isUnread) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
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
        }
    }
}

@Composable
private fun EmptyNotifications() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(HoneyTint)
                .border(1.5.dp, AvatarOutline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = Charcoal,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Nothing new yet",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = "Message replies from seekers and providers land here.",
            fontSize = 12.sp,
            color = MutedText,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}
