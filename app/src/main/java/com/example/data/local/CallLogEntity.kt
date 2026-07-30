package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val id: String,
    val participantId: String,
    val participantName: String,
    val participantAvatar: String = "",
    val isVideoCall: Boolean = false,
    val isOutgoing: Boolean = true,
    val isMissed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)
