package com.example.campushive.model

data class ChatMessage(
    val id: String,
    val text: String,
    val time: String,
    val isFromMe: Boolean,
    val senderRole: String = ""
)

val sampleMessagesMap = mapOf(
    "chat_1" to listOf(
        ChatMessage("1", "Hi Ming Yuan! Are you available for Calculus I tutoring this week?", "4:00 PM", isFromMe = true),
        ChatMessage("2", "Hey! Yes I am. I have availability tomorrow or Thursday afternoon.", "4:05 PM", isFromMe = false),
        ChatMessage("3", "Thursday around 4 PM works great for me. Does the library work?", "4:10 PM", isFromMe = true),
        ChatMessage("4", "Sounds good! Let's meet at the 2nd floor library at 4 PM.", "4:15 PM", isFromMe = false)
    ),
    "chat_2" to listOf(
        ChatMessage("1", "Hi Shi Qi, can you design an event flyer for our CS Club?", "2:00 PM", isFromMe = true),
        ChatMessage("2", "Sure! What is your event topic and date?", "2:15 PM", isFromMe = false),
        ChatMessage("3", "I attached the color palette for the flyer!", "2:30 PM", isFromMe = false)
    )
)


