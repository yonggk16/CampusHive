package com.example.campushive.model

data class ChatMessage(
    val id: String,
    val text: String,
    val time: String,
    val isFromMe: Boolean,
    val senderRole: String = "",
    val attachmentType: String = "", // "IMAGE" | "FILE" | ""
    val attachmentName: String = "",
    val attachmentSize: String = "",
    val attachmentBase64: String = "",
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val timestampMillis: Long = 0L,
    val senderName: String = "",
    val replyToMessageId: String = "",
    val replyToSenderName: String = "",
    val replyToPreviewText: String = "",
    val replyToAttachmentType: String = "",
    val reactions: Map<String, List<String>> = emptyMap()
)
