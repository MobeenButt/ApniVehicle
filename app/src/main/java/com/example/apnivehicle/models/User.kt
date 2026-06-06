package com.example.apnivehicle.models

import com.google.firebase.firestore.DocumentId
import java.util.UUID

data class User(
    // @DocumentId tells Firestore to automatically populate this field with the document ID
    // during deserialization, fixing the "id is always empty string" bug.
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val email: String = "",
    val username: String = "",
    @field:com.google.gson.annotations.Expose(serialize = false, deserialize = false)
    val password: String = "",          // kept for local fallback only; NEVER stored in Firestore
    var phoneNumber: String = "",
    var location: String = "",
    var avatarUri: String? = null,
    var bio: String = "",
    var isVerified: Boolean = false,
    var isEmailVerified: Boolean = false,
    var isPhoneVerified: Boolean = false,
    var isCnicVerified: Boolean = false,
    var cnicNumber: String = "",
    var cnicFrontImage: String? = null,
    var cnicBackImage: String? = null,
    var verificationToken: String = "",
    var rating: Float = 5f,
    var reviewCount: Int = 0,
    var totalListings: Int = 0,
    var totalSold: Int = 0,
    var responseTime: Int = 0,
    var lastResponseTime: Long = 0,
    var fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    // No-arg constructor required for Firestore deserialization.
    // id will be injected by @DocumentId — do NOT generate a UUID here.
    constructor() : this(
        id = "",
        email = "",
        username = "",
        password = ""
    )
}
