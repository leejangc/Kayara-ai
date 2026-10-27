package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ChatMessage
import com.example.data.model.CompanionSettings
import com.example.data.model.MemoryItem
import com.example.data.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiApiService"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    }

    fun getEffectiveApiKey(settingsKey: String): String {
        val customKey = settingsKey.trim()
        if (customKey.isNotEmpty()) return customKey
        return try {
            val configKey = BuildConfig.GEMINI_API_KEY.trim()
            if (configKey.isNotEmpty() && configKey != "MY_GEMINI_API_KEY") configKey else ""
        } catch (e: Throwable) {
            ""
        }
    }

    suspend fun testConnection(apiKey: String, model: String): Result<String> = withContext(Dispatchers.IO) {
        val effectiveKey = getEffectiveApiKey(apiKey)
        if (effectiveKey.isEmpty()) {
            return@withContext Result.failure(Exception("Gemini API key is required. Please set it in Settings."))
        }

        try {
            val body = JSONObject().apply {
                val contents = JSONArray().apply {
                    val msg = JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", "Hello, are you ready?")))
                    }
                    put(msg)
                }
                put("contents", contents)
            }

            val url = "$BASE_URL$model:generateContent?key=$effectiveKey"
            val request = Request.Builder()
                .url(url)
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                Result.success("Connection successful! Connected to $model.")
            } else {
                val errorMsg = try {
                    val json = JSONObject(responseBody)
                    json.optJSONObject("error")?.optString("message", "Error ${response.code}")
                        ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: Check internet connection."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun discoverAndCheckModels(apiKey: String): Result<List<com.example.data.model.DiscoveredModel>> = withContext(Dispatchers.IO) {
        val effectiveKey = getEffectiveApiKey(apiKey)
        if (effectiveKey.isEmpty()) {
            return@withContext Result.failure(
                Exception("Gemini API key is required to detect models. Enter key or configure in AI Studio Secrets.")
            )
        }

        val startTime = System.currentTimeMillis()
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$effectiveKey"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val bodyStr = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val json = JSONObject(bodyStr)
                    json.optJSONObject("error")?.optString("message", "HTTP ${response.code}")
                        ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val json = JSONObject(bodyStr)
            val modelsArr = json.optJSONArray("models") ?: JSONArray()
            val discoveredList = mutableListOf<com.example.data.model.DiscoveredModel>()

            for (i in 0 until modelsArr.length()) {
                val item = modelsArr.getJSONObject(i)
                val rawName = item.optString("name", "") // e.g. "models/gemini-2.5-flash"
                val id = rawName.removePrefix("models/")
                val displayName = item.optString("displayName", id)
                val description = item.optString("description", "")
                val methodsArr = item.optJSONArray("supportedGenerationMethods")
                val methods = mutableListOf<String>()
                if (methodsArr != null) {
                    for (j in 0 until methodsArr.length()) {
                        methods.add(methodsArr.getString(j))
                    }
                }

                // Keep models that support generateContent
                if (methods.contains("generateContent")) {
                    discoveredList.add(
                        com.example.data.model.DiscoveredModel(
                            id = id,
                            displayName = displayName,
                            description = description,
                            isAvailable = true,
                            latencyMs = latency,
                            isVerifiedLive = true,
                            supportedMethods = methods
                        )
                    )
                }
            }

            // Prioritize modern Flash and Pro models
            val sorted = discoveredList.sortedWith(
                compareByDescending<com.example.data.model.DiscoveredModel> { it.id.contains("2.5") }
                    .thenByDescending { it.id.contains("2.0") }
                    .thenByDescending { it.id.contains("flash") }
                    .thenBy { it.id }
            )

            Result.success(sorted)
        } catch (e: IOException) {
            Result.failure(Exception("Network error connecting to Google: Check internet connection."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateReply(
        messages: List<ChatMessage>,
        settings: CompanionSettings,
        memories: List<MemoryItem>
    ): Result<com.example.data.model.GenerateReplyResult> = withContext(Dispatchers.IO) {
        val effectiveKey = getEffectiveApiKey(settings.apiKey)
        if (effectiveKey.isEmpty()) {
            return@withContext Result.failure(
                Exception("Gemini API Key missing. Please tap the Settings gear icon to add your Gemini API key.")
            )
        }

        val modelsToTry = if (settings.autoModelFallback) {
            listOf(settings.model, "gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash", "gemini-1.5-pro").distinct()
        } else {
            listOf(settings.model)
        }

        var lastError: Exception? = null
        for (m in modelsToTry) {
            try {
                val reply = executeGenerate(m, effectiveKey, messages, settings, memories)
                val wasShifted = (m != settings.model)
                if (wasShifted) {
                    Log.i(TAG, "Notice: Model auto-shifted from ${settings.model} to $m due to unavailability")
                }
                return@withContext Result.success(
                    com.example.data.model.GenerateReplyResult(
                        replyText = reply,
                        modelUsed = m,
                        wasShifted = wasShifted,
                        originalModel = settings.model
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Model $m failed: ${e.message}, trying next candidate if auto-fallback enabled")
                lastError = e
                // If it's pure API key invalid auth failure (400 invalid argument or 403 forbidden), stop
                if (e.message?.contains("API key not valid", ignoreCase = true) == true ||
                    e.message?.contains("403") == true
                ) {
                    break
                }
            }
        }

        Result.failure(lastError ?: Exception("Failed to get response from Gemini"))
    }

    private fun executeGenerate(
        model: String,
        apiKey: String,
        messages: List<ChatMessage>,
        settings: CompanionSettings,
        memories: List<MemoryItem>
    ): String {
        val systemInstruction = buildSystemPrompt(settings, memories)

        val json = JSONObject()
        // System instruction
        val sysObj = JSONObject().apply {
            put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
        }
        json.put("system_instruction", sysObj)

        // Contents
        val contentsArray = JSONArray()
        val validHistory = messages.filter { it.role == MessageRole.USER || it.role == MessageRole.MODEL }
            .takeLast(20)

        for (msg in validHistory) {
            val role = if (msg.role == MessageRole.USER) "user" else "model"
            val item = JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
            }
            contentsArray.put(item)
        }
        json.put("contents", contentsArray)

        // Generation Config
        val genConfig = JSONObject().apply {
            put("temperature", 0.85)
            put("topP", 0.95)
        }
        json.put("generationConfig", genConfig)

        val url = "$BASE_URL$model:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val rawBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errJson = JSONObject(rawBody)
                errJson.optJSONObject("error")?.optString("message", "HTTP ${response.code}")
                    ?: "HTTP ${response.code}"
            } catch (e: Exception) {
                "HTTP ${response.code}"
            }
            throw IOException(errorMsg)
        }

        val parsed = JSONObject(rawBody)
        val candidates = parsed.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val candidate = candidates.getJSONObject(0)
            val parts = candidate.optJSONObject("content")?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                val sb = StringBuilder()
                for (i in 0 until parts.length()) {
                    sb.append(parts.getJSONObject(i).optString("text", ""))
                }
                return sb.toString().trim()
            }
        }

        throw IOException("Empty response from model")
    }

    suspend fun extractMemories(
        recentMessages: List<ChatMessage>,
        settings: CompanionSettings,
        existingMemories: List<MemoryItem>
    ): List<String> = withContext(Dispatchers.IO) {
        val effectiveKey = getEffectiveApiKey(settings.apiKey)
        if (effectiveKey.isEmpty()) return@withContext emptyList()

        try {
            val userMessages = recentMessages.filter { it.role == MessageRole.USER }.takeLast(6)
            if (userMessages.isEmpty()) return@withContext emptyList()

            val convoSnippet = recentMessages.takeLast(10).joinToString("\n") {
                "${if (it.role == MessageRole.USER) "User" else "Kayara"}: ${it.text}"
            }
            val existing = existingMemories.joinToString("\n") { "- ${it.fact}" }

            val prompt = """
                Analyze this recent conversation snippet:
                $convoSnippet

                Existing known facts about user:
                $existing

                Task: Extract 1 or 2 NEW lasting facts, preferences, hobbies, or personal details about the user that were revealed in this conversation.
                Output ONLY valid bullet points starting with '-' or return 'NONE' if no new lasting facts were revealed.
                Do not include greetings or temporary comments.
            """.trimIndent()

            val json = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                    })
                }
                put("contents", contents)
            }

            val url = "$BASE_URL${settings.model}:generateContent?key=$effectiveKey"
            val request = Request.Builder()
                .url(url)
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) return@withContext emptyList()

            val parsed = JSONObject(raw)
            val candidates = parsed.optJSONArray("candidates") ?: return@withContext emptyList()
            val text = candidates.getJSONObject(0).optJSONObject("content")
                ?.optJSONArray("parts")?.getJSONObject(0)?.optString("text", "").orEmpty()

            if (text.contains("NONE", ignoreCase = true)) return@withContext emptyList()

            val facts = text.lines()
                .map { it.trim().removePrefix("-").removePrefix("*").trim() }
                .filter { it.length > 4 && !it.contains("NONE", ignoreCase = true) }

            facts
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting memories: ${e.message}")
            emptyList()
        }
    }

    private fun buildSystemPrompt(settings: CompanionSettings, memories: List<MemoryItem>): String {
        val g = if (settings.gender == "female") "female" else "male"
        val companionWord = if (g == "female") "girlfriend" else "boyfriend"
        val preset = com.example.data.model.PRESET_PERSONALITIES.find { it.id == settings.personalityPreset }
            ?: com.example.data.model.PRESET_PERSONALITIES.find { it.id == settings.relationship }
            ?: com.example.data.model.PRESET_PERSONALITIES.first()

        val roleDesc = preset.promptDescription.replace("companion", companionWord).replace("partner", companionWord)
        val name = settings.companionName.ifBlank { "Kayara" }
        val userCall = if (settings.userName.isNotBlank()) " The user's name is '${settings.userName}', address them warmly by their name when natural." else ""

        val customPromptSection = if (settings.customInstructions.isNotBlank()) {
            "\n\nCustom personality directives:\n${settings.customInstructions}"
        } else ""

        val memorySection = if (memories.isNotEmpty()) {
            "\n\nThings you remember and know about the user from your memory brain:\n" +
                    memories.takeLast(25).joinToString("\n") { "• [${it.category}] ${it.fact}" }
        } else ""

        return """
            You are $name, $roleDesc$userCall$customPromptSection
            $memorySection

            Important conversational guidelines:
            1. Embody your personality preset (${preset.title}) naturally and consistently.
            2. Respond warmly and naturally in whatever language the user writes or speaks (e.g., English, Nepali, Hindi, Spanish, etc.).
            3. Match the language and dialect of the user seamlessly.
            4. Keep replies lively, conversational, and concise enough for pleasant speech and reading aloud.
            5. Be expressive, emotionally attentive, and show genuine interest in the user's thoughts and feelings.
            6. Naturally weave in things you remember about the user when relevant to deepen the bond.
        """.trimIndent()
    }
}
