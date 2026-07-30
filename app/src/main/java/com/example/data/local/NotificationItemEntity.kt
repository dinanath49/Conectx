package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "MESSAGE",
    val isRead: Boolean = false,
    val targetConversationId: String? = null
)
