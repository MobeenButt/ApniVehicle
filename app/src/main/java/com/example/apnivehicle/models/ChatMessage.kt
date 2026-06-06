package com.example.apnivehicle.models

import java.util.UUID

data class ChatMessage(
    val messageId: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "text"   // "text" or "image"
) {
    // Fix: no-arg constructor for Firestore deserialization must NOT generate a new UUID,
    // otherwise every deserialized message gets a different id than what's stored in Firestore.
    constructor() : this(
        messageId = "",
        senderId = "",
        text = "",
        timestamp = 0L,
        type = "text"
    )
}

data class Chat(
    val chatId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val participants: List<String> = emptyList(),
    val vehicleId: String = "",
    val vehicleTitle: String = "",
    val vehicleBrand: String = "",
    val vehicleModel: String = "",
    val vehicleYear: Int = 0,
    val vehiclePrice: Long = 0L,
    val buyerName: String = "",
    val sellerName: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageSenderName: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Map<String, Int> = emptyMap()
) {
    // Fix: stable no-arg constructor for Firestore deserialization
    constructor() : this(
        chatId = "",
        buyerId = "",
        sellerId = "",
        participants = emptyList(),
        vehicleId = "",
        vehicleTitle = "",
        vehicleBrand = "",
        vehicleModel = "",
        vehicleYear = 0,
        vehiclePrice = 0L,
        buyerName = "",
        sellerName = "",
        lastMessageSenderId = "",
        lastMessageSenderName = "",
        lastMessage = "",
        lastMessageTimestamp = 0L,
        unreadCount = emptyMap()
    )
}
