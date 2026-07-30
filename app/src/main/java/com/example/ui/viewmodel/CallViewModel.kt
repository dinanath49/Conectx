package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ConnectXRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class CallState {
    RINGING, CONNECTED, ENDED
}

class CallViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ConnectXRepository(application)

    private val _participantName = MutableStateFlow("Sarah Connor")
    val participantName: StateFlow<String> = _participantName

    private val _participantAvatar = MutableStateFlow("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400")
    val participantAvatar: StateFlow<String> = _participantAvatar

    private val _participantId = MutableStateFlow("user_sarah")

    private val _isVideoCall = MutableStateFlow(false)
    val isVideoCall: StateFlow<Boolean> = _isVideoCall

    private val _callState = MutableStateFlow(CallState.RINGING)
    val callState: StateFlow<CallState> = _callState

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    private val _isSpeakerOn = MutableStateFlow(true)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn

    private val _isVideoEnabled = MutableStateFlow(true)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled

    private val _isFrontCamera = MutableStateFlow(true)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera

    private var timerJob: Job? = null

    fun startCall(name: String, avatar: String, participantId: String, isVideo: Boolean) {
        _participantName.value = name.ifBlank { "Sarah Connor" }
        _participantAvatar.value = avatar.ifBlank { "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400" }
        _participantId.value = participantId
        _isVideoCall.value = isVideo
        _callState.value = CallState.RINGING
        _callDurationSeconds.value = 0
        _isMuted.value = false
        _isSpeakerOn.value = true
        _isVideoEnabled.value = true

        // Simulate connecting after 2.5 seconds
        viewModelScope.launch {
            delay(2500)
            if (_callState.value == CallState.RINGING) {
                _callState.value = CallState.CONNECTED
                startCallTimer()
            }
        }
    }

    private fun startCallTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_callState.value == CallState.CONNECTED) {
                delay(1000)
                _callDurationSeconds.value += 1
            }
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
    }

    fun toggleVideo() {
        _isVideoEnabled.value = !_isVideoEnabled.value
    }

    fun switchCamera() {
        _isFrontCamera.value = !_isFrontCamera.value
    }

    fun endCall(onEnded: () -> Unit) {
        timerJob?.cancel()
        val duration = _callDurationSeconds.value
        _callState.value = CallState.ENDED

        viewModelScope.launch {
            repository.addCallLog(
                participantId = _participantId.value,
                participantName = _participantName.value,
                participantAvatar = _participantAvatar.value,
                isVideoCall = _isVideoCall.value,
                isOutgoing = true,
                durationSeconds = duration
            )
            delay(500)
            onEnded()
        }
    }
}
