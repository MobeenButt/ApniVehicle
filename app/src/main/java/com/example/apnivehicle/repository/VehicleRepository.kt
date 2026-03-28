package com.example.apnivehicle.repository

import com.example.apnivehicle.R
import com.example.apnivehicle.models.Vehicle

object VehicleRepository {
    enum class SortOption {
        PRICE_LOW_HIGH,
        PRICE_HIGH_LOW,
        ALPHABETICAL,
        LATEST,
        OLDEST
    }

    private val vehicles = arrayListOf(
        Vehicle(
            title = "Toyota Corolla XLi",
            price = 3200000,
            city = "Lahore",
            year = 2019,
            image = R.drawable.ic_directions_car,
            description = "Neat family sedan in excellent condition.",
            isFavorite = false,
            isMyAd = false
        ),
        Vehicle(
            title = "Honda Civic Oriel",
            price = 4200000,
            city = "Karachi",
            year = 2021,
            image = R.drawable.ic_car_rental,
            description = "Single owner, low mileage, complete documents.",
            isFavorite = true,
            isMyAd = false
        ),
        Vehicle(
            title = "Suzuki Alto VXR",
            price = 1950000,
            city = "Islamabad",
            year = 2020,
            image = R.drawable.ic_directions_car,
            description = "Economical city car with chilled AC.",
            isFavorite = false,
            isMyAd = true
        )
    )

    fun addVehicle(vehicle: Vehicle) {
        vehicles.add(0, vehicle)
    }

    fun deleteVehicle(vehicleId: String) {
        vehicles.removeAll { it.id == vehicleId }
    }

    fun updateVehicle(updatedVehicle: Vehicle) {
        val index = vehicles.indexOfFirst { it.id == updatedVehicle.id }
        if (index != -1) {
            vehicles[index] = updatedVehicle
        }
    }

    fun getVehicles(): List<Vehicle> = vehicles.toList()

    fun getFavorites(): List<Vehicle> = vehicles.filter { it.isFavorite }

    fun getMyAds(): List<Vehicle> = vehicles.filter { it.isMyAd }

    fun toggleFavorite(vehicleId: String): Vehicle? {
        val vehicle = vehicles.find { it.id == vehicleId } ?: return null
        vehicle.isFavorite = !vehicle.isFavorite
        return vehicle
    }

    fun searchVehicles(query: String): List<Vehicle> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) return getVehicles()
        return vehicles.filter {
            it.title.lowercase().contains(normalizedQuery) ||
                it.city.lowercase().contains(normalizedQuery) ||
                it.brand.lowercase().contains(normalizedQuery)
        }
    }

    fun filterVehicles(
        city: String? = null,
        minPrice: Long? = null,
        maxPrice: Long? = null,
        brand: String? = null,
        year: Int? = null,
        source: List<Vehicle> = vehicles
    ): List<Vehicle> {
        return source.filter { vehicle ->
            val cityMatches = city.isNullOrBlank() || vehicle.city.equals(city, ignoreCase = true)
            val brandMatches = brand.isNullOrBlank() || vehicle.brand.equals(brand, ignoreCase = true)
            val minMatches = minPrice == null || vehicle.price >= minPrice
            val maxMatches = maxPrice == null || vehicle.price <= maxPrice
            val yearMatches = year == null || vehicle.year == year
            cityMatches && brandMatches && minMatches && maxMatches && yearMatches
        }
    }

    fun sortVehicles(
        sortOption: SortOption,
        source: List<Vehicle> = vehicles
    ): List<Vehicle> {
        return when (sortOption) {
            SortOption.PRICE_LOW_HIGH -> source.sortedBy { it.price }
            SortOption.PRICE_HIGH_LOW -> source.sortedByDescending { it.price }
            SortOption.ALPHABETICAL -> source.sortedBy { it.title.lowercase() }
            SortOption.LATEST -> source.sortedByDescending { it.createdAt }
            SortOption.OLDEST -> source.sortedBy { it.createdAt }
        }
    }
}
