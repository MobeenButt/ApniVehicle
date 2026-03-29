package com.example.apnivehicle.models

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var email: String,
    var phone: String,
    var profileImage: String? = null,
    var address: String? = null
)
