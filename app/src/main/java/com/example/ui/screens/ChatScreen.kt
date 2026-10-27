package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ChatMessage
import com.example.data.model.CompanionSettings
import com.example.ui.components.ChatInputBar
import com.example.ui.components.HeaderBar
import com.example.ui.components.MessageBubble
import com.example.ui.components.QuickPromptSuggestions
import com.example.ui.components.ThinkingIndicator
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.DimText
import com.example.ui.theme.LineBorderSubtle
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.PanelSurface

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    settings: CompanionSettings,
    isThinking: Boolean,
    isListening: Boolean,
    onSendMessage: (String) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onOpenLiveCall: () -> Unit,
    onToggleAutoRead: () -> Unit,
    onOpenSettings: () -> Unit,
    onSpeakMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Permission launcher for RECORD_AUDIO
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartVoice()
        }
    }

    val liveCallPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onOpenLiveCall()
        }
    }

    // Auto-scroll to bottom when messages update or thinking starts
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B0F33),
                        Color(0xFF0F0B1E),
                        DeepSpace
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            HeaderBar(
                isAutoReadActive = settings.autoReadReplies,
                onToggleAutoRead = onToggleAutoRead,
                onOpenSettings = onOpenSettings,
                companionName = settings.companionName,
                personalityPresetId = settings.personalityPreset
            )

            // Active Model & Status Indicator Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelSurface.copy(alpha = 0.5f))
                        .border(1.dp, LineBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${settings.model} • Google Live",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = DimText
                            )
                        )
                    }
                }
            }

            // Chat Message List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            onSpeak = onSpeakMessage
                        )
                    }

                    if (isThinking) {
                        item {
                            ThinkingIndicator()
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Quick Prompt Suggestions
            if (messages.size <= 4) {
                QuickPromptSuggestions(
                    onSelect = { prompt ->
                        onSendMessage(prompt)
                    }
                )
            }

            // Bottom Input Bar
            ChatInputBar(
                isListening = isListening,
                isThinking = isThinking,
                onSendMessage = onSendMessage,
                onStartVoice = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        onStartVoice()
                    } else {
                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopVoice = onStopVoice,
                onOpenLiveCall = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        onOpenLiveCall()
                    } else {
                        liveCallPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }
    }
}
