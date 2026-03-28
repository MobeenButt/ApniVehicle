package com.example.apnivehicle.models

data class Vehicle(
    val id: String,
    val title: String,
    val price: Long,
    val mileage: String,
    val location: String,
    val fuelType: String,
    val imageUrl: String,
    val isVerified: Boolean = false
)
