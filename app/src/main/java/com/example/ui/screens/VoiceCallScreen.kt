package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CallRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.viewmodel.CallState
import com.example.ui.viewmodel.CallViewModel

@Composable
fun VoiceCallScreen(
    participantName: String,
    participantAvatar: String,
    participantId: String,
    callViewModel: CallViewModel,
    onEndCall: () -> Unit
) {
    LaunchedEffect(participantName) {
        callViewModel.startCall(
            name = participantName,
            avatar = participantAvatar,
            participantId = participantId,
            isVideo = false
        )
    }

    val callState by callViewModel.callState.collectAsState()
    val durationSeconds by callViewModel.callDurationSeconds.collectAsState()
    val isMuted by callViewModel.isMuted.collectAsState()
    val isSpeakerOn by callViewModel.isSpeakerOn.collectAsState()

    // Pulsing ripple animation for audio
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val formattedTimer = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        containerColor = DarkBackground,
        modifier = Modifier.fillMaxSize().testTag("voice_call_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Participant Details & Status
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ConnectX Voice Call",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = participantName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (callState) {
                        CallState.RINGING -> "Ringing..."
                        CallState.CONNECTED -> formattedTimer
                        CallState.ENDED -> "Call Ended"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = if (callState == CallState.CONNECTED) PrimaryIndigo else Color.White.copy(alpha = 0.8f)
                )
            }

            // Animated Avatar with Audio Pulsing Ripples
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                if (callState == CallState.CONNECTED) {
                    Box(
                        modifier = Modifier
                            .size((140 * pulseScale).dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo.copy(alpha = 0.25f))
                    )
                }

                UserAvatar(
                    imageUrl = participantAvatar,
                    name = participantName,
                    size = 120.dp
                )
            }

            // Call Action Buttons
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { callViewModel.toggleMute() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (isMuted) DarkBackground else Color.White
                        )
                    }

                    IconButton(
                        onClick = { callViewModel.toggleSpeaker() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isSpeakerOn) PrimaryIndigo else Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Speaker",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { callViewModel.endCall(onEndCall) },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(CallRed)
                            .testTag("end_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
