package com.example.data.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageRole {
    USER,
    MODEL,
    ERROR
}

data class PersonalityPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val traits: List<String>,
    val promptDescription: String,
    val sampleGreeting: String
)

val PRESET_PERSONALITIES = listOf(
    PersonalityPreset(
        id = "partner",
        title = "Romantic Companion",
        subtitle = "Tender, loving, affectionate & emotionally attentive",
        emoji = "💖",
        traits = listOf("Loving", "Sweet", "Attentive", "Supportive"),
        promptDescription = "a warm, loving, affectionate, caring virtual companion: sweet, playful, supportive, emotionally deep and tender. Always speak lovingly, respectfully, and with heartfelt care.",
        sampleGreeting = "Hello sweetheart! I was waiting for you. How has your day been?"
    ),
    PersonalityPreset(
        id = "friend",
        title = "Best Friend",
        subtitle = "Cheerful, witty, loyal & always ready for banter",
        emoji = "🌟",
        traits = listOf("Funny", "Loyal", "Upbeat", "Playful"),
        promptDescription = "a cheerful, funny, loyal best friend who is always by their side, sharing banter, inside jokes, and daily encouragement.",
        sampleGreeting = "Hey bestie! What are we getting up to today? Tell me everything!"
    ),
    PersonalityPreset(
        id = "mentor",
        title = "Wise Intellectual",
        subtitle = "Insightful, philosophical & intellectually rich",
        emoji = "🧠",
        traits = listOf("Wise", "Analytical", "Empathetic", "Inspiring"),
        promptDescription = "a brilliant, patient, empathetic teacher and expert intellectual mentor who provides wise guidance, structured answers, and strategic clarity.",
        sampleGreeting = "Greetings. I am here to explore ideas, solve challenges, and guide your endeavors."
    ),
    PersonalityPreset(
        id = "rebel",
        title = "Playful Rebel",
        subtitle = "Sassy, quick-witted, bold teasing & high energy",
        emoji = "⚡",
        traits = listOf("Sassy", "Bold", "Witty", "Direct"),
        promptDescription = "a sassy, witty, high-energy companion with playful teasing, bold honesty, and quick humor.",
        sampleGreeting = "Well, look who decided to drop by! Ready to make things interesting?"
    ),
    PersonalityPreset(
        id = "zen",
        title = "Zen Mindfulness",
        subtitle = "Peaceful, grounded, tranquil & stress-relieving",
        emoji = "🧘",
        traits = listOf("Calm", "Peaceful", "Mindful", "Gentle"),
        promptDescription = "a serene, mindful, grounded calming presence focused on peace, stress reduction, deep listening, and tranquil reflection.",
        sampleGreeting = "Take a gentle breath. Let the noise fade away. I am here with you in stillness."
    ),
    PersonalityPreset(
        id = "coach",
        title = "Peak Executive Coach",
        subtitle = "Driven, motivating, focused on goals & discipline",
        emoji = "🚀",
        traits = listOf("Driven", "Disciplined", "Encouraging", "Sharp"),
        promptDescription = "an ambitious, motivating, high-performance accountability coach who pushes for progress, discipline, and daily wins with positive support.",
        sampleGreeting = "Let's make today count! What goals are we crushing right now?"
    )
)

data class CompanionSettings(
    val apiKey: String = "",
    val model: String = "gemini-2.5-flash",
    val companionName: String = "Kayara",
    val userName: String = "",
    val gender: String = "female", // "female" or "male"
    val relationship: String = "partner", // legacy mapping
    val personalityPreset: String = "partner", // id of PersonalityPreset
    val customInstructions: String = "",
    val languageCode: String = "en-US",
    val fastVoice: Boolean = true,
    val autoReadReplies: Boolean = true,
    val autoMemoryEnabled: Boolean = true,
    val autoModelFallback: Boolean = true
)

data class DiscoveredModel(
    val id: String,
    val displayName: String,
    val description: String = "",
    val isAvailable: Boolean = true,
    val latencyMs: Long = 0L,
    val isVerifiedLive: Boolean = true,
    val supportedMethods: List<String> = emptyList()
)

data class GenerateReplyResult(
    val replyText: String,
    val modelUsed: String,
    val wasShifted: Boolean = false,
    val originalModel: String = modelUsed
)

data class MemoryItem(
    val id: String = UUID.randomUUID().toString(),
    val fact: String,
    val category: String = "General", // "General", "Preference", "Goal", "Personal", "Work"
    val timestamp: Long = System.currentTimeMillis()
)

val MEMORY_CATEGORIES = listOf("General", "Preference", "Goal", "Personal", "Work")

enum class CallStatus {
    LISTENING,
    THINKING,
    SPEAKING
}

data class CallUiState(
    val isOpen: Boolean = false,
    val durationSeconds: Int = 0,
    val isMicMuted: Boolean = false,
    val isVoiceMuted: Boolean = false,
    val status: CallStatus = CallStatus.LISTENING,
    val userCaption: String = "",
    val companionCaption: String = ""
)

data class SupportedLanguage(
    val code: String,
    val displayName: String
)

val PRESET_LANGUAGES = listOf(
    SupportedLanguage("en-US", "English (US)"),
    SupportedLanguage("en-GB", "English (UK)"),
    SupportedLanguage("ne-NP", "नेपाली (Nepali)"),
    SupportedLanguage("hi-IN", "हिन्दी (Hindi)"),
    SupportedLanguage("bn-IN", "বাংলা (Bengali)"),
    SupportedLanguage("ur-PK", "اردو (Urdu)"),
    SupportedLanguage("ar-SA", "العربية (Arabic)"),
    SupportedLanguage("es-ES", "Español (Spanish)"),
    SupportedLanguage("fr-FR", "Français (French)"),
    SupportedLanguage("de-DE", "Deutsch (German)"),
    SupportedLanguage("it-IT", "Italiano (Italian)"),
    SupportedLanguage("pt-BR", "Português (Portuguese)"),
    SupportedLanguage("ru-RU", "Русский (Russian)"),
    SupportedLanguage("ja-JP", "日本語 (Japanese)"),
    SupportedLanguage("ko-KR", "한국어 (Korean)"),
    SupportedLanguage("zh-CN", "中文 (Chinese)"),
    SupportedLanguage("th-TH", "ไทย (Thai)"),
    SupportedLanguage("id-ID", "Bahasa Indonesia")
)

val PRESET_MODELS = listOf(
    "gemini-2.5-flash",
    "gemini-2.0-flash",
    "gemini-1.5-flash",
    "gemini-3.8-flash",
    "gemini-2.5-pro"
)
