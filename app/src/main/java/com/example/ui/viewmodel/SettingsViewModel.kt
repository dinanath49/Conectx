package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.repository.ConnectXRepository
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ConnectXRepository(application)

    val isDarkMode: StateFlow<Boolean> = repository.isDarkMode
    val notificationsEnabled: StateFlow<Boolean> = repository.notificationsEnabled
    val readReceiptsEnabled: StateFlow<Boolean> = repository.readReceiptsEnabled
    val autoPlayVoice: StateFlow<Boolean> = repository.autoPlayVoice

    fun toggleDarkMode(enabled: Boolean) {
        repository.updateDarkMode(enabled)
    }

    fun toggleNotifications(enabled: Boolean) {
        repository.updateNotificationsEnabled(enabled)
    }

    fun toggleReadReceipts(enabled: Boolean) {
        repository.updateReadReceipts(enabled)
    }
}
