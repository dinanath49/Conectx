package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageType {
    TEXT, IMAGE, VOICE, VIDEO
}

enum class MessageStatus {
    SENT, DELIVERED, READ
}

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val text: String = "",
    val mediaUrl: String = "",
    val mediaType: MessageType = MessageType.TEXT,
    val voiceDurationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isSentByMe: Boolean = true,
    val status: MessageStatus = MessageStatus.READ,
    val reaction: String = ""
)
