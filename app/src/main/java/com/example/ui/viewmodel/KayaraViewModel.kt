package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechManager
import com.example.data.api.GeminiApiService
import com.example.data.model.CallStatus
import com.example.data.model.CallUiState
import com.example.data.model.ChatMessage
import com.example.data.model.CompanionSettings
import com.example.data.model.MemoryItem
import com.example.data.model.MessageRole
import com.example.data.storage.PreferencesManager
import android.util.Log
import com.example.data.firebase.FirestoreRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KayaraViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesManager(application)
    private val apiService = GeminiApiService()
    private val firestoreRepo = FirestoreRepository(application)
    val speechManager = SpeechManager(application)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _settings = MutableStateFlow(CompanionSettings())
    val settings: StateFlow<CompanionSettings> = _settings.asStateFlow()

    private val _memories = MutableStateFlow<List<MemoryItem>>(emptyList())
    val memories: StateFlow<List<MemoryItem>> = _memories.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _callState = MutableStateFlow(CallUiState())
    val callState: StateFlow<CallUiState> = _callState.asStateFlow()

    private val _detectedModels = MutableStateFlow<List<com.example.data.model.DiscoveredModel>>(emptyList())
    val detectedModels: StateFlow<List<com.example.data.model.DiscoveredModel>> = _detectedModels.asStateFlow()

    private val _isDetectingModels = MutableStateFlow(false)
    val isDetectingModels: StateFlow<Boolean> = _isDetectingModels.asStateFlow()

    private val _modelDetectionStatus = MutableStateFlow<String?>(null)
    val modelDetectionStatus: StateFlow<String?> = _modelDetectionStatus.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private var callTimerJob: Job? = null
    private var syncJob: Job? = null
    private var turnCounter = 0

    init {
        // Load persisted state
        _settings.value = prefs.loadSettings()
        _memories.value = prefs.loadMemories()
        val savedMessages = prefs.loadChatHistory()
        if (savedMessages.isNotEmpty()) {
            _messages.value = savedMessages
        } else {
            // Default companion introductory greeting matching persona
            val introText = getIntroGreeting(_settings.value)
            val initial = listOf(ChatMessage(role = MessageRole.MODEL, text = introText))
            _messages.value = initial
            prefs.saveChatHistory(initial)
        }

        // Start Firestore real-time synchronization
        startCloudSync()
    }

    private fun startCloudSync() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            // Observe settings
            launch {
                try {
                    firestoreRepo.observeUserSettings(uid).collect { cloudSettings ->
                        if (cloudSettings != null) {
                            val localKey = _settings.value.apiKey
                            val merged = cloudSettings.copy(apiKey = localKey.ifBlank { prefs.loadSettings().apiKey })
                            _settings.value = merged
                            prefs.saveSettings(merged)
                        } else {
                            // Initial cloud seeding
                            firestoreRepo.saveUserSettings(_settings.value)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("KayaraVM", "Cloud settings sync paused: ${e.message}")
                }
            }

            // Observe messages
            launch {
                try {
                    firestoreRepo.observeChatMessages(uid).collect { cloudMsgs ->
                        if (cloudMsgs.isNotEmpty()) {
                            _messages.value = cloudMsgs
                            prefs.saveChatHistory(cloudMsgs)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("KayaraVM", "Cloud messages sync paused: ${e.message}")
                }
            }

            // Observe memories
            launch {
                try {
                    firestoreRepo.observeMemories(uid).collect { cloudMemories ->
                        if (cloudMemories.isNotEmpty()) {
                            _memories.value = cloudMemories
                            prefs.saveMemories(cloudMemories)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("KayaraVM", "Cloud memories sync paused: ${e.message}")
                }
            }
        }
    }

    private fun getIntroGreeting(s: CompanionSettings): String {
        val userName = if (s.userName.isNotBlank()) " ${s.userName}" else ""
        val preset = com.example.data.model.PRESET_PERSONALITIES.find { it.id == s.personalityPreset }
            ?: com.example.data.model.PRESET_PERSONALITIES.find { it.id == s.relationship }
        if (preset != null) {
            return preset.sampleGreeting.replace("sweetheart", "sweetheart$userName").replace("bestie", "bestie$userName")
        }
        return "Hello$userName! I am ${s.companionName}, your companion. How are you today?"
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isThinking.value) return

        val userMessage = ChatMessage(role = MessageRole.USER, text = trimmed)
        val updated = _messages.value + userMessage
        _messages.value = updated
        prefs.saveChatHistory(updated)

        // Save to Firestore
        viewModelScope.launch {
            try {
                firestoreRepo.saveChatMessage(userMessage)
            } catch (e: Exception) {
                Log.w("KayaraVM", "Failed to sync message to cloud: ${e.message}")
            }
        }

        fetchAiReply(updated)
    }

    private fun fetchAiReply(currentMessages: List<ChatMessage>) {
        viewModelScope.launch {
            _isThinking.value = true
            val currentSettings = _settings.value

            val result = apiService.generateReply(
                messages = currentMessages,
                settings = currentSettings,
                memories = _memories.value
            )

            _isThinking.value = false

            result.onSuccess { genResult ->
                val replyText = genResult.replyText
                if (genResult.wasShifted) {
                    val prev = genResult.originalModel
                    val shifted = genResult.modelUsed
                    val updated = _settings.value.copy(model = shifted)
                    _settings.value = updated
                    prefs.saveSettings(updated)
                    viewModelScope.launch {
                        try {
                            firestoreRepo.saveUserSettings(updated)
                        } catch (e: Exception) {
                            Log.w("KayaraVM", "Failed to sync shifted settings: ${e.message}")
                        }
                        _toastEvent.emit("Auto-shifted to '$shifted': '$prev' was unavailable.")
                    }
                }

                val modelMessage = ChatMessage(role = MessageRole.MODEL, text = replyText)
                val newList = _messages.value + modelMessage
                _messages.value = newList
                prefs.saveChatHistory(newList)

                viewModelScope.launch {
                    try {
                        firestoreRepo.saveChatMessage(modelMessage)
                    } catch (e: Exception) {
                        Log.w("KayaraVM", "Failed to sync model reply to cloud: ${e.message}")
                    }
                }

                if (currentSettings.autoReadReplies) {
                    speechManager.speak(
                        text = replyText,
                        gender = currentSettings.gender,
                        languageCode = currentSettings.languageCode
                    )
                }

                // Periodic memory extraction
                turnCounter++
                if (turnCounter % 3 == 0) {
                    extractMemoriesSilently(newList)
                }
            }.onFailure { error ->
                val errorMsg = error.localizedMessage ?: "Connection error"
                val errorMessage = ChatMessage(
                    role = MessageRole.ERROR,
                    text = errorMsg
                )
                val newList = _messages.value + errorMessage
                _messages.value = newList
                prefs.saveChatHistory(newList)
            }
        }
    }

    private fun extractMemoriesSilently(currentMessages: List<ChatMessage>) {
        viewModelScope.launch {
            val extracted = apiService.extractMemories(
                recentMessages = currentMessages,
                settings = _settings.value,
                existingMemories = _memories.value
            )
            if (extracted.isNotEmpty()) {
                val newItems = extracted.map { MemoryItem(fact = it) }
                val updated = _memories.value + newItems
                _memories.value = updated
                prefs.saveMemories(updated)

                for (item in newItems) {
                    try {
                        firestoreRepo.saveMemory(item)
                    } catch (e: Exception) {
                        Log.w("KayaraVM", "Failed to sync extracted memory: ${e.message}")
                    }
                }
            }
        }
    }

    fun speakMessage(text: String) {
        speechManager.speak(
            text = text,
            gender = _settings.value.gender,
            languageCode = _settings.value.languageCode
        )
    }

    fun stopSpeaking() {
        speechManager.stopSpeaking()
    }

    fun toggleAutoRead() {
        val updated = _settings.value.copy(autoReadReplies = !_settings.value.autoReadReplies)
        _settings.value = updated
        prefs.saveSettings(updated)
    }

    fun startDictation() {
        speechManager.startListening(
            languageCode = _settings.value.languageCode,
            onResult = { recognizedText ->
                sendMessage(recognizedText)
            },
            onError = { err ->
                viewModelScope.launch {
                    _toastEvent.emit(err)
                }
            }
        )
    }

    fun stopDictation() {
        speechManager.stopListening()
    }

    fun saveSettings(newSettings: CompanionSettings) {
        _settings.value = newSettings
        prefs.saveSettings(newSettings)
        viewModelScope.launch {
            try {
                firestoreRepo.saveUserSettings(newSettings)
            } catch (e: Exception) {
                Log.w("KayaraVM", "Failed to sync settings to Firestore: ${e.message}")
            }
            _toastEvent.emit("Settings saved successfully")
        }
    }

    fun testApiKey(key: String, model: String) {
        viewModelScope.launch {
            val result = apiService.testConnection(key, model)
            result.onSuccess { msg ->
                _toastEvent.emit(msg)
            }.onFailure { err ->
                _toastEvent.emit("Test failed: ${err.localizedMessage}")
            }
        }
    }

    fun detectModels(apiKey: String = _settings.value.apiKey) {
        viewModelScope.launch {
            _isDetectingModels.value = true
            _modelDetectionStatus.value = "Connecting to Google Generative AI to scan available models..."
            val result = apiService.discoverAndCheckModels(apiKey)
            _isDetectingModels.value = false
            result.onSuccess { list ->
                _detectedModels.value = list
                val latency = list.firstOrNull()?.latencyMs ?: 0L
                _modelDetectionStatus.value = "Live connected with Google: ${list.size} models verified active ($latency ms)"
                _toastEvent.emit("Google Generative AI online: ${list.size} models detected ($latency ms)")
            }.onFailure { err ->
                _modelDetectionStatus.value = "Detection failed: ${err.localizedMessage}"
                _toastEvent.emit("Detection failed: ${err.localizedMessage}")
            }
        }
    }

    fun addManualMemory(fact: String, category: String = "General") {
        val trimmed = fact.trim()
        if (trimmed.isEmpty()) return
        val newItem = MemoryItem(fact = trimmed, category = category)
        val updated = _memories.value + newItem
        _memories.value = updated
        prefs.saveMemories(updated)
        viewModelScope.launch {
            try {
                firestoreRepo.saveMemory(newItem)
            } catch (e: Exception) {
                Log.w("KayaraVM", "Failed to sync manual memory: ${e.message}")
            }
        }
    }

    fun updateMemory(id: String, newFact: String, category: String = "General") {
        val trimmed = newFact.trim()
        if (trimmed.isEmpty()) return
        val existing = _memories.value.find { it.id == id }
        val updatedItem = (existing ?: MemoryItem(id = id, fact = trimmed)).copy(fact = trimmed, category = category)
        val updated = _memories.value.map { if (it.id == id) updatedItem else it }
        _memories.value = updated
        prefs.saveMemories(updated)
        viewModelScope.launch {
            try {
                firestoreRepo.saveMemory(updatedItem)
            } catch (e: Exception) {
                Log.w("KayaraVM", "Failed to update memory in Firestore: ${e.message}")
            }
        }
    }

    fun removeMemory(id: String) {
        val updated = _memories.value.filter { it.id != id }
        _memories.value = updated
        prefs.saveMemories(updated)
        viewModelScope.launch {
            try {
                firestoreRepo.deleteMemory(id)
            } catch (e: Exception) {
                Log.w("KayaraVM", "Failed to delete memory in Firestore: ${e.message}")
            }
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        prefs.clearChat()
        // Provide fresh greeting
        val intro = listOf(ChatMessage(role = MessageRole.MODEL, text = getIntroGreeting(_settings.value)))
        _messages.value = intro
        prefs.saveChatHistory(intro)
        viewModelScope.launch {
            try {
                firestoreRepo.clearChatMessages()
            } catch (e: Exception) {
                Log.w("KayaraVM", "Failed to clear cloud messages: ${e.message}")
            }
            _toastEvent.emit("Chat history cleared")
        }
    }

    fun clearMemories() {
        _memories.value = emptyList()
        prefs.clearMemories()
        viewModelScope.launch {
            try {
                firestoreRepo.clearMemories()
            } catch (e: Exception) {
                Log.w("KayaraVM", "Failed to clear cloud memories: ${e.message}")
            }
            _toastEvent.emit("All memories forgotten")
        }
    }

    // --- Live Voice Call Engine ---

    fun startLiveCall() {
        speechManager.stopSpeaking()
        speechManager.stopListening()

        _callState.value = CallUiState(
            isOpen = true,
            durationSeconds = 0,
            isMicMuted = false,
            isVoiceMuted = false,
            status = CallStatus.LISTENING,
            userCaption = "",
            companionCaption = "Listening to you..."
        )

        // Start call timer
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (isActive && _callState.value.isOpen) {
                delay(1000L)
                _callState.update { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }

        listenInLiveCall()
    }

    fun endLiveCall() {
        callTimerJob?.cancel()
        speechManager.stopSpeaking()
        speechManager.stopListening()
        _callState.value = CallUiState(isOpen = false)
    }

    fun toggleCallMic() {
        val newMuted = !_callState.value.isMicMuted
        _callState.update { it.copy(isMicMuted = newMuted) }
        if (newMuted) {
            speechManager.stopListening()
        } else {
            if (_callState.value.status == CallStatus.LISTENING) {
                listenInLiveCall()
            }
        }
    }

    fun toggleCallVoice() {
        val newVoiceMuted = !_callState.value.isVoiceMuted
        _callState.update { it.copy(isVoiceMuted = newVoiceMuted) }
        if (newVoiceMuted) {
            speechManager.stopSpeaking()
        }
    }

    fun interruptCallSpeaking() {
        speechManager.stopSpeaking()
        if (_callState.value.isOpen && !_callState.value.isMicMuted) {
            listenInLiveCall()
        }
    }

    private fun listenInLiveCall() {
        if (!_callState.value.isOpen || _callState.value.isMicMuted) return

        _callState.update {
            it.copy(
                status = CallStatus.LISTENING,
                companionCaption = if (it.companionCaption.isNotBlank()) it.companionCaption else "Listening..."
            )
        }

        speechManager.startListening(
            languageCode = _settings.value.languageCode,
            onResult = { userSpeech ->
                handleUserSpokenInCall(userSpeech)
            },
            onError = { _ ->
                // If timed out and still open, gently loop back to listening
                if (_callState.value.isOpen && !_callState.value.isMicMuted && _callState.value.status == CallStatus.LISTENING) {
                    viewModelScope.launch {
                        delay(600)
                        listenInLiveCall()
                    }
                }
            }
        )
    }

    private fun handleUserSpokenInCall(userSpeech: String) {
        if (!_callState.value.isOpen) return

        val userMessage = ChatMessage(role = MessageRole.USER, text = userSpeech)
        val updatedMessages = _messages.value + userMessage
        _messages.value = updatedMessages
        prefs.saveChatHistory(updatedMessages)

        viewModelScope.launch {
            try {
                firestoreRepo.saveChatMessage(userMessage)
            } catch (e: Exception) {
                Log.w("KayaraVM", "Failed to sync call user speech: ${e.message}")
            }
        }

        _callState.update {
            it.copy(
                status = CallStatus.THINKING,
                userCaption = "\"$userSpeech\"",
                companionCaption = "Thinking..."
            )
        }

        viewModelScope.launch {
            val result = apiService.generateReply(
                messages = updatedMessages,
                settings = _settings.value,
                memories = _memories.value
            )

            if (!_callState.value.isOpen) return@launch

            result.onSuccess { genResult ->
                val replyText = genResult.replyText
                if (genResult.wasShifted) {
                    val prev = genResult.originalModel
                    val shifted = genResult.modelUsed
                    val updated = _settings.value.copy(model = shifted)
                    _settings.value = updated
                    prefs.saveSettings(updated)
                    viewModelScope.launch {
                        try { firestoreRepo.saveUserSettings(updated) } catch (e: Exception) {}
                        _toastEvent.emit("Call shifted to '$shifted': '$prev' was unavailable.")
                    }
                }

                val modelMessage = ChatMessage(role = MessageRole.MODEL, text = replyText)
                val newList = _messages.value + modelMessage
                _messages.value = newList
                prefs.saveChatHistory(newList)

                viewModelScope.launch {
                    try {
                        firestoreRepo.saveChatMessage(modelMessage)
                    } catch (e: Exception) {
                        Log.w("KayaraVM", "Failed to sync call model speech: ${e.message}")
                    }
                }

                _callState.update {
                    it.copy(
                        status = CallStatus.SPEAKING,
                        companionCaption = replyText
                    )
                }

                if (!_callState.value.isVoiceMuted) {
                    speechManager.speak(
                        text = replyText,
                        gender = _settings.value.gender,
                        languageCode = _settings.value.languageCode,
                        onDone = {
                            if (_callState.value.isOpen) {
                                listenInLiveCall()
                            }
                        }
                    )
                } else {
                    // Voice is muted, simulate short pause then listen again
                    delay(2500)
                    if (_callState.value.isOpen) {
                        listenInLiveCall()
                    }
                }
            }.onFailure { err ->
                _callState.update {
                    it.copy(
                        status = CallStatus.LISTENING,
                        companionCaption = "I couldn't hear that clearly. (${err.localizedMessage})"
                    )
                }
                delay(2000)
                if (_callState.value.isOpen) {
                    listenInLiveCall()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        callTimerJob?.cancel()
        speechManager.shutdown()
    }
}
