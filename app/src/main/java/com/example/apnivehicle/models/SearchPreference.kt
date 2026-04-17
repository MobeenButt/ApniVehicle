package com.example.apnivehicle.models

import java.util.UUID

data class SearchPreference(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val brand: String = "",
    val model: String = "",
    val minPrice: Long = 0L,
    val maxPrice: Long = 10000000L,
    val minMileage: Int = 0,
    val maxMileage: Int = 500000,
    val transmission: String = "",
    val fuelType: String = "",
    val condition: String = "",
    val city: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

