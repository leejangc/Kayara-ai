package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.CompanionSettings
import com.example.data.model.MemoryItem
import com.example.data.model.MessageRole
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun saveUserSettings(settings: CompanionSettings) {
        val uid = requireUserId()
        val path = "users/$uid"
        val docRef = db.collection("users").document(uid)
        val data = hashMapOf(
            "userId" to uid,
            "companionName" to settings.companionName,
            "userName" to settings.userName,
            "relationship" to settings.relationship,
            "personalityPreset" to settings.personalityPreset,
            "customInstructions" to settings.customInstructions,
            "gender" to settings.gender,
            "languageCode" to settings.languageCode,
            "autoReadReplies" to settings.autoReadReplies,
            "autoMemoryEnabled" to settings.autoMemoryEnabled,
            "autoModelFallback" to settings.autoModelFallback,
            "model" to settings.model,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    fun observeUserSettings(uid: String): Flow<CompanionSettings?> = flow {
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        val preset = snapshot.getString("personalityPreset")
                            ?: snapshot.getString("relationship")
                            ?: "partner"
                        CompanionSettings(
                            apiKey = "", // API key remains device-local or user secret
                            model = snapshot.getString("model") ?: "gemini-2.5-flash",
                            companionName = snapshot.getString("companionName") ?: "Kayara",
                            userName = snapshot.getString("userName") ?: "",
                            gender = snapshot.getString("gender") ?: "female",
                            relationship = snapshot.getString("relationship") ?: preset,
                            personalityPreset = preset,
                            customInstructions = snapshot.getString("customInstructions") ?: "",
                            languageCode = snapshot.getString("languageCode") ?: "en-US",
                            fastVoice = true,
                            autoReadReplies = snapshot.getBoolean("autoReadReplies") ?: true,
                            autoMemoryEnabled = snapshot.getBoolean("autoMemoryEnabled") ?: true,
                            autoModelFallback = snapshot.getBoolean("autoModelFallback") ?: true
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun saveChatMessage(message: ChatMessage) {
        val uid = requireUserId()
        val path = "users/$uid/messages/${message.id}"
        val docRef = db.collection("users").document(uid).collection("messages").document(message.id)
        val data = hashMapOf(
            "id" to message.id,
            "userId" to uid,
            "role" to message.role.name.lowercase(),
            "text" to message.text,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    fun observeChatMessages(uid: String): Flow<List<ChatMessage>> = flow {
        val path = "users/$uid/messages"
        emitAll(
            db.collection("users").document(uid).collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        val id = doc.getString("id") ?: doc.id
                        val roleStr = doc.getString("role")?.uppercase() ?: "MODEL"
                        val text = doc.getString("text") ?: ""
                        val role = try {
                            MessageRole.valueOf(roleStr)
                        } catch (e: Exception) {
                            MessageRole.MODEL
                        }
                        ChatMessage(id = id, role = role, text = text)
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveMemory(memory: MemoryItem) {
        val uid = requireUserId()
        val path = "users/$uid/memories/${memory.id}"
        val docRef = db.collection("users").document(uid).collection("memories").document(memory.id)
        val data = hashMapOf(
            "id" to memory.id,
            "userId" to uid,
            "fact" to memory.fact,
            "category" to memory.category,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(data).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun deleteMemory(memoryId: String) {
        val uid = requireUserId()
        val path = "users/$uid/memories/$memoryId"
        try {
            db.collection("users").document(uid).collection("memories").document(memoryId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    fun observeMemories(uid: String): Flow<List<MemoryItem>> = flow {
        val path = "users/$uid/memories"
        emitAll(
            db.collection("users").document(uid).collection("memories")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        val id = doc.getString("id") ?: doc.id
                        val fact = doc.getString("fact") ?: ""
                        val category = doc.getString("category") ?: "General"
                        if (fact.isNotBlank()) {
                            MemoryItem(id = id, fact = fact, category = category)
                        } else null
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun clearChatMessages() {
        val uid = requireUserId()
        val path = "users/$uid/messages"
        try {
            val snapshot = db.collection("users").document(uid).collection("messages").get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    suspend fun clearMemories() {
        val uid = requireUserId()
        val path = "users/$uid/memories"
        try {
            val snapshot = db.collection("users").document(uid).collection("memories").get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    private suspend fun <T> kotlinx.coroutines.flow.FlowCollector<T>.emitAll(source: Flow<T>) {
        source.collect { emit(it) }
    }
}
