package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserEntity
import com.example.data.repository.ConnectXRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ConnectXRepository(application)

    val currentUser: StateFlow<UserEntity?> = repository.currentUser

    fun updateProfile(username: String, bio: String, avatarUrl: String) {
        viewModelScope.launch {
            repository.updateProfile(username, bio, avatarUrl)
        }
    }
}
