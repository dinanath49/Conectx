package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ConnectXRepository(context: Context) {
    private val database = ConnectXDatabase.getDatabase(context)
    private val dao = database.dao()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Current Auth State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser

    // App Settings State
    private val prefs = context.getSharedPreferences("connectx_prefs", Context.MODE_PRIVATE)

    val isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val readReceiptsEnabled = MutableStateFlow(prefs.getBoolean("read_receipts", true))
    val autoPlayVoice = MutableStateFlow(prefs.getBoolean("auto_play_voice", true))

    init {
        scope.launch {
            // Seed initial data if empty
            val existingUser = dao.getUserByIdSync("current_user")
            if (existingUser == null) {
                val initialUser = UserEntity(
                    id = "current_user",
                    username = "Alex Morgan",
                    email = "alex.morgan@connectx.io",
                    bio = "Creating the future with ConnectX 🚀 | Tech Explorer",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                    isOnline = true
                )
                dao.insertUser(initialUser)
                _currentUser.value = initialUser
                seedInitialData()
            } else {
                _currentUser.value = existingUser
            }
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun updateReadReceipts(enabled: Boolean) {
        readReceiptsEnabled.value = enabled
        prefs.edit().putBoolean("read_receipts", enabled).apply()
    }

    // Auth actions
    suspend fun login(email: String, name: String): Boolean {
        val user = UserEntity(
            id = "current_user",
            username = name.ifBlank { "ConnectX Member" },
            email = email,
            bio = "Available on ConnectX",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
            isOnline = true
        )
        dao.insertUser(user)
        _currentUser.value = user
        return true
    }

    suspend fun signup(username: String, email: String, bio: String): Boolean {
        val user = UserEntity(
            id = "current_user",
            username = username,
            email = email,
            bio = bio.ifBlank { "Hey there! I am using ConnectX." },
            avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=400",
            isOnline = true
        )
        dao.insertUser(user)
        _currentUser.value = user
        return true
    }

    suspend fun updateProfile(username: String, bio: String, avatarUrl: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            username = username,
            bio = bio,
            avatarUrl = avatarUrl
        )
        dao.insertUser(updated)
        _currentUser.value = updated
    }

    // Conversations & Messages
    fun getAllConversations(): Flow<List<ConversationEntity>> = dao.getAllConversations()

    fun getConversationById(id: String): Flow<ConversationEntity?> = dao.getConversationById(id)

    fun getMessagesForConversation(id: String): Flow<List<MessageEntity>> = dao.getMessagesForConversation(id)

    suspend fun markConversationRead(id: String) {
        dao.markConversationRead(id)
    }

    suspend fun sendMessage(
        conversationId: String,
        text: String = "",
        mediaUrl: String = "",
        mediaType: MessageType = MessageType.TEXT,
        voiceDurationSeconds: Int = 0
    ) {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = "current_user",
            senderName = _currentUser.value?.username ?: "Me",
            text = text,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            voiceDurationSeconds = voiceDurationSeconds,
            timestamp = System.currentTimeMillis(),
            isSentByMe = true,
            status = MessageStatus.SENT
        )

        dao.insertMessage(message)

        val previewText = when (mediaType) {
            MessageType.TEXT -> text
            MessageType.IMAGE -> "📷 Photo message"
            MessageType.VOICE -> "🎙️ Voice message (${voiceDurationSeconds}s)"
            MessageType.VIDEO -> "📹 Video clip"
        }

        dao.updateLastMessage(conversationId, previewText, message.timestamp, 0)

        // Simulate reply after 1.5 seconds
        simulateAutoReply(conversationId, text, mediaType)
    }

    suspend fun setMessageReaction(messageId: String, reaction: String) {
        dao.updateMessageReaction(messageId, reaction)
    }

    private fun simulateAutoReply(conversationId: String, userText: String, mediaType: MessageType) {
        scope.launch {
            delay(1800)
            val replies = listOf(
                "Awesome! Thanks for sending that over.",
                "Sounds great! Let's jump on a ConnectX Video Call soon.",
                "Got it! I will review this right away.",
                "Loved the update! ConnectX speed is impressive.",
                "Super cool! Let's keep in touch."
            )
            val replyText = replies.random()

            val replyMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                senderId = "contact_$conversationId",
                senderName = "Contact",
                text = replyText,
                mediaType = MessageType.TEXT,
                timestamp = System.currentTimeMillis(),
                isSentByMe = false,
                status = MessageStatus.READ
            )

            dao.insertMessage(replyMsg)
            dao.updateLastMessage(conversationId, replyText, replyMsg.timestamp, 0)

            // Trigger notification
            val notif = NotificationItemEntity(
                id = UUID.randomUUID().toString(),
                title = "New message",
                body = replyText,
                type = "MESSAGE",
                targetConversationId = conversationId
            )
            dao.insertNotification(notif)
        }
    }

    // Call Logs
    fun getAllCallLogs(): Flow<List<CallLogEntity>> = dao.getAllCallLogs()

    suspend fun addCallLog(
        participantId: String,
        participantName: String,
        participantAvatar: String,
        isVideoCall: Boolean,
        isOutgoing: Boolean,
        durationSeconds: Int
    ) {
        val log = CallLogEntity(
            id = UUID.randomUUID().toString(),
            participantId = participantId,
            participantName = participantName,
            participantAvatar = participantAvatar,
            isVideoCall = isVideoCall,
            isOutgoing = isOutgoing,
            isMissed = durationSeconds == 0 && !isOutgoing,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds
        )
        dao.insertCallLog(log)
    }

    // Notifications
    fun getAllNotifications(): Flow<List<NotificationItemEntity>> = dao.getAllNotifications()
    fun getUnreadNotificationCount(): Flow<Int> = dao.getUnreadNotificationCount()

    suspend fun markAllNotificationsRead() {
        dao.markAllNotificationsAsRead()
    }

    // Initial Data Seeding
    private suspend fun seedInitialData() {
        val now = System.currentTimeMillis()

        val c1 = ConversationEntity(
            id = "conv_sarah",
            participantId = "user_sarah",
            participantName = "Sarah Connor",
            participantAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
            participantStatus = "Online",
            lastMessageText = "Let's test the ConnectX voice call feature now!",
            lastMessageTimestamp = now - 1000 * 60 * 5,
            unreadCount = 2,
            isPinned = true
        )

        val c2 = ConversationEntity(
            id = "conv_alex",
            participantId = "user_alex",
            participantName = "Alex Rivera",
            participantAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
            participantStatus = "In a meeting",
            lastMessageText = "📷 Photo message",
            lastMessageTimestamp = now - 1000 * 60 * 35,
            unreadCount = 0
        )

        val c3 = ConversationEntity(
            id = "conv_elena",
            participantId = "user_elena",
            participantName = "Elena Rostova",
            participantAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400",
            participantStatus = "Online",
            lastMessageText = "🎙️ Voice message (14s)",
            lastMessageTimestamp = now - 1000 * 60 * 120,
            unreadCount = 1
        )

        val c4 = ConversationEntity(
            id = "conv_tech_group",
            participantId = "group_tech",
            participantName = "Tech Innovators 🚀",
            participantAvatar = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=400",
            participantStatus = "5 members",
            lastMessageText = "📹 Check out our ConnectX product demo video!",
            lastMessageTimestamp = now - 1000 * 60 * 300,
            unreadCount = 0,
            isGroup = true
        )

        dao.insertConversation(c1)
        dao.insertConversation(c2)
        dao.insertConversation(c3)
        dao.insertConversation(c4)

        // Seed messages for Sarah
        dao.insertMessage(
            MessageEntity(
                id = "m1",
                conversationId = "conv_sarah",
                senderId = "user_sarah",
                senderName = "Sarah Connor",
                text = "Hey Alex! Welcome to ConnectX. How are you liking the Material 3 UI design?",
                timestamp = now - 1000 * 60 * 15,
                isSentByMe = false
            )
        )
        dao.insertMessage(
            MessageEntity(
                id = "m2",
                conversationId = "conv_sarah",
                senderId = "current_user",
                senderName = "Me",
                text = "It looks incredible! Smooth animations, real-time audio waveforms, video calls, and instant media sharing.",
                timestamp = now - 1000 * 60 * 10,
                isSentByMe = true
            )
        )
        dao.insertMessage(
            MessageEntity(
                id = "m3",
                conversationId = "conv_sarah",
                senderId = "user_sarah",
                senderName = "Sarah Connor",
                text = "Let's test the ConnectX voice call feature now!",
                timestamp = now - 1000 * 60 * 5,
                isSentByMe = false
            )
        )

        // Seed messages for Alex (with Image)
        dao.insertMessage(
            MessageEntity(
                id = "m4",
                conversationId = "conv_alex",
                senderId = "user_alex",
                senderName = "Alex Rivera",
                text = "Hey check out this futuristic workspace preview!",
                timestamp = now - 1000 * 60 * 40,
                isSentByMe = false
            )
        )
        dao.insertMessage(
            MessageEntity(
                id = "m5",
                conversationId = "conv_alex",
                senderId = "user_alex",
                senderName = "Alex Rivera",
                text = "High resolution design concept",
                mediaUrl = "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=800",
                mediaType = MessageType.IMAGE,
                timestamp = now - 1000 * 60 * 35,
                isSentByMe = false
            )
        )

        // Seed messages for Elena (Voice message)
        dao.insertMessage(
            MessageEntity(
                id = "m6",
                conversationId = "conv_elena",
                senderId = "user_elena",
                senderName = "Elena Rostova",
                text = "Voice note update",
                mediaType = MessageType.VOICE,
                voiceDurationSeconds = 14,
                timestamp = now - 1000 * 60 * 120,
                isSentByMe = false
            )
        )

        // Seed messages for Tech Group (Video sharing)
        dao.insertMessage(
            MessageEntity(
                id = "m7",
                conversationId = "conv_tech_group",
                senderId = "user_david",
                senderName = "David Miller",
                text = "Check out our ConnectX product demo video!",
                mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                mediaType = MessageType.VIDEO,
                timestamp = now - 1000 * 60 * 300,
                isSentByMe = false
            )
        )

        // Seed Call Logs
        dao.insertCallLog(
            CallLogEntity(
                id = "call_1",
                participantId = "user_sarah",
                participantName = "Sarah Connor",
                participantAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
                isVideoCall = true,
                isOutgoing = true,
                durationSeconds = 245,
                timestamp = now - 1000 * 60 * 60 * 2
            )
        )
        dao.insertCallLog(
            CallLogEntity(
                id = "call_2",
                participantId = "user_alex",
                participantName = "Alex Rivera",
                participantAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
                isVideoCall = false,
                isOutgoing = false,
                isMissed = true,
                durationSeconds = 0,
                timestamp = now - 1000 * 60 * 60 * 18
            )
        )

        // Seed Notifications
        dao.insertNotification(
            NotificationItemEntity(
                id = "notif_1",
                title = "Incoming Video Call",
                body = "Sarah Connor called you on ConnectX",
                type = "CALL",
                isRead = false
            )
        )
        dao.insertNotification(
            NotificationItemEntity(
                id = "notif_2",
                title = "New Voice Message",
                body = "Elena Rostova sent a 14s voice message",
                type = "MESSAGE",
                isRead = false,
                targetConversationId = "conv_elena"
            )
        )
    }
}
