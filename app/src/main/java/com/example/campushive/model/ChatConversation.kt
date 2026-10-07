package com.example.campushive.model

data class ChatConversation(
    val id: String,
    val studentName: String,
    val serviceTitle: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isVerifiedStudent: Boolean = true
)

val sampleConversations = listOf(
    ChatConversation(
        id = "chat_1",
        studentName = "Ming Yuan",
        serviceTitle = "Calculus I Tutoring",
        lastMessage = "Sounds good! Let's meet at the 2nd floor library at 4 PM.",
        timestamp = "10m ago",
        unreadCount = 2
    ),
    ChatConversation(
        id = "chat_2",
        studentName = "Shi Qi",
        serviceTitle = "Poster Design for Club",
        lastMessage = "I attached the color palette for the flyer!",
        timestamp = "1h ago",
        unreadCount = 1
    ),
    ChatConversation(
        id = "chat_3",
        studentName = "Jacob Chin",
        serviceTitle = "Java Code Debugging",
        lastMessage = "Thanks for fixing the null pointer bug! Rating 5 stars.",
        timestamp = "Yesterday",
        unreadCount = 0
    )
)