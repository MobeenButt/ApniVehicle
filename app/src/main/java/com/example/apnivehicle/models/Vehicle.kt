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
    var imageList: MutableList<String> = mutableListOf(),  // Multiple images URIs
    var description: String,
    var type: VehicleType = VehicleType.CAR,  // Vehicle type
    var isFavorite: Boolean = false,
    var isMyAd: Boolean = false,
    var sellerId: String = "",  // Link to seller
    var brand: String = title.substringBefore(" ", title),  // Brand/Make
    var model: String = "",  // Model name
    var mileage: Int = 0,  // Mileage in km
    var transmission: String = "Manual",  // Manual/Automatic
    var fuelType: String = "Petrol",  // Petrol/Diesel/CNG/Hybrid
    var condition: String = "Used",  // New/Used/Certified
    var color: String = "",
    var numberOfOwners: Int = 1,
    var sellerRating: Float = 5f,  // 1-5 stars
    var sellerReviewCount: Int = 0,
    var viewCount: Int = 0,
    var priceHistory: MutableList<PriceRecord> = mutableListOf(),  // Track price changes
    var createdAt: Long = System.currentTimeMillis()
)

data class PriceRecord(
    val price: Long,
    val timestamp: Long = System.currentTimeMillis()
)
