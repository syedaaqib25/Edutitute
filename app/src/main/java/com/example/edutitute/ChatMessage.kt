package com.example.edutitute

data class ChatMessage(
    val senderId: String = "",
    val senderName: String = "",
    val messageText: String = "",
    val timestamp: Long = 0L
)
