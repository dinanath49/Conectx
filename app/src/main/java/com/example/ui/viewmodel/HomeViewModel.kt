package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.repository.ConnectXRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ConversationFilter {
    ALL, UNREAD, GROUPS
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ConnectXRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filter = MutableStateFlow(ConversationFilter.ALL)
    val filter: StateFlow<ConversationFilter> = _filter

    val conversations: StateFlow<List<ConversationEntity>> = combine(
        repository.getAllConversations(),
        _searchQuery,
        _filter
    ) { list, query, filter ->
        list.filter { conv ->
            val matchesQuery = query.isBlank() || conv.participantName.contains(query, ignoreCase = true) || conv.lastMessageText.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                ConversationFilter.ALL -> true
                ConversationFilter.UNREAD -> conv.unreadCount > 0
                ConversationFilter.GROUPS -> conv.isGroup
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadNotificationsCount: StateFlow<Int> = repository.getUnreadNotificationCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(newFilter: ConversationFilter) {
        _filter.value = newFilter
    }

    fun markRead(conversationId: String) {
        viewModelScope.launch {
            repository.markConversationRead(conversationId)
        }
    }
}
