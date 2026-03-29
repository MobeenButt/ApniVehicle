package com.example.apnivehicle.models

import java.util.UUID

data class Vehicle(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var price: Long,
    var city: String,
    var year: Int,
    var type: VehicleType = VehicleType.CAR,
    var fuelType: String = "Petrol",
    var transmission: String = "Manual",
    var engineCapacity: String = "1000 cc",
    var color: String = "White",
    var assembly: String = "Local", // Local or Imported
    var mileage: Int = 0,
    var condition: String = "Used",
    var sellerName: String = "Owner",
    var sellerPhone: String = "03001234567",
    var isVerified: Boolean = false, // PakWheels-like "Managed by" or "Inspected"
    var inspectionScore: Int = 0, // 0-10 score for inspected vehicles
    var image: Int = 0,
    var imageUri: String? = null,
    var description: String,
    var isFavorite: Boolean = false,
    var isMyAd: Boolean = false,
    var createdAt: Long = System.currentTimeMillis()
) {
    val brand: String
        get() = title.substringBefore(" ", title)
}

enum class VehicleType {
    CAR, BIKE, TRUCK, VAN
}
