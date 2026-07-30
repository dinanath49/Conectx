package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.NotificationItemEntity
import com.example.data.repository.ConnectXRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ConnectXRepository(application)

    val notifications: StateFlow<List<NotificationItemEntity>> = repository.getAllNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }
}
