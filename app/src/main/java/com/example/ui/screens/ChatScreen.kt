package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.MessageEntity
import com.example.data.local.MessageType
import com.example.ui.components.UserAvatar
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    chatViewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onVoiceCallClick: (String, String, String) -> Unit,
    onVideoCallClick: (String, String, String) -> Unit
) {
    LaunchedEffect(conversationId) {
        chatViewModel.loadConversation(conversationId)
    }

    val conversation by chatViewModel.conversation.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val isRecordingVoice by chatViewModel.isRecordingVoice.collectAsState()
    val recordingDuration by chatViewModel.recordingDurationSeconds.collectAsState()
    val playingMessageId by chatViewModel.playingMessageId.collectAsState()
    val isParticipantTyping by chatViewModel.isParticipantTyping.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    var activeReactionMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var activeFloatingEmoji by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new messages or when typing starts
    LaunchedEffect(messages.size, isParticipantTyping) {
        val totalCount = messages.size + if (isParticipantTyping) 1 else 0
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { }
                    ) {
                        UserAvatar(
                            imageUrl = conversation?.participantAvatar,
                            name = conversation?.participantName ?: "Chat",
                            size = 40.dp,
                            isOnline = conversation?.participantStatus == "Online"
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = conversation?.participantName ?: "Chat",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            AnimatedContent(
                                targetState = isParticipantTyping,
                                label = "typing_status_anim"
                            ) { typing ->
                                if (typing) {
                                    Text(
                                        text = "typing...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PrimaryIndigo,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.testTag("chat_typing_status_text")
                                    )
                                } else {
                                    Text(
                                        text = conversation?.participantStatus ?: "Online",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (conversation?.participantStatus == "Online") OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val c = conversation
                            if (c != null) {
                                onVoiceCallClick(c.participantName, c.participantAvatar, c.participantId)
                            }
                        },
                        modifier = Modifier.testTag("chat_voice_call_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = PrimaryIndigo)
                    }

                    IconButton(
                        onClick = {
                            val c = conversation
                            if (c != null) {
                                onVideoCallClick(c.participantName, c.participantAvatar, c.participantId)
                            }
                        },
                        modifier = Modifier.testTag("chat_video_call_button")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = PrimaryIndigo)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                if (isRecordingVoice) {
                    // Voice Recorder Controls Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording... 00:${if (recordingDuration < 10) "0$recordingDuration" else recordingDuration}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }

                        Row {
                            TextButton(onClick = { chatViewModel.cancelVoiceRecording() }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.error)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { chatViewModel.stopAndSendVoiceRecording() },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PrimaryIndigo)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send Voice Note", tint = Color.White)
                            }
                        }
                    }
                } else {
                    // Standard Message Input Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showAttachmentSheet = true },
                            modifier = Modifier.testTag("attach_media_button")
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Attach media", tint = PrimaryIndigo)
                        }

                        OutlinedTextField(
                            value = textInput,
                            onValueChange = {
                                textInput = it
                                chatViewModel.onUserTyping(it)
                            },
                            placeholder = { Text("Type a message...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_message_input"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        if (textInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    chatViewModel.sendTextMessage(textInput)
                                    textInput = ""
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PrimaryIndigo)
                                    .testTag("send_message_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send Message", tint = Color.White)
                            }
                        } else {
                            // Voice recorder trigger
                            IconButton(
                                onClick = { chatViewModel.startVoiceRecording() },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("record_voice_button")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Record Voice Note", tint = PrimaryIndigo)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        isPlayingVoice = playingMessageId == msg.id,
                        onVoiceToggle = { chatViewModel.toggleVoicePlayback(msg.id) },
                        onImageClick = { previewImageUrl = msg.mediaUrl },
                        onOpenReactionPicker = { activeReactionMessage = msg },
                        onToggleReaction = { emoji ->
                            chatViewModel.toggleMessageReaction(msg.id, msg.reaction, emoji)
                            activeFloatingEmoji = emoji
                        }
                    )
                }

                if (isParticipantTyping) {
                    item(key = "typing_indicator") {
                        TypingIndicatorBubble(
                            imageUrl = conversation?.participantAvatar,
                            name = conversation?.participantName ?: "Chat"
                        )
                    }
                }
            }
        }
    }

    // Attachment Modal Bottom Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(onDismissRequest = { showAttachmentSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Share Content",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOptionItem(
                        icon = Icons.Default.Image,
                        label = "Image",
                        color = PrimaryIndigo,
                        onClick = {
                            showAttachmentSheet = false
                            chatViewModel.sendImageMessage(
                                imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                                caption = "Beautiful landscape photo!"
                            )
                        }
                    )

                    AttachmentOptionItem(
                        icon = Icons.Default.Videocam,
                        label = "Video",
                        color = Color(0xFFEF4444),
                        onClick = {
                            showAttachmentSheet = false
                            chatViewModel.sendVideoMessage(
                                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                                caption = "Check out this video clip!"
                            )
                        }
                    )

                    AttachmentOptionItem(
                        icon = Icons.Default.Mic,
                        label = "Voice Note",
                        color = Color(0xFF10B981),
                        onClick = {
                            showAttachmentSheet = false
                            chatViewModel.startVoiceRecording()
                        }
                    )
                }
            }
        }
    }

    // Image Zoom Dialog
    if (previewImageUrl != null) {
        Dialog(onDismissRequest = { previewImageUrl = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(previewImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Expanded Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        contentScale = ContentScale.Crop
                    )

                    TextButton(onClick = { previewImageUrl = null }) {
                        Text("Close Preview")
                    }
                }
            }
        }
    }

    // Emoji Reaction Picker Dialog
    if (activeReactionMessage != null) {
        EmojiReactionPickerDialog(
            message = activeReactionMessage!!,
            onDismiss = { activeReactionMessage = null },
            onSelectEmoji = { emoji ->
                val msg = activeReactionMessage!!
                chatViewModel.toggleMessageReaction(msg.id, msg.reaction, emoji)
                activeFloatingEmoji = emoji
                activeReactionMessage = null
            }
        )
    }

    // Floating Animated Emoji Particles Overlay Effect
    if (activeFloatingEmoji != null) {
        FloatingEmojiOverlay(
            emoji = activeFloatingEmoji!!,
            onFinished = { activeFloatingEmoji = null }
        )
    }
}

@Composable
fun AttachmentOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageEntity,
    isPlayingVoice: Boolean,
    onVoiceToggle: () -> Unit,
    onImageClick: () -> Unit,
    onOpenReactionPicker: () -> Unit,
    onToggleReaction: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { dateFormat.format(Date(message.timestamp)) }

    val isMe = message.isSentByMe
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isMe) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("message_bubble_${message.id}"),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .combinedClickable(
                        onClick = { },
                        onLongClick = onOpenReactionPicker
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    when (message.mediaType) {
                        MessageType.TEXT -> {
                            Text(
                                text = message.text,
                                color = textColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        MessageType.IMAGE -> {
                            if (message.mediaUrl.isNotBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(message.mediaUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Shared image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(onClick = onImageClick),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            if (message.text.isNotBlank()) {
                                Text(
                                    text = message.text,
                                    color = textColor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        MessageType.VOICE -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onVoiceToggle) {
                                    Icon(
                                        imageVector = if (isPlayingVoice) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                        contentDescription = "Play/Pause Voice",
                                        tint = textColor,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Waveform simulation bars
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(12, 24, 18, 30, 16, 28, 14, 22, 10).forEach { barHeight ->
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(barHeight.dp)
                                                .clip(CircleShape)
                                                .background(if (isPlayingVoice) OnlineGreen else textColor.copy(alpha = 0.7f))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "0:${if (message.voiceDurationSeconds < 10) "0${message.voiceDurationSeconds}" else message.voiceDurationSeconds}",
                                    color = textColor,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        MessageType.VIDEO -> {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data("https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Video preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Play Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = message.text,
                                color = textColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onOpenReactionPicker,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Outlined.AddReaction,
                                contentDescription = "Add Reaction",
                                tint = textColor.copy(alpha = 0.65f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formattedTime,
                                fontSize = 10.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )

                            if (isMe) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.DoneAll,
                                    contentDescription = "Read status",
                                    tint = OnlineGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Attached Reaction Badge Chip
            AnimatedVisibility(
                visible = message.reaction.isNotBlank(),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Surface(
                    onClick = { onToggleReaction(message.reaction) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .offset(y = (-8).dp)
                        .padding(horizontal = 6.dp)
                        .testTag("message_reaction_badge_${message.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.reaction,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "1",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(
    imageUrl: String?,
    name: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_transition")

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1_alpha"
    )

    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2_alpha"
    )

    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("typing_indicator_bubble"),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        UserAvatar(
            imageUrl = imageUrl,
            name = name,
            size = 28.dp
        )

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(end = 48.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = alpha1))
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = alpha2))
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = alpha3))
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiReactionPickerDialog(
    message: MessageEntity,
    onDismiss: () -> Unit,
    onSelectEmoji: (String) -> Unit
) {
    val quickEmojis = remember { listOf("❤️", "👍", "😄", "😍", "🔥", "🎉", "💩", "👻") }
    
    val allEmojis = remember {
        listOf(
            "😄", "💩", "👻", "😍", "☺️", "😘", "🤩", "🙃", "😛", "🤡",
            "😕", "😟", "☹️", "😖", "😤", "🥵", "🥱", "😨", "😱", "😢",
            "😓", "😴", "🤔", "🤭", "😮", "💨", "😭", "🤫", "😥", "😐",
            "😧", "😪", "🙄", "😑", "😯", "✌️", "🖖", "🤚", "👌", "👈",
            "👉", "👆", "👇", "💪", "🤙", "☝️", "👄", "💋", "💄", "👅",
            "❤️", "👍", "🔥", "🎉", "💯", "🙏", "😂", "👏"
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val categories = remember { listOf("All", "Faces & Animated", "Gestures & Items") }

    val filteredEmojis = remember(selectedTab) {
        when (selectedTab) {
            1 -> allEmojis.take(35)
            2 -> allEmojis.drop(35)
            else -> allEmojis
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("emoji_reaction_picker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "React with Emoji",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (message.text.isNotBlank()) {
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 200.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Reaction Bar
                Text(
                    text = "Quick Reactions",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    quickEmojis.forEach { emoji ->
                        AnimatedEmojiButton(
                            emoji = emoji,
                            fontSize = 24.sp,
                            isSelected = message.reaction == emoji,
                            onClick = { onSelectEmoji(emoji) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEachIndexed { index, title ->
                        FilterChip(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            label = { Text(title, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryIndigo,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // All Emojis Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredEmojis, key = { it }) { emoji ->
                        AnimatedEmojiButton(
                            emoji = emoji,
                            fontSize = 22.sp,
                            isSelected = message.reaction == emoji,
                            onClick = { onSelectEmoji(emoji) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedEmojiButton(
    emoji: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.35f else if (isSelected) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "emoji_button_scale"
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isSelected) PrimaryIndigo.copy(alpha = 0.2f) else Color.Transparent
            )
            .clickable {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FloatingEmojiOverlay(
    emoji: String,
    onFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(emoji) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = LinearOutSlowInEasing)
        )
        onFinished()
    }

    val particleCount = 8
    val randomOffsets = remember {
        List(particleCount) {
            Pair(
                (-120..120).random().dp,
                (-280..-100).random().dp
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("floating_emoji_overlay"),
        contentAlignment = Alignment.Center
    ) {
        randomOffsets.forEach { (xOffset, yOffset) ->
            val animatedY = yOffset * progress.value
            val animatedX = xOffset * progress.value
            val alpha = (1f - progress.value).coerceIn(0f, 1f)
            val particleScale = (0.6f + progress.value * 0.8f).coerceAtMost(1.5f)

            Text(
                text = emoji,
                fontSize = 36.sp,
                modifier = Modifier
                    .offset(x = animatedX, y = animatedY)
                    .scale(particleScale)
                    .alpha(alpha)
            )
        }
    }
}
