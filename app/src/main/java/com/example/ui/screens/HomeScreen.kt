package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CallLogEntity
import com.example.data.local.ConversationEntity
import com.example.ui.components.UserAvatar
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.UnreadBadge
import com.example.ui.viewmodel.ConversationFilter
import com.example.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onConversationClick: (String) -> Unit,
    onVoiceCallClick: (String, String, String) -> Unit,
    onVideoCallClick: (String, String, String) -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Chats, 1 = Calls
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val filter by homeViewModel.filter.collectAsState()
    val conversations by homeViewModel.conversations.collectAsState()
    val unreadNotifsCount by homeViewModel.unreadNotificationsCount.collectAsState()
    val callLogs by homeViewModel.repository.getAllCallLogs().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Forum,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "ConnectX",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.testTag("notifications_icon_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifsCount > 0) {
                                    Badge(
                                        containerColor = UnreadBadge,
                                        contentColor = Color.White
                                    ) {
                                        Text(unreadNotifsCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                        }
                    }

                    IconButton(onClick = onProfileClick) {
                        UserAvatar(
                            imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                            name = "Me",
                            size = 32.dp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(if (selectedTab == 0) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline, contentDescription = "Chats") },
                    label = { Text("Chats") },
                    modifier = Modifier.testTag("nav_chats_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(if (selectedTab == 1) Icons.Filled.Call else Icons.Outlined.Call, contentDescription = "Calls") },
                    label = { Text("Calls") },
                    modifier = Modifier.testTag("nav_calls_tab")
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onNotificationClick,
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadNotifsCount > 0) {
                                    Badge { Text(unreadNotifsCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Alerts")
                        }
                    },
                    label = { Text("Alerts") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onProfileClick,
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onSettingsClick,
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0 && conversations.isNotEmpty()) {
                        onConversationClick(conversations.first().id)
                    } else {
                        onVoiceCallClick("Sarah Connor", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400", "user_sarah")
                    }
                },
                containerColor = PrimaryIndigo,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("new_action_fab")
            ) {
                Icon(
                    imageVector = if (selectedTab == 0) Icons.Default.Edit else Icons.Default.Call,
                    contentDescription = "New Action"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search TextField
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { homeViewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search messages, contacts, calls...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { homeViewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_chats_input")
            )

            if (selectedTab == 0) {
                // Stories / Online Users Row
                OnlineUsersStoryRow(
                    onUserClick = { name, avatar, id ->
                        onConversationClick(id)
                    }
                )

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filter == ConversationFilter.ALL,
                        onClick = { homeViewModel.onFilterChanged(ConversationFilter.ALL) },
                        label = { Text("All Chats") }
                    )

                    FilterChip(
                        selected = filter == ConversationFilter.UNREAD,
                        onClick = { homeViewModel.onFilterChanged(ConversationFilter.UNREAD) },
                        label = { Text("Unread") }
                    )

                    FilterChip(
                        selected = filter == ConversationFilter.GROUPS,
                        onClick = { homeViewModel.onFilterChanged(ConversationFilter.GROUPS) },
                        label = { Text("Groups") }
                    )
                }

                // Conversations List
                if (conversations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No conversations found", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(conversations, key = { it.id }) { conv ->
                            ConversationItemRow(
                                conversation = conv,
                                onClick = {
                                    homeViewModel.markRead(conv.id)
                                    onConversationClick(conv.id)
                                }
                            )
                        }
                    }
                }
            } else {
                // Calls Tab
                if (callLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.PhoneMissed,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No recent call history", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(callLogs, key = { it.id }) { call ->
                            CallLogRowItem(
                                callLog = call,
                                onVoiceCallClick = {
                                    onVoiceCallClick(call.participantName, call.participantAvatar, call.participantId)
                                },
                                onVideoCallClick = {
                                    onVideoCallClick(call.participantName, call.participantAvatar, call.participantId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnlineUsersStoryRow(
    onUserClick: (String, String, String) -> Unit
) {
    val stories = remember {
        listOf(
            Triple("Sarah C.", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400", "conv_sarah"),
            Triple("Alex R.", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400", "conv_alex"),
            Triple("Elena R.", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400", "conv_elena"),
            Triple("Tech Group", "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=400", "conv_tech_group")
        )
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Story",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Your Story", style = MaterialTheme.typography.labelMedium)
            }
        }

        items(stories) { (name, avatar, convId) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onUserClick(name, avatar, convId) }
            ) {
                UserAvatar(
                    imageUrl = avatar,
                    name = name,
                    size = 56.dp,
                    isOnline = true
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ConversationItemRow(
    conversation: ConversationEntity,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(conversation.lastMessageTimestamp) {
        dateFormat.format(Date(conversation.lastMessageTimestamp))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("conversation_item_${conversation.id}"),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                imageUrl = conversation.participantAvatar,
                name = conversation.participantName,
                size = 52.dp,
                isOnline = conversation.participantStatus == "Online"
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.participantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (conversation.unreadCount > 0) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessageText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (conversation.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallLogRowItem(
    callLog: CallLogEntity,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(callLog.timestamp) { dateFormat.format(Date(callLog.timestamp)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            imageUrl = callLog.participantAvatar,
            name = callLog.participantName,
            size = 48.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = callLog.participantName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        callLog.isMissed -> Icons.Default.CallMissed
                        callLog.isOutgoing -> Icons.Default.CallMade
                        else -> Icons.Default.CallReceived
                    },
                    contentDescription = null,
                    tint = if (callLog.isMissed) MaterialTheme.colorScheme.error else OnlineGreen,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "$formattedTime • ${if (callLog.isVideoCall) "Video" else "Voice"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onVoiceCallClick) {
            Icon(Icons.Default.Call, contentDescription = "Voice call", tint = PrimaryIndigo)
        }

        IconButton(onClick = onVideoCallClick) {
            Icon(Icons.Default.Videocam, contentDescription = "Video call", tint = PrimaryIndigo)
        }
    }
}
