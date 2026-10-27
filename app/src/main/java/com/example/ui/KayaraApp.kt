package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.LiveCallScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.signOutUser
import com.example.ui.viewmodel.KayaraViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@Composable
fun KayaraApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        SignInScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        MainKayaraContent(
            onSignOut = {
                signOutUser(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        currentUser = null
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}

@Composable
fun MainKayaraContent(
    onSignOut: () -> Unit,
    viewModel: KayaraViewModel = viewModel()
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val isThinking by viewModel.isThinking.collectAsStateWithLifecycle()
    val callState by viewModel.callState.collectAsStateWithLifecycle()
    val isListening by viewModel.speechManager.isListening.collectAsStateWithLifecycle()
    val detectedModels by viewModel.detectedModels.collectAsStateWithLifecycle()
    val isDetectingModels by viewModel.isDetectingModels.collectAsStateWithLifecycle()
    val modelDetectionStatus by viewModel.modelDetectionStatus.collectAsStateWithLifecycle()

    var isSettingsOpen by remember { mutableStateOf(false) }

    // Toast listener
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Chat Screen
        ChatScreen(
            messages = messages,
            settings = settings,
            isThinking = isThinking,
            isListening = isListening,
            onSendMessage = { text -> viewModel.sendMessage(text) },
            onStartVoice = { viewModel.startDictation() },
            onStopVoice = { viewModel.stopDictation() },
            onOpenLiveCall = { viewModel.startLiveCall() },
            onToggleAutoRead = { viewModel.toggleAutoRead() },
            onOpenSettings = { isSettingsOpen = true },
            onSpeakMessage = { text -> viewModel.speakMessage(text) }
        )

        // Live Voice Call Screen Overlay
        if (callState.isOpen) {
            LiveCallScreen(
                companionName = settings.companionName,
                callState = callState,
                onEndCall = { viewModel.endLiveCall() },
                onToggleMic = { viewModel.toggleCallMic() },
                onToggleVoice = { viewModel.toggleCallVoice() },
                onInterrupt = { viewModel.interruptCallSpeaking() }
            )
        }

        // Settings Sheet Overlay
        AnimatedVisibility(
            visible = isSettingsOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            BackHandler {
                isSettingsOpen = false
            }

            ProfileSettingsScreen(
                settings = settings,
                memories = memories,
                detectedModels = detectedModels,
                isDetectingModels = isDetectingModels,
                modelDetectionStatus = modelDetectionStatus,
                onDetectModels = { key -> viewModel.detectModels(key) },
                onSaveSettings = { newS -> viewModel.saveSettings(newS) },
                onTestApiKey = { key, model -> viewModel.testApiKey(key, model) },
                onAddMemory = { fact, category -> viewModel.addManualMemory(fact, category) },
                onUpdateMemory = { id, fact, category -> viewModel.updateMemory(id, fact, category) },
                onRemoveMemory = { id -> viewModel.removeMemory(id) },
                onClearChat = { viewModel.clearChat() },
                onClearMemories = { viewModel.clearMemories() },
                onSpeakTest = { text -> viewModel.speakMessage(text) },
                onSignOut = onSignOut,
                onClose = { isSettingsOpen = false }
            )
        }
    }
}
