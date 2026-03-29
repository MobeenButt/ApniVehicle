package com.example.apnivehicle.repository

import com.example.apnivehicle.R
import com.example.apnivehicle.models.Vehicle
import com.example.apnivehicle.models.VehicleType

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
            title = "Toyota Corolla Altis 1.6",
            price = 4500000,
            city = "Lahore",
            year = 2022,
            type = VehicleType.CAR,
            fuelType = "Petrol",
            transmission = "Automatic",
            engineCapacity = "1600 cc",
            color = "White",
            assembly = "Local",
            mileage = 15000,
            condition = "Like New",
            sellerName = "Ahmed Ali",
            sellerPhone = "03214567890",
            isVerified = true,
            inspectionScore = 9,
            image = R.drawable.ic_directions_car,
            description = "First hand, bumper to bumper original. Maintained from Toyota dealership. PakWheels Inspected.",
            isFavorite = false,
            isMyAd = false
        ),
        Vehicle(
            title = "Honda Civic RS",
            price = 7500000,
            city = "Karachi",
            year = 2023,
            type = VehicleType.CAR,
            fuelType = "Petrol",
            transmission = "Automatic",
            engineCapacity = "1500 cc Turbo",
            color = "Meteoroid Gray",
            assembly = "Local",
            mileage = 5000,
            condition = "New",
            sellerName = "Zain Mansoor",
            sellerPhone = "03339876543",
            isVerified = true,
            inspectionScore = 10,
            image = R.drawable.ic_car_rental,
            description = "Top of the line variant. Ceramic coated. Managed by ApniVehicle.",
            isFavorite = true,
            isMyAd = false
        ),
        Vehicle(
            title = "Honda CD 70",
            price = 150000,
            city = "Islamabad",
            year = 2023,
            type = VehicleType.BIKE,
            fuelType = "Petrol",
            transmission = "Manual",
            engineCapacity = "70 cc",
            color = "Red",
            assembly = "Local",
            mileage = 2000,
            condition = "Used",
            sellerName = "Usman Khan",
            sellerPhone = "03001122334",
            isVerified = false,
            image = R.drawable.ic_two_wheeler,
            description = "Standard bike, very economical fuel average. Registered.",
            isFavorite = false,
            isMyAd = true
        ),
        Vehicle(
            title = "Toyota Land Cruiser ZX",
            price = 85000000,
            city = "Islamabad",
            year = 2022,
            type = VehicleType.CAR,
            fuelType = "Diesel",
            transmission = "Automatic",
            engineCapacity = "3300 cc",
            color = "Black",
            assembly = "Imported",
            mileage = 12000,
            condition = "Excellent",
            sellerName = "Malik Sahab",
            sellerPhone = "03210000000",
            isVerified = true,
            inspectionScore = 9,
            image = R.drawable.ic_directions_car,
            description = "Full option, Japanese import. 7 seater luxury SUV.",
            isFavorite = false,
            isMyAd = false
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

    fun searchVehicles(query: String, source: List<Vehicle> = vehicles): List<Vehicle> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) return source
        return source.filter {
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
        type: VehicleType? = null,
        source: List<Vehicle> = vehicles
    ): List<Vehicle> {
        return source.filter { vehicle ->
            val cityMatches = city.isNullOrBlank() || vehicle.city.equals(city, ignoreCase = true)
            val brandMatches = brand.isNullOrBlank() || vehicle.brand.equals(brand, ignoreCase = true)
            val minMatches = minPrice == null || vehicle.price >= minPrice
            val maxMatches = maxPrice == null || vehicle.price <= maxPrice
            val yearMatches = year == null || vehicle.year == year
            val typeMatches = type == null || vehicle.type == type
            cityMatches && brandMatches && minMatches && maxMatches && yearMatches && typeMatches
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
