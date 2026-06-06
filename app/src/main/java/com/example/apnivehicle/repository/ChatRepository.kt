package com.example.apnivehicle.repository

import android.util.Log
import com.example.apnivehicle.models.Vehicle
import com.example.apnivehicle.models.Chat
import com.example.apnivehicle.models.ChatMessage
import com.example.apnivehicle.repository.VehicleRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * ChatRepository — real-time Firestore chat using chats/{chatId}/messages subcollection.
 */
object ChatRepository {

    private const val TAG = "ChatRepository"
    private const val COLLECTION_CHATS = "chats"
    private const val COLLECTION_MESSAGES = "messages"

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    /** Generate a deterministic chatId from two user IDs + vehicleId */
    fun buildChatId(userId1: String, userId2: String, vehicleId: String): String {
        val sorted = listOf(userId1, userId2).sorted()
        return "${sorted[0]}_${sorted[1]}_$vehicleId"
    }

    /** Create or get existing chat document */
    suspend fun getOrCreateChat(
        currentUserId: String,
        sellerId: String,
        vehicleId: String,
        vehicleTitle: String
    ): Result<Chat> {
        return try {
            val chatId = buildChatId(currentUserId, sellerId, vehicleId)
            val docRef = db.collection(COLLECTION_CHATS).document(chatId)
            val doc = docRef.get().await()
            val currentUser = AuthRepository.getCurrentUser()
            val sellerUser = AuthRepository.getUserById(sellerId)
            val vehicle = VehicleRepository.getVehicleById(vehicleId)
            if (doc.exists()) {
                val existing = doc.toObject(Chat::class.java)
                val chat = if (existing == null) {
                    Chat(chatId = chatId)
                } else {
                    // Repair malformed older docs where chatId/participants could be missing.
                    val repaired = existing.copy(
                        chatId = existing.chatId.ifBlank { chatId },
                        buyerId = existing.buyerId.ifBlank { currentUserId },
                        sellerId = existing.sellerId.ifBlank { sellerId },
                        participants = if (existing.participants.isEmpty()) listOf(currentUserId, sellerId) else existing.participants,
                        vehicleId = existing.vehicleId.ifBlank { vehicleId },
                        vehicleTitle = existing.vehicleTitle.ifBlank { vehicleTitle },
                        vehicleBrand = existing.vehicleBrand.ifBlank { vehicle?.brand.orEmpty() },
                        vehicleModel = existing.vehicleModel.ifBlank { vehicle?.model.orEmpty() },
                        vehicleYear = if (existing.vehicleYear == 0) vehicle?.year ?: 0 else existing.vehicleYear,
                        vehiclePrice = if (existing.vehiclePrice == 0L) vehicle?.price ?: 0L else existing.vehiclePrice,
                        buyerName = existing.buyerName.ifBlank { currentUser?.username ?: "Buyer" },
                        sellerName = existing.sellerName.ifBlank { sellerUser?.username ?: "Seller" }
                    )
                    if (repaired != existing) {
                        docRef.set(repaired, SetOptions.merge()).await()
                    }
                    repaired
                }
                Result.success(chat)
            } else {
                val chat = Chat(
                    chatId = chatId,
                    buyerId = currentUserId,
                    sellerId = sellerId,
                    participants = listOf(currentUserId, sellerId),
                    vehicleId = vehicleId,
                    vehicleTitle = vehicleTitle,
                    vehicleBrand = vehicle?.brand.orEmpty(),
                    vehicleModel = vehicle?.model.orEmpty(),
                    vehicleYear = vehicle?.year ?: 0,
                    vehiclePrice = vehicle?.price ?: 0L,
                    buyerName = currentUser?.username ?: "Buyer",
                    sellerName = sellerUser?.username ?: "Seller",
                    lastMessage = "",
                    lastMessageSenderId = "",
                    lastMessageSenderName = "",
                    lastMessageTimestamp = System.currentTimeMillis(),
                    unreadCount = mapOf(currentUserId to 0, sellerId to 0)
                )
                docRef.set(chat).await()
                Result.success(chat)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getOrCreateChat failed", e)
            Result.failure(e)
        }
    }

    /** Send a message */
    suspend fun sendMessage(chatId: String, message: ChatMessage, recipientId: String): Result<Unit> {
        return try {
            val chatRef = db.collection(COLLECTION_CHATS).document(chatId)
            val msgRef = chatRef.collection(COLLECTION_MESSAGES).document(message.messageId)
            val currentUser = AuthRepository.getCurrentUser()

            // Write message
            msgRef.set(message).await()

            // Update chat metadata
            val chatDoc = chatRef.get().await()
            val currentUnread = chatDoc.toObject(Chat::class.java)?.unreadCount?.toMutableMap() ?: mutableMapOf()
            currentUnread[recipientId] = (currentUnread[recipientId] ?: 0) + 1

            chatRef.update(
                mapOf(
                    "lastMessage" to message.text,
                    "lastMessageSenderId" to message.senderId,
                    "lastMessageSenderName" to (currentUser?.username ?: ""),
                    "lastMessageTimestamp" to message.timestamp,
                    "unreadCount" to currentUnread
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage failed", e)
            Result.failure(e)
        }
    }

    /** Listen to messages in a chat (real-time) */
    fun listenToMessages(
        chatId: String,
        onMessages: (List<ChatMessage>) -> Unit
    ): ListenerRegistration {
        return try {
            // Safety check: chatId should not be empty
            if (chatId.isBlank()) {
                Log.w(TAG, "listenToMessages called with empty chatId")
                return object : ListenerRegistration {
                    override fun remove() {}
                }
            }

            db.collection(COLLECTION_CHATS).document(chatId)
                .collection(COLLECTION_MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    try {
                        if (error != null) {
                            Log.w(TAG, "listenToMessages error: ${error.message}", error)
                            onMessages(emptyList())  // Return empty list on error
                            return@addSnapshotListener
                        }

                        // Safety: handle null snapshot
                        val messages = if (snapshot != null && !snapshot.isEmpty) {
                            snapshot.documents.mapNotNull { doc ->
                                try {
                                    doc.toObject(ChatMessage::class.java)
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error parsing message", e)
                                    null
                                }
                            }
                        } else {
                            emptyList()
                        }
                        onMessages(messages)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in listenToMessages snapshot handler", e)
                        onMessages(emptyList())
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "listenToMessages setup failed", e)
            // Return a no-op listener
            return object : ListenerRegistration {
                override fun remove() {}
            }
        }
    }

    /** Listen to all chats for a user (real-time) */
    fun listenToUserChats(
        userId: String,
        onChats: (List<Chat>) -> Unit
    ): ListenerRegistration {
        return try {
            if (userId.isBlank()) {
                Log.w(TAG, "listenToUserChats called with empty userId")
                return object : ListenerRegistration { override fun remove() {} }
            }

            // IMPORTANT: Do NOT combine whereArrayContains + orderBy in the same query.
            // That compound query requires a composite Firestore index which must be manually
            // created in Firebase Console — without it Firestore returns FAILED_PRECONDITION
            // and the inbox shows empty even though chats exist.
            // Solution: query by participants only, then sort client-side.
            db.collection(COLLECTION_CHATS)
                .whereArrayContains("participants", userId)
                .addSnapshotListener { snapshot, error ->
                    try {
                        if (error != null) {
                            Log.e(TAG, "listenToUserChats error: ${error.message}", error)
                            onChats(emptyList())
                            return@addSnapshotListener
                        }

                        val chats = if (snapshot != null && !snapshot.isEmpty) {
                            snapshot.documents.mapNotNull { doc ->
                                try {
                                    doc.toObject(Chat::class.java)
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error parsing chat doc ${doc.id}", e)
                                    null
                                }
                            }
                            // Sort client-side: newest conversation first
                            .sortedByDescending { it.lastMessageTimestamp }
                        } else {
                            emptyList()
                        }

                        Log.d(TAG, "listenToUserChats: got ${chats.size} chats for $userId")
                        onChats(chats)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in listenToUserChats snapshot handler", e)
                        onChats(emptyList())
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "listenToUserChats setup failed", e)
            object : ListenerRegistration { override fun remove() {} }
        }
    }

    /** Mark messages as read for a user */
    suspend fun markAsRead(chatId: String, userId: String) {
        try {
            val chatRef = db.collection(COLLECTION_CHATS).document(chatId)
            val chatDoc = chatRef.get().await()
            val currentUnread = chatDoc.toObject(Chat::class.java)?.unreadCount?.toMutableMap() ?: mutableMapOf()
            currentUnread[userId] = 0
            chatRef.update("unreadCount", currentUnread).await()
        } catch (e: Exception) {
            Log.e(TAG, "markAsRead failed", e)
        }
    }

    /** Get total unread count for a user across all chats */
    fun listenToUnreadCount(userId: String, onCount: (Int) -> Unit): ListenerRegistration {
        // Same rule: no orderBy alongside whereArrayContains — query only, sort/aggregate client-side
        return db.collection(COLLECTION_CHATS)
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "listenToUnreadCount error", error)
                    return@addSnapshotListener
                }
                val total = snapshot?.documents?.sumOf { doc ->
                    val chat = doc.toObject(Chat::class.java)
                    chat?.unreadCount?.get(userId) ?: 0
                } ?: 0
                onCount(total)
            }
    }
}
