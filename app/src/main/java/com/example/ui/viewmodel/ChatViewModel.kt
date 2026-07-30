package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.MessageType
import com.example.data.repository.ConnectXRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ConnectXRepository(application)

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId

    val conversation: StateFlow<ConversationEntity?> = _currentConversationId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repository.getConversationById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val messages: StateFlow<List<MessageEntity>> = _currentConversationId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getMessagesForConversation(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Voice recording state
    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice

    private val _recordingDurationSeconds = MutableStateFlow(0)
    val recordingDurationSeconds: StateFlow<Int> = _recordingDurationSeconds

    private var recordingJob: Job? = null

    // Image & Video sharing sheet state
    private val _selectedImageUri = MutableStateFlow<String?>(null)
    val selectedImageUri: StateFlow<String?> = _selectedImageUri

    private val _selectedVideoUri = MutableStateFlow<String?>(null)
    val selectedVideoUri: StateFlow<String?> = _selectedVideoUri

    // Playing Voice Message State
    private val _playingMessageId = MutableStateFlow<String?>(null)
    val playingMessageId: StateFlow<String?> = _playingMessageId

    // Typing status state
    private val _isParticipantTyping = MutableStateFlow(false)
    val isParticipantTyping: StateFlow<Boolean> = _isParticipantTyping

    private var userTypingJob: Job? = null

    fun onUserTyping(currentInput: String) {
        userTypingJob?.cancel()
        if (currentInput.isNotBlank()) {
            userTypingJob = viewModelScope.launch {
                delay(1200)
                // Simulate partner starting to type when user types
                _isParticipantTyping.value = true
                delay(3000)
                _isParticipantTyping.value = false
            }
        } else {
            _isParticipantTyping.value = false
        }
    }

    fun loadConversation(conversationId: String) {
        _currentConversationId.value = conversationId
        viewModelScope.launch {
            repository.markConversationRead(conversationId)
        }
    }

    fun sendTextMessage(text: String) {
        val id = _currentConversationId.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = id,
                text = text.trim(),
                mediaType = MessageType.TEXT
            )
            // Show typing indicator while background simulateAutoReply is preparing response
            delay(500)
            _isParticipantTyping.value = true
            delay(1300)
            _isParticipantTyping.value = false
        }
    }

    fun sendImageMessage(imageUrl: String, caption: String = "") {
        val id = _currentConversationId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = id,
                text = caption,
                mediaUrl = imageUrl,
                mediaType = MessageType.IMAGE
            )
            _selectedImageUri.value = null
        }
    }

    fun sendVideoMessage(videoUrl: String, caption: String = "") {
        val id = _currentConversationId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = id,
                text = caption.ifBlank { "Video clip" },
                mediaUrl = videoUrl,
                mediaType = MessageType.VIDEO
            )
            _selectedVideoUri.value = null
        }
    }

    fun startVoiceRecording() {
        _isRecordingVoice.value = true
        _recordingDurationSeconds.value = 0
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (_isRecordingVoice.value) {
                delay(1000)
                _recordingDurationSeconds.value += 1
            }
        }
    }

    fun stopAndSendVoiceRecording() {
        val id = _currentConversationId.value ?: return
        val duration = _recordingDurationSeconds.value
        _isRecordingVoice.value = false
        recordingJob?.cancel()

        if (duration >= 1) {
            viewModelScope.launch {
                repository.sendMessage(
                    conversationId = id,
                    text = "Voice message",
                    mediaType = MessageType.VOICE,
                    voiceDurationSeconds = duration
                )
            }
        }
        _recordingDurationSeconds.value = 0
    }

    fun cancelVoiceRecording() {
        _isRecordingVoice.value = false
        recordingJob?.cancel()
        _recordingDurationSeconds.value = 0
    }

    fun toggleVoicePlayback(messageId: String) {
        if (_playingMessageId.value == messageId) {
            _playingMessageId.value = null
        } else {
            _playingMessageId.value = messageId
            // Auto reset playback after 3s simulation
            viewModelScope.launch {
                delay(4000)
                if (_playingMessageId.value == messageId) {
                    _playingMessageId.value = null
                }
            }
        }
    }

    fun selectImageForSharing(uri: String?) {
        _selectedImageUri.value = uri
    }

    fun selectVideoForSharing(uri: String?) {
        _selectedVideoUri.value = uri
    }

    fun toggleMessageReaction(messageId: String, currentReaction: String, emoji: String) {
        val newReaction = if (currentReaction == emoji) "" else emoji
        viewModelScope.launch {
            repository.setMessageReaction(messageId, newReaction)
        }
    }
}
