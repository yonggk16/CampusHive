package com.example.campushive.model

data class ChatConversation(
    val id: String,
    val studentName: String,
    val serviceTitle: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isVerifiedStudent: Boolean = true,
    val listingId: String = "",
    val seekerUid: String = "",
    val providerUID: String = "",
    val providerName: String = "",
    val seekerName: String = "",
    val isProvider: Boolean = false
)
