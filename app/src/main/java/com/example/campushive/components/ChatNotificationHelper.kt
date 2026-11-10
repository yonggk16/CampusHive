package com.example.campushive.components

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.campushive.MainActivity
import com.example.campushive.R
import com.example.campushive.model.ChatConversation
import com.example.campushive.ui.notifications.BrandAccentArgb
import com.example.campushive.ui.notifications.NotificationColors

/**
 * Heads-up chat notification ("island" popup) for CampusHive.
 *
 * Design notes — this replaces the previous generic two-line notification:
 *
 * - **MessagingStyle** instead of BigTextStyle. On Android 11+ this is the style the
 *   system renders as a conversation-aware heads-up card: sender name on top, message
 *   below, and replies collapse into the same bubble when several arrive quickly.
 *   BigTextStyle looked like a system alert, not a chat.
 * - **Branded status-bar icon.** Was `android.R.drawable.sym_action_chat`, a platform
 *   asset, so Android 12+ tinted a generic blob. Now `ic_notification_hive`.
 * - **Accent colour** set via setColor so the small icon and app-name row carry the
 *   CampusHive charcoal instead of default grey.
 * - **Sender monogram avatar** generated at runtime — no network fetch, no extra
 *   permission, and no Bitmap decoding on the notification service thread.
 * - **No setShortcutId.** That API only puts the notification in the Shade's
 *   Conversation section if a *published long-lived dynamic shortcut* with the same
 *   id exists, and this app never creates ShortcutManagerCompat shortcuts. Setting it
 *   without one is a no-op at best; we omit it rather than imply Conversation features
 *   the app doesn't actually support.
 * - **One notification per conversation** (id = conversation id hash) with
 *   setOnlyAlertOnce(false) so a repeat message re-pops but does not stack duplicates
 *   for the same chat.
 * - Channel is registered with a stable id. Note: Android caches channel settings
 *   after first creation, so name/importance changes require a reinstall or a new id.
 */
object ChatNotificationHelper {

    const val CHANNEL_ID = "campushive_chat_messages_v2"
    private const val CHANNEL_NAME = "Chat Messages"
    private const val CHANNEL_DESC = "Real-time alerts for incoming CampusHive messages"

    private const val KEY_CHAT_ID = "open_chat_id"
    private const val KEY_CHAT_STUDENT = "open_chat_student"
    private const val KEY_CHAT_SERVICE = "open_chat_service"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESC
            enableVibration(true)
            // A short, subtle buzz that matches the spring feel of in-app interactions.
            vibrationPattern = longArrayOf(0, 40, 60, 40)
            enableLights(false)
            setShowBadge(true)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.createNotificationChannel(channel)
    }

    fun showChatNotification(context: Context, conversation: ChatConversation) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val senderLabel = conversation.studentName.ifBlank { "Campus Student" }
        val serviceLabel = conversation.serviceTitle.ifBlank { "Campus Service" }
        val messageBody = conversation.lastMessage.ifBlank { "Sent you a new message" }

        val pendingIntent = chatPendingIntent(context, conversation)

        val person = androidx.core.app.Person.Builder()
            .setName(senderLabel)
            .setIcon(
                androidx.core.graphics.drawable.IconCompat.createWithBitmap(
                    monogramBitmap(senderLabel)
                )
            )
            .build()

        val messagingStyle = NotificationCompat.MessagingStyle(
            androidx.core.app.Person.Builder().setName("You").build()
        )
            .setConversationTitle(serviceLabel)
            .addMessage(messageBody, conversation.lastMessageMillis, person)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_hive)
            .setColor(BrandAccentArgb)
            .setStyle(messagingStyle)
            .setSubText(serviceLabel)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setWhen(
                if (conversation.lastMessageMillis > 0L) conversation.lastMessageMillis
                else System.currentTimeMillis()
            )
            .setShowWhen(true)
            .setContentIntent(pendingIntent)
            .setGroup("campushive_chat")

        if (conversation.unreadCount > 1) {
            // Show the conversation depth without inflating the body text.
            builder.setNumber(conversation.unreadCount)
        }

        try {
            NotificationManagerCompat.from(context)
                .notify(conversation.id.hashCode(), builder.build())
        } catch (_: SecurityException) {
            // Permission revoked between the check above and notify().
        }
    }

    /** Clears a conversation's notification once the user is actually reading it. */
    fun clearChatNotification(context: Context, conversationId: String) {
        if (conversationId.isBlank()) return
        runCatching {
            NotificationManagerCompat.from(context).cancel(conversationId.hashCode())
        }
    }

    private fun chatPendingIntent(context: Context, conversation: ChatConversation): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(KEY_CHAT_ID, conversation.id)
            putExtra(KEY_CHAT_STUDENT, conversation.studentName)
            putExtra(KEY_CHAT_SERVICE, conversation.serviceTitle)
        }
        return PendingIntent.getActivity(
            context,
            conversation.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Draws the sender's initials into a soft circle for the messaging avatar.
     * Deliberately pure Canvas: no image library, no IO, safe on any thread.
     */
    private fun monogramBitmap(name: String, sizePx: Int = 192): Bitmap {
        val initials = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { "S" }

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NotificationColors.AvatarBackgroundArgb
        }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, bg)

        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NotificationColors.AvatarOutlineArgb
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.0625f
        }
        val inset = ring.strokeWidth / 2f
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - inset, ring)

        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NotificationColors.AvatarTextArgb
            textSize = sizePx * 0.38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val bounds = Rect()
        text.getTextBounds(initials, 0, initials.length, bounds)
        val y = sizePx / 2f - bounds.exactCenterY()
        canvas.drawText(initials, sizePx / 2f, y, text)

        return bitmap
    }
}
