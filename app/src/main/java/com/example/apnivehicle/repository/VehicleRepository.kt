package com.example.apnivehicle.repository

import android.content.Context
import android.util.Log
import com.example.apnivehicle.R
import com.example.apnivehicle.models.PriceRecord
import com.example.apnivehicle.models.SearchHistory
import com.example.apnivehicle.models.SearchPreference
import com.example.apnivehicle.models.Vehicle
import com.example.apnivehicle.utils.FileManager

object VehicleRepository {
    
    private const val TAG = "VehicleRepository"
    
    enum class SortOption {
        PRICE_LOW_HIGH,
        PRICE_HIGH_LOW,
        ALPHABETICAL,
        LATEST,
        OLDEST
    }

    private val vehicles = arrayListOf<Vehicle>()
    private val favoriteIds = mutableSetOf<String>()
    private var isInitialized = false
    
    /**
     * Initialize repository - load data from storage
     */
    fun init(context: Context) {
        if (isInitialized) return
        
        FileManager.init(context)
        
        // Load vehicles from storage
        val savedVehicles = FileManager.loadVehicles()
        if (savedVehicles.isNotEmpty()) {
            vehicles.clear()
            vehicles.addAll(savedVehicles)
            Log.d(TAG, "Loaded ${vehicles.size} vehicles from storage")
        } else {
            // Add sample data if no saved vehicles
            loadSampleData()
            saveVehicles()
        }
        
        // Load favorites
        val savedFavorites = FileManager.loadFavorites()
        favoriteIds.clear()
        favoriteIds.addAll(savedFavorites)
        
        // Update favorite status in vehicles
        vehicles.forEach { vehicle ->
            vehicle.isFavorite = favoriteIds.contains(vehicle.id)
        }
        
        isInitialized = true
        Log.d(TAG, "Repository initialized with ${vehicles.size} vehicles and ${favoriteIds.size} favorites")
    }
    
    /**
     * Load sample data for first launch
     */
    private fun loadSampleData() {
        vehicles.addAll(listOf(
        Vehicle(
            title = "Toyota Corolla XLi",
            price = 3200000,
            city = "Lahore",
            year = 2019,
            image = R.drawable.ic_directions_car,
            description = "Neat family sedan in excellent condition.",
            brand = "Toyota",
            model = "Corolla",
            mileage = 45000,
            transmission = "Manual",
            fuelType = "Petrol",
            condition = "Used",
            sellerRating = 4.8f,
            sellerReviewCount = 12
        ),
        Vehicle(
            title = "Honda Civic Oriel",
            price = 4200000,
            city = "Karachi",
            year = 2021,
            image = R.drawable.ic_car_rental,
            description = "Single owner, low mileage, complete documents.",
            brand = "Honda",
            model = "Civic",
            mileage = 25000,
            transmission = "Automatic",
            fuelType = "Petrol",
            condition = "Used",
            sellerRating = 5f,
            sellerReviewCount = 28,
            isFavorite = true
        ),
        Vehicle(
            title = "Suzuki Alto VXR",
            price = 1950000,
            city = "Islamabad",
            year = 2020,
            image = R.drawable.ic_directions_car,
            description = "Economical city car with chilled AC.",
            brand = "Suzuki",
            model = "Alto",
            mileage = 35000,
            transmission = "Manual",
            fuelType = "Petrol",
            condition = "Used",
            isMyAd = true,
            sellerRating = 4.5f,
            sellerReviewCount = 8
        ),
        Vehicle(
            title = "Hyundai Elantra",
            price = 3800000,
            city = "Lahore",
            year = 2020,
            image = R.drawable.ic_directions_car,
            description = "Imported model with sunroof.",
            brand = "Hyundai",
            model = "Elantra",
            mileage = 52000,
            transmission = "Automatic",
            fuelType = "Petrol",
            condition = "Used",
            sellerRating = 4.7f,
            sellerReviewCount = 15
        )
    ))
    }
    
    /**
     * Save vehicles to storage
     */
    private fun saveVehicles() {
        FileManager.saveVehicles(vehicles)
    }
    
    /**
     * Save favorites to storage
     */
    private fun saveFavorites() {
        FileManager.saveFavorites(favoriteIds.toList())
    }

    private val searchPreferences = mutableListOf<SearchPreference>()
    private val searchHistory = mutableListOf<SearchHistory>()
    private val wishlistComparisons = mutableListOf<List<String>>()  // Store vehicle IDs for comparison

    // ===== Basic Operations =====
    fun addVehicle(vehicle: Vehicle) {
        vehicles.add(0, vehicle)
        saveVehicles()
        Log.d(TAG, "Vehicle added: ${vehicle.title}")
    }

    fun deleteVehicle(vehicleId: String) {
        val vehicle = vehicles.find { it.id == vehicleId }
        vehicles.removeAll { it.id == vehicleId }
        
        // Delete associated images
        vehicle?.let {
            if (!it.imageUri.isNullOrEmpty()) {
                FileManager.deleteImage(it.imageUri!!)
            }
            if (it.imageList.isNotEmpty()) {
                FileManager.deleteImages(it.imageList)
            }
        }
        
        // Remove from favorites if present
        if (favoriteIds.remove(vehicleId)) {
            saveFavorites()
        }
        
        saveVehicles()
        Log.d(TAG, "Vehicle deleted: $vehicleId")
    }

    fun updateVehicle(updatedVehicle: Vehicle) {
        val index = vehicles.indexOfFirst { it.id == updatedVehicle.id }
        if (index != -1) {
            vehicles[index] = updatedVehicle
            saveVehicles()
            Log.d(TAG, "Vehicle updated: ${updatedVehicle.title}")
        }
    }

    fun getVehicles(): List<Vehicle> = vehicles.toList()

    fun getVehicleById(vehicleId: String): Vehicle? = vehicles.find { it.id == vehicleId }

    fun getFavorites(): List<Vehicle> = vehicles.filter { it.isFavorite }

    fun getMyAds(): List<Vehicle> = vehicles.filter { it.isMyAd }

    fun toggleFavorite(vehicleId: String): Vehicle? {
        val vehicle = vehicles.find { it.id == vehicleId } ?: return null
        vehicle.isFavorite = !vehicle.isFavorite
        
        if (vehicle.isFavorite) {
            favoriteIds.add(vehicleId)
        } else {
            favoriteIds.remove(vehicleId)
        }
        
        saveFavorites()
        Log.d(TAG, "Favorite toggled for: ${vehicle.title}, isFavorite: ${vehicle.isFavorite}")
        return vehicle
    }

    fun incrementViewCount(vehicleId: String) {
        vehicles.find { it.id == vehicleId }?.let { vehicle ->
            vehicle.viewCount++
            saveVehicles()
        }
    }

    // ===== Search & Filter =====
    fun searchVehicles(query: String, source: List<Vehicle> = vehicles): List<Vehicle> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isNotEmpty()) {
            addSearchHistory(query)
        }
        if (normalizedQuery.isEmpty()) return source
        return source.filter {
            it.title.lowercase().contains(normalizedQuery) ||
                it.city.lowercase().contains(normalizedQuery) ||
                it.brand.lowercase().contains(normalizedQuery) ||
                it.model.lowercase().contains(normalizedQuery)
        }
    }

    fun advancedSearch(
        query: String = "",
        brand: String? = null,
        model: String? = null,
        minPrice: Long? = null,
        maxPrice: Long? = null,
        minMileage: Int? = null,
        maxMileage: Int? = null,
        transmission: String? = null,
        fuelType: String? = null,
        condition: String? = null,
        city: String? = null,
        source: List<Vehicle> = vehicles
    ): List<Vehicle> {
        var result = source

        // Text search first
        if (query.isNotBlank()) {
            result = searchVehicles(query, result)
        }

        // Then apply filters
        result = result.filter { vehicle ->
            val brandMatches = brand.isNullOrBlank() || vehicle.brand.equals(brand, ignoreCase = true)
            val modelMatches = model.isNullOrBlank() || vehicle.model.equals(model, ignoreCase = true)
            val minPriceMatches = minPrice == null || vehicle.price >= minPrice
            val maxPriceMatches = maxPrice == null || vehicle.price <= maxPrice
            val minMileageMatches = minMileage == null || vehicle.mileage >= minMileage
            val maxMileageMatches = maxMileage == null || vehicle.mileage <= maxMileage
            val transmissionMatches = transmission.isNullOrBlank() || vehicle.transmission.equals(transmission, ignoreCase = true)
            val fuelMatches = fuelType.isNullOrBlank() || vehicle.fuelType.equals(fuelType, ignoreCase = true)
            val conditionMatches = condition.isNullOrBlank() || vehicle.condition.equals(condition, ignoreCase = true)
            val cityMatches = city.isNullOrBlank() || vehicle.city.equals(city, ignoreCase = true)

            brandMatches && modelMatches && minPriceMatches && maxPriceMatches &&
                minMileageMatches && maxMileageMatches && transmissionMatches &&
                fuelMatches && conditionMatches && cityMatches
        }

        return result
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

    // ===== Sorting =====
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

    // ===== Search Preferences =====
    fun saveSearchPreference(preference: SearchPreference): Boolean {
        return searchPreferences.add(preference)
    }

    fun getSearchPreferences(): List<SearchPreference> = searchPreferences.toList()

    fun deleteSearchPreference(preferenceId: String): Boolean {
        return searchPreferences.removeAll { it.id == preferenceId }
    }

    fun applySearchPreference(preference: SearchPreference): List<Vehicle> {
        return advancedSearch(
            brand = if (preference.brand.isNotEmpty()) preference.brand else null,
            model = if (preference.model.isNotEmpty()) preference.model else null,
            minPrice = if (preference.minPrice > 0) preference.minPrice else null,
            maxPrice = if (preference.maxPrice < 10000000L) preference.maxPrice else null,
            minMileage = if (preference.minMileage > 0) preference.minMileage else null,
            maxMileage = if (preference.maxMileage < 500000) preference.maxMileage else null,
            transmission = if (preference.transmission.isNotEmpty()) preference.transmission else null,
            fuelType = if (preference.fuelType.isNotEmpty()) preference.fuelType else null,
            condition = if (preference.condition.isNotEmpty()) preference.condition else null,
            city = if (preference.city.isNotEmpty()) preference.city else null
        )
    }

    // ===== Search History =====
    fun addSearchHistory(query: String) {
        if (query.isNotBlank()) {
            searchHistory.add(0, SearchHistory(query))
            // Keep only last 20 searches
            if (searchHistory.size > 20) {
                searchHistory.removeAt(searchHistory.size - 1)
            }
        }
    }

    fun getSearchHistory(): List<SearchHistory> = searchHistory.toList()

    fun clearSearchHistory() {
        searchHistory.clear()
    }

    // ===== Wishlist Comparison =====
    fun getComparisonVehicles(vehicleIds: List<String>): List<Vehicle> {
        return vehicles.filter { it.id in vehicleIds }
    }

    fun saveComparison(vehicleIds: List<String>) {
        if (vehicleIds.isNotEmpty() && vehicleIds.size <= 3) {
            wishlistComparisons.add(vehicleIds)
        }
    }

    fun getSavedComparisons(): List<List<String>> = wishlistComparisons.toList()

    fun deleteComparison(index: Int) {
        if (index in wishlistComparisons.indices) {
            wishlistComparisons.removeAt(index)
        }
    }

    // ===== Price Tracking =====
    fun recordPriceChange(vehicleId: String, newPrice: Long) {
        vehicles.find { it.id == vehicleId }?.apply {
            priceHistory.add(PriceRecord(newPrice))
            price = newPrice
        }
    }

    fun getPriceHistory(vehicleId: String): List<PriceRecord> {
        return vehicles.find { it.id == vehicleId }?.priceHistory ?: emptyList()
    }

    // ===== Utility Functions =====
    fun getUniqueBrands(): List<String> {
        return vehicles.map { it.brand }.distinct().sorted()
    }

    fun getUniqueModels(brand: String): List<String> {
        return vehicles.filter { it.brand.equals(brand, ignoreCase = true) }
            .map { it.model }
            .distinct()
            .sorted()
    }

    fun getUniqueCities(): List<String> {
        return vehicles.map { it.city }.distinct().sorted()
    }

    fun getUniqueFuelTypes(): List<String> {
        return vehicles.map { it.fuelType }.distinct().sorted()
    }

    fun getUniqueTransmissions(): List<String> {
        return vehicles.map { it.transmission }.distinct().sorted()
    }

    fun getUniqueConditions(): List<String> {
        return vehicles.map { it.condition }.distinct().sorted()
    }

    fun getPriceRange(): Pair<Long, Long> {
        return if (vehicles.isEmpty()) {
            0L to 10000000L
        } else {
            vehicles.minOf { it.price } to vehicles.maxOf { it.price }
        }
    }

    fun getMileageRange(): Pair<Int, Int> {
        return if (vehicles.isEmpty()) {
            0 to 500000
        } else {
            vehicles.minOf { it.mileage } to vehicles.maxOf { it.mileage }
        }
    }
}

