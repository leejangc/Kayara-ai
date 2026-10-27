package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ChatMessage
import com.example.data.model.CompanionSettings
import com.example.data.model.MemoryItem
import com.example.data.model.MessageRole
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kayara_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "model"
        private const val KEY_NAME = "companion_name"
        private const val KEY_USER = "user_name"
        private const val KEY_GENDER = "gender"
        private const val KEY_RELATIONSHIP = "relationship"
        private const val KEY_PERSONALITY_PRESET = "personality_preset"
        private const val KEY_CUSTOM_INSTRUCTIONS = "custom_instructions"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_FAST_VOICE = "fast_voice"
        private const val KEY_AUTO_READ = "auto_read"
        private const val KEY_AUTO_MEMORY = "auto_memory"
        private const val KEY_AUTO_MODEL_FALLBACK = "auto_model_fallback"
        private const val KEY_CHAT_HISTORY = "chat_history"
        private const val KEY_MEMORIES = "memories"
    }

    fun loadSettings(): CompanionSettings {
        return CompanionSettings(
            apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            model = prefs.getString(KEY_MODEL, "gemini-2.5-flash") ?: "gemini-2.5-flash",
            companionName = prefs.getString(KEY_NAME, "Kayara") ?: "Kayara",
            userName = prefs.getString(KEY_USER, "") ?: "",
            gender = prefs.getString(KEY_GENDER, "female") ?: "female",
            relationship = prefs.getString(KEY_RELATIONSHIP, "partner") ?: "partner",
            personalityPreset = prefs.getString(KEY_PERSONALITY_PRESET, "partner") ?: "partner",
            customInstructions = prefs.getString(KEY_CUSTOM_INSTRUCTIONS, "") ?: "",
            languageCode = prefs.getString(KEY_LANGUAGE, "en-US") ?: "en-US",
            fastVoice = prefs.getBoolean(KEY_FAST_VOICE, true),
            autoReadReplies = prefs.getBoolean(KEY_AUTO_READ, true),
            autoMemoryEnabled = prefs.getBoolean(KEY_AUTO_MEMORY, true),
            autoModelFallback = prefs.getBoolean(KEY_AUTO_MODEL_FALLBACK, true)
        )
    }

    fun saveSettings(settings: CompanionSettings) {
        prefs.edit()
            .putString(KEY_API_KEY, settings.apiKey)
            .putString(KEY_MODEL, settings.model)
            .putString(KEY_NAME, settings.companionName)
            .putString(KEY_USER, settings.userName)
            .putString(KEY_GENDER, settings.gender)
            .putString(KEY_RELATIONSHIP, settings.relationship)
            .putString(KEY_PERSONALITY_PRESET, settings.personalityPreset)
            .putString(KEY_CUSTOM_INSTRUCTIONS, settings.customInstructions)
            .putString(KEY_LANGUAGE, settings.languageCode)
            .putBoolean(KEY_FAST_VOICE, settings.fastVoice)
            .putBoolean(KEY_AUTO_READ, settings.autoReadReplies)
            .putBoolean(KEY_AUTO_MEMORY, settings.autoMemoryEnabled)
            .putBoolean(KEY_AUTO_MODEL_FALLBACK, settings.autoModelFallback)
            .apply()
    }

    fun loadChatHistory(): List<ChatMessage> {
        val raw = prefs.getString(KEY_CHAT_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<ChatMessage>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "")
                val roleStr = obj.optString("role", "MODEL")
                val text = obj.optString("text", "")
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                val role = try {
                    MessageRole.valueOf(roleStr)
                } catch (e: Exception) {
                    MessageRole.MODEL
                }
                list.add(ChatMessage(id = id, role = role, text = text, timestamp = timestamp))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveChatHistory(messages: List<ChatMessage>) {
        val limited = messages.takeLast(100)
        val array = JSONArray()
        for (msg in limited) {
            val obj = JSONObject()
            obj.put("id", msg.id)
            obj.put("role", msg.role.name)
            obj.put("text", msg.text)
            obj.put("timestamp", msg.timestamp)
            array.put(obj)
        }
        prefs.edit().putString(KEY_CHAT_HISTORY, array.toString()).apply()
    }

    fun loadMemories(): List<MemoryItem> {
        val raw = prefs.getString(KEY_MEMORIES, null) ?: return emptyList()
        val list = mutableListOf<MemoryItem>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "")
                val fact = obj.optString("fact", "")
                val category = obj.optString("category", "General")
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                if (fact.isNotBlank()) {
                    list.add(MemoryItem(id = id, fact = fact, category = category, timestamp = timestamp))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveMemories(memories: List<MemoryItem>) {
        val array = JSONArray()
        for (m in memories) {
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("fact", m.fact)
            obj.put("category", m.category)
            obj.put("timestamp", m.timestamp)
            array.put(obj)
        }
        prefs.edit().putString(KEY_MEMORIES, array.toString()).apply()
    }

    fun clearChat() {
        prefs.edit().remove(KEY_CHAT_HISTORY).apply()
    }

    fun clearMemories() {
        prefs.edit().remove(KEY_MEMORIES).apply()
    }
}
