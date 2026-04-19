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
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1623859627248-cb5f1d391f4d?q=80&w=1000&auto=format&fit=crop",
                description = "Neat family sedan in excellent condition. Well maintained with complete service history. First owner, accident-free.",
                brand = "Toyota",
                model = "Corolla",
                mileage = 45000,
                transmission = "Manual",
                fuelType = "Petrol",
                condition = "Used",
                color = "Silver",
                numberOfOwners = 1,
                sellerId = "seller-001",
                sellerPhone = "0300-1234567",
                sellerRating = 4.8f,
                sellerReviewCount = 12
            ),
            Vehicle(
                title = "Honda Civic Oriel",
                price = 4200000,
                city = "Karachi",
                year = 2021,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1594502184342-2e12f877aa73?q=80&w=1000&auto=format&fit=crop",
                description = "Single owner, low mileage, complete documents. Imported model with sunroof and leather seats. Excellent condition.",
                brand = "Honda",
                model = "Civic",
                mileage = 25000,
                transmission = "Automatic",
                fuelType = "Petrol",
                condition = "Used",
                color = "Black",
                numberOfOwners = 1,
                sellerId = "seller-002",
                sellerPhone = "0321-9876543",
                sellerRating = 5f,
                sellerReviewCount = 28,
                isFavorite = true
            ),
            Vehicle(
                title = "Suzuki Alto VXR",
                price = 1950000,
                city = "Islamabad",
                year = 2020,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?q=80&w=1000&auto=format&fit=crop",
                description = "Economical city car with chilled AC. Perfect for daily commute. Low fuel consumption and easy maintenance.",
                brand = "Suzuki",
                model = "Alto",
                mileage = 35000,
                transmission = "Manual",
                fuelType = "Petrol",
                condition = "Used",
                color = "White",
                numberOfOwners = 1,
                sellerId = "seller-003",
                sellerPhone = "0333-5555666",
                isMyAd = true,
                sellerRating = 4.5f,
                sellerReviewCount = 8
            ),
            Vehicle(
                title = "Hyundai Elantra",
                price = 3800000,
                city = "Lahore",
                year = 2020,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?q=80&w=1000&auto=format&fit=crop",
                description = "Imported model with sunroof. Fully loaded with all features. Excellent driving experience and comfort.",
                brand = "Hyundai",
                model = "Elantra",
                mileage = 52000,
                transmission = "Automatic",
                fuelType = "Petrol",
                condition = "Used",
                color = "Blue",
                numberOfOwners = 2,
                sellerId = "seller-004",
                sellerPhone = "0345-7778899",
                sellerRating = 4.7f,
                sellerReviewCount = 15
            ),
            Vehicle(
                title = "Honda City Aspire",
                price = 2850000,
                city = "Faisalabad",
                year = 2018,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1583121274602-3e2820c69888?q=80&w=1000&auto=format&fit=crop",
                description = "Well maintained Honda City with complete service record. Smooth engine, excellent fuel average.",
                brand = "Honda",
                model = "City",
                mileage = 68000,
                transmission = "Manual",
                fuelType = "Petrol",
                condition = "Used",
                color = "Red",
                numberOfOwners = 1,
                sellerId = "seller-005",
                sellerPhone = "0301-2223344",
                sellerRating = 4.6f,
                sellerReviewCount = 10
            ),
            Vehicle(
                title = "Suzuki Cultus VXL",
                price = 2100000,
                city = "Multan",
                year = 2019,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?q=80&w=1000&auto=format&fit=crop",
                description = "Family car in pristine condition. Low mileage, single owner. All original parts and documents.",
                brand = "Suzuki",
                model = "Cultus",
                mileage = 42000,
                transmission = "Automatic",
                fuelType = "Petrol",
                condition = "Used",
                color = "Pearl White",
                numberOfOwners = 1,
                sellerId = "seller-006",
                sellerPhone = "0322-4445566",
                sellerRating = 4.9f,
                sellerReviewCount = 22
            ),
            Vehicle(
                title = "Toyota Yaris GLi",
                price = 2950000,
                city = "Rawalpindi",
                year = 2021,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?q=80&w=1000&auto=format&fit=crop",
                description = "Brand new condition Toyota Yaris. Barely driven, like showroom condition. Full warranty remaining.",
                brand = "Toyota",
                model = "Yaris",
                mileage = 15000,
                transmission = "Automatic",
                fuelType = "Petrol",
                condition = "Used",
                color = "White",
                numberOfOwners = 1,
                sellerId = "seller-007",
                sellerPhone = "0334-6667788",
                sellerRating = 4.9f,
                sellerReviewCount = 18
            ),
            Vehicle(
                title = "Suzuki Wagon R VXL",
                price = 1750000,
                city = "Peshawar",
                year = 2019,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?q=80&w=1000&auto=format&fit=crop",
                description = "Spacious family hatchback. Great fuel economy and comfortable ride. Well maintained.",
                brand = "Suzuki",
                model = "Wagon R",
                mileage = 48000,
                transmission = "Manual",
                fuelType = "Petrol",
                condition = "Used",
                color = "Silver",
                numberOfOwners = 1,
                sellerId = "seller-008",
                sellerPhone = "0346-8889900",
                sellerRating = 4.4f,
                sellerReviewCount = 9
            ),
            Vehicle(
                title = "Kia Sportage AWD",
                price = 5500000,
                city = "Karachi",
                year = 2022,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=1000&auto=format&fit=crop",
                description = "Premium SUV with all features. Leather interior, panoramic sunroof, advanced safety features. Like new condition.",
                brand = "Kia",
                model = "Sportage",
                mileage = 18000,
                transmission = "Automatic",
                fuelType = "Petrol",
                condition = "Used",
                color = "Grey",
                numberOfOwners = 1,
                sellerId = "seller-009",
                sellerPhone = "0311-1112233",
                sellerRating = 5f,
                sellerReviewCount = 35
            ),
            Vehicle(
                title = "Toyota Fortuner 4x4",
                price = 8500000,
                city = "Lahore",
                year = 2020,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?q=80&w=1000&auto=format&fit=crop",
                description = "Powerful 4x4 SUV. Perfect for family trips and off-road adventures. Fully maintained with service history.",
                brand = "Toyota",
                model = "Fortuner",
                mileage = 55000,
                transmission = "Automatic",
                fuelType = "Diesel",
                condition = "Used",
                color = "White",
                numberOfOwners = 1,
                sellerId = "seller-010",
                sellerPhone = "0323-3334455",
                sellerRating = 4.8f,
                sellerReviewCount = 20
            ),
            Vehicle(
                title = "Suzuki Swift DLX",
                price = 2250000,
                city = "Sialkot",
                year = 2019,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1502877338535-766e1452684a?q=80&w=1000&auto=format&fit=crop",
                description = "Sporty hatchback with excellent fuel economy. Perfect for city driving. Well maintained with all documents.",
                brand = "Suzuki",
                model = "Swift",
                mileage = 38000,
                transmission = "Manual",
                fuelType = "Petrol",
                condition = "Used",
                color = "Red",
                numberOfOwners = 1,
                sellerId = "seller-011",
                sellerPhone = "0335-5556677",
                sellerRating = 4.6f,
                sellerReviewCount = 14
            ),
            Vehicle(
                title = "Honda BRV i-VTEC",
                price = 3650000,
                city = "Gujranwala",
                year = 2021,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1494976388531-d1058494cdd8?q=80&w=1000&auto=format&fit=crop",
                description = "7-seater family SUV. Spacious interior, comfortable ride. Perfect for large families. Excellent condition.",
                brand = "Honda",
                model = "BRV",
                mileage = 32000,
                transmission = "Manual",
                fuelType = "Petrol",
                condition = "Used",
                color = "Silver",
                numberOfOwners = 1,
                sellerId = "seller-012",
                sellerPhone = "0347-7778899",
                sellerRating = 4.7f,
                sellerReviewCount = 16
            ),
            Vehicle(
                title = "Nissan Dayz Highway Star",
                price = 1850000,
                city = "Quetta",
                year = 2018,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1542362567-b05500269734?q=80&w=1000&auto=format&fit=crop",
                description = "Japanese import. Excellent fuel economy. Ideal for city use. Well maintained with low mileage.",
                brand = "Nissan",
                model = "Dayz",
                mileage = 28000,
                transmission = "Automatic",
                fuelType = "Petrol",
                condition = "Used",
                color = "Blue",
                numberOfOwners = 1,
                sellerId = "seller-013",
                sellerPhone = "0312-9990011",
                sellerRating = 4.5f,
                sellerReviewCount = 11
            ),
            Vehicle(
                title = "MG HS Exclusive",
                price = 4950000,
                city = "Islamabad",
                year = 2022,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1617469767053-d3b508a0d182?q=80&w=1000&auto=format&fit=crop",
                description = "Modern SUV with latest technology. Panoramic sunroof, digital cluster, ADAS features. Under warranty.",
                brand = "MG",
                model = "HS",
                mileage = 12000,
                transmission = "Automatic",
                fuelType = "Petrol",
                condition = "Used",
                color = "Black",
                numberOfOwners = 1,
                sellerId = "seller-014",
                sellerPhone = "0324-1122334",
                sellerRating = 4.9f,
                sellerReviewCount = 25
            ),
            Vehicle(
                title = "Daihatsu Mira ES",
                price = 1450000,
                city = "Hyderabad",
                year = 2017,
                image = 0,
                imageUri = "https://images.unsplash.com/photo-1619767886558-efdc259cde1a?q=80&w=1000&auto=format&fit=crop",
                description = "Budget-friendly city car. Great fuel economy. Perfect for daily commute. Clean and well maintained.",
                brand = "Daihatsu",
                model = "Mira",
                mileage = 62000,
                transmission = "Automatic",
                fuelType = "Petrol",
                condition = "Used",
                color = "White",
                numberOfOwners = 2,
                sellerId = "seller-015",
                sellerPhone = "0336-2233445",
                sellerRating = 4.3f,
                sellerReviewCount = 7
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

    fun updateVehicle(updatedVehicle: Vehicle, context: Context? = null) {
        val index = vehicles.indexOfFirst { it.id == updatedVehicle.id }
        if (index != -1) {
            val oldVehicle = vehicles[index]
            
            // Check for price drop
            if (updatedVehicle.price < oldVehicle.price) {
                // Record price change
                updatedVehicle.priceHistory.addAll(oldVehicle.priceHistory)
                updatedVehicle.priceHistory.add(PriceRecord(updatedVehicle.price))
                
                // Send price drop broadcast if vehicle is favorited and context is available
                if (updatedVehicle.isFavorite && context != null) {
                    com.example.apnivehicle.receivers.PriceDropBroadcastReceiver.sendPriceDropAlert(
                        context,
                        updatedVehicle.id,
                        oldVehicle.price,
                        updatedVehicle.price
                    )
                    Log.d(TAG, "Price drop alert sent for ${updatedVehicle.title}: ${oldVehicle.price} -> ${updatedVehicle.price}")
                }
            }
            
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
