package com.example.apnivehicle.models

import java.util.UUID

data class Vehicle(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var price: Long,
    var city: String,
    var year: Int,
    var image: Int = 0,  // drawable resource id, 0 if imageUri is used
    var imageUri: String? = null,  // URI for custom images
    var description: String,
    var isFavorite: Boolean = false,
    var isMyAd: Boolean = false,
    var createdAt: Long = System.currentTimeMillis()
) {
    val brand: String
        get() = title.substringBefore(" ", title)
}
