package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallStatus
import com.example.data.model.CallUiState
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.DimText
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InkBackground
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MistText
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PanelSurface
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RosePrimary

@Composable
fun LiveCallScreen(
    companionName: String,
    callState: CallUiState,
    onEndCall: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleVoice: () -> Unit,
    onInterrupt: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onEndCall()
    }

    val minutes = callState.durationSeconds / 60
    val seconds = callState.durationSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val transition = rememberInfiniteTransition(label = "orbAnimations")

    // Breathing pulse for listening state
    val breatheScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Morph rotation for thinking state
    val rotateDeg by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    // Talking pulse for speaking state
    val talkScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "talk"
    )

    // Waveform ring expansion
    val ringScale by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringScale"
    )

    val ringAlpha by transition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAlpha"
    )

    val waveHeight1 by transition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w1"
    )
    val waveHeight2 by transition.animateFloat(
        initialValue = 24f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w2"
    )
    val waveHeight3 by transition.animateFloat(
        initialValue = 8f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w3"
    )

    val currentScale = when (callState.status) {
        CallStatus.LISTENING -> breatheScale
        CallStatus.THINKING -> 1.03f
        CallStatus.SPEAKING -> talkScale
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF321350),
                        Color(0xFF1B0B30),
                        DeepSpace
                    ),
                    radius = 1400f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Companion Name, Time, and Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = companionName,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = MistText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MistText
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelSurface.copy(alpha = 0.7f))
                        .border(1.dp, LineBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when (callState.status) {
                            CallStatus.LISTENING -> if (callState.isMicMuted) "Microphone muted" else "Listening to your voice..."
                            CallStatus.THINKING -> "Kayara is thinking..."
                            CallStatus.SPEAKING -> "Speaking to you..."
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccent
                    )
                }
            }

            // Center Stage: Animated Glowing Holographic Orb and Rings
            Box(
                modifier = Modifier
                    .size(290.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding Ripple Rings
                if (callState.status == CallStatus.SPEAKING || callState.status == CallStatus.LISTENING) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(ringScale)
                            .border(2.dp, GoldAccent.copy(alpha = ringAlpha), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(ringScale * 0.82f)
                            .border(2.dp, RosePrimary.copy(alpha = ringAlpha * 0.8f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(ringScale * 0.65f)
                            .border(1.5.dp, ElectricViolet.copy(alpha = ringAlpha), CircleShape)
                    )
                }

                // The Central Glowing Celestial Orb
                Box(
                    modifier = Modifier
                        .size(175.dp)
                        .scale(currentScale)
                        .rotate(if (callState.status == CallStatus.THINKING) rotateDeg else 0f)
                        .shadow(
                            elevation = 36.dp,
                            shape = CircleShape,
                            spotColor = RosePrimary,
                            ambientColor = GoldAccent
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GoldAccent,
                                    RosePrimary,
                                    ElectricViolet,
                                    RoseDark,
                                    Color(0xFF380826)
                                )
                            )
                        )
                        .border(2.dp, GoldAccent.copy(alpha = 0.6f), CircleShape)
                )

                // Soundwave visualizer bars in center
                if (callState.status == CallStatus.SPEAKING || (callState.status == CallStatus.LISTENING && !callState.isMicMuted)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(waveHeight1.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(waveHeight2.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(waveHeight3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(waveHeight2.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(waveHeight1.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White)
                        )
                    }
                }
            }

            // Live Captions Section (Glassmorphic Container)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PanelSurface.copy(alpha = 0.65f))
                    .border(1.dp, LineBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (callState.userCaption.isNotBlank()) {
                        Text(
                            text = callState.userCaption,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            color = DimText,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = callState.companionCaption.ifBlank { "Listening..." },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        ),
                        color = MistText,
                        textAlign = TextAlign.Center,
                        maxLines = 3
                    )
                }
            }

            // Bottom Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Mute Button
                IconButton(
                    onClick = onToggleMic,
                    modifier = Modifier
                        .testTag("call_mute_mic_button")
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (callState.isMicMuted) MistText else PanelSurface)
                        .border(1.dp, LineBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = if (callState.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Toggle Microphone",
                        tint = if (callState.isMicMuted) InkBackground else MistText,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Interrupt / Stop Speaking Button (visible only when speaking)
                AnimatedVisibility(
                    visible = callState.status == CallStatus.SPEAKING,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(
                        onClick = onInterrupt,
                        modifier = Modifier
                            .testTag("call_interrupt_button")
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PanelSurface)
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Interrupt",
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Voice Mute Button (companion voice audio toggle)
                IconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier
                        .testTag("call_toggle_voice_button")
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (callState.isVoiceMuted) MistText else PanelSurface)
                        .border(1.dp, LineBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = if (callState.isVoiceMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Toggle Companion Voice",
                        tint = if (callState.isVoiceMuted) InkBackground else MistText,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // End Call Red Button (Glowing)
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .testTag("call_end_button")
                        .size(68.dp)
                        .shadow(elevation = 16.dp, shape = CircleShape, spotColor = ErrorRed)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ErrorRed, Color(0xFFC01538))
                            )
                        )
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
