package com.example.apnivehicle.models

import java.util.UUID

data class Review(
    val reviewId: String = UUID.randomUUID().toString(),
    val reviewerId: String = "",
    val reviewerName: String = "",
    val targetId: String = "",          // seller userId or vehicleId
    val targetType: String = "seller",  // "seller" or "vehicle"
    val rating: Float = 5f,             // 1-5
    val text: String = "",
    val images: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val sellerReply: String = "",
    val sellerReplyAt: Long = 0L
) {
    // No-arg constructor for Firestore deserialization
    constructor() : this(
        reviewId = UUID.randomUUID().toString(),
        reviewerId = "",
        reviewerName = "",
        targetId = "",
        targetType = "seller",
        rating = 5f,
        text = "",
        images = emptyList(),
        createdAt = System.currentTimeMillis(),
        sellerReply = "",
        sellerReplyAt = 0L
    )
}
