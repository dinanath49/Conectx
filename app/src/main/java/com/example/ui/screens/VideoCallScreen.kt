package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CallRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.viewmodel.CallState
import com.example.ui.viewmodel.CallViewModel

@Composable
fun VideoCallScreen(
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
            isVideo = true
        )
    }

    val callState by callViewModel.callState.collectAsState()
    val durationSeconds by callViewModel.callDurationSeconds.collectAsState()
    val isMuted by callViewModel.isMuted.collectAsState()
    val isVideoEnabled by callViewModel.isVideoEnabled.collectAsState()

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val formattedTimer = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        containerColor = DarkBackground,
        modifier = Modifier.fillMaxSize().testTag("video_call_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Fullscreen Remote Video Stream Simulation
            if (isVideoEnabled) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(participantAvatar)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Video Stream",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        UserAvatar(imageUrl = participantAvatar, name = participantName, size = 100.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(participantName, color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text("Camera Turned Off", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            // Dark Gradient Overlay at Top & Bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // Top Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(imageUrl = participantAvatar, name = participantName, size = 42.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(participantName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = when (callState) {
                                CallState.RINGING -> "Connecting..."
                                CallState.CONNECTED -> "HD Video Call • $formattedTimer"
                                CallState.ENDED -> "Call Ended"
                            },
                            color = PrimaryIndigo,
                            fontSize = 12.sp
                        )
                    }
                }

                Surface(
                    color = Color.Red.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Local Camera Selfie Preview PIP Box (Top Right)
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 20.dp)
                    .size(width = 110.dp, height = 150.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Local Camera Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Bottom Call Actions Control Bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { callViewModel.toggleMute() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (isMuted) DarkBackground else Color.White
                    )
                }

                IconButton(
                    onClick = { callViewModel.toggleVideo() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (!isVideoEnabled) Color.White else Color.White.copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Video Toggle",
                        tint = if (!isVideoEnabled) DarkBackground else Color.White
                    )
                }

                IconButton(
                    onClick = { callViewModel.switchCamera() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                ) {
                    Icon(Icons.Default.FlipCameraIos, contentDescription = "Switch Camera", tint = Color.White)
                }

                IconButton(
                    onClick = { callViewModel.endCall(onEndCall) },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(CallRed)
                        .testTag("end_video_call_button")
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
