package com.example.campushive.model

data class ChatMessage(
    val id: String,
    val text: String,
    val time: String,
    val isFromMe: Boolean,
    val senderRole: String = ""
)




