package com.example.apnivehicle.models

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val username: String,
    val password: String,
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
    val createdAt: Long = System.currentTimeMillis()
)

