package com.example.apnivehicle.models

import java.util.UUID

data class Vehicle(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var price: Long = 0L,
    var city: String = "",
    var year: Int = 0,
    var image: Int = 0,                          // drawable resource id, 0 if imageUri is used
    var imageUri: String? = null,                // URI for custom images
    var imageList: MutableList<String> = mutableListOf(),  // Multiple images URIs
    var description: String = "",
    var type: VehicleType = VehicleType.CAR,
    var isFavorite: Boolean = false,
    var isMyAd: Boolean = false,
    var sellerId: String = "",
    var sellerPhone: String = "",
    var brand: String = "",
    var model: String = "",
    var mileage: Int = 0,
    var transmission: String = "Manual",
    var fuelType: String = "Petrol",
    var condition: String = "Used",
    var color: String = "",
    var numberOfOwners: Int = 1,
    var sellerRating: Float = 5f,
    var sellerReviewCount: Int = 0,
    var viewCount: Int = 0,
    var priceHistory: MutableList<PriceRecord> = mutableListOf(),
    var createdAt: Long = System.currentTimeMillis(),
    // Offline stale data tracking
    var lastSyncedAt: Long = System.currentTimeMillis()
)

data class PriceRecord(
    val price: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
) {
    // Fix: stable no-arg constructor for Firestore deserialization
    constructor() : this(0L, 0L)
}
