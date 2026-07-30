package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserEntity
import com.example.data.repository.ConnectXRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ConnectXRepository(application)

    val currentUser: StateFlow<UserEntity?> = repository.currentUser

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun login(email: String, name: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.login(email, name)
            _isLoggedIn.value = true
            _isLoading.value = false
        }
    }

    fun signup(username: String, email: String, bio: String) {
        if (username.isBlank() || email.isBlank()) {
            _errorMessage.value = "Username and email are required"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.signup(username, email, bio)
            _isLoggedIn.value = true
            _isLoading.value = false
        }
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
