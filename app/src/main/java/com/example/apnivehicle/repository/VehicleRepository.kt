package com.example.apnivehicle.repository

import android.content.Context
import android.util.Log
import com.example.apnivehicle.models.PriceRecord
import com.example.apnivehicle.models.SearchHistory
import com.example.apnivehicle.models.SearchPreference
import com.example.apnivehicle.models.Vehicle
import com.example.apnivehicle.utils.FileManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object VehicleRepository {

    private const val TAG = "VehicleRepository"
    private const val COLLECTION_VEHICLES = "vehicles"
    private const val COLLECTION_FAVORITES = "favorites"

    enum class SortOption {
        PRICE_LOW_HIGH, PRICE_HIGH_LOW, ALPHABETICAL, LATEST, OLDEST
    }

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    // Fix: use a synchronized list so concurrent reads (main thread) and writes
    // (Firestore snapshot listener on background thread) don't cause ConcurrentModificationException.
    private val vehicles = java.util.Collections.synchronizedList(arrayListOf<Vehicle>())
    private val favoriteIds = mutableSetOf<String>()
    private val searchPreferences = mutableListOf<SearchPreference>()
    private val searchHistory = mutableListOf<SearchHistory>()
    @Volatile private var isInitialized = false
    private var vehiclesListener: ListenerRegistration? = null
    // Retain the search-preferences listener so it can be removed when stopListening() is called.
    // The old code discarded the return value, creating a permanent leak that grew with every
    // screen rotation or re-login.
    private var searchPreferencesListener: ListenerRegistration? = null
    var lastSyncedAt: Long = 0L
        private set

    fun init(context: Context) {
        if (isInitialized) return
        FileManager.init(context)
        val savedVehicles = FileManager.loadVehicles()
        if (savedVehicles.isNotEmpty()) {
            vehicles.clear()
            vehicles.addAll(savedVehicles)
        } else {
            loadSampleData()
            FileManager.saveVehicles(vehicles)
        }
        val savedFavorites = FileManager.loadFavorites()
        favoriteIds.clear()
        favoriteIds.addAll(savedFavorites)
        vehicles.forEach { it.isFavorite = favoriteIds.contains(it.id) }
        isInitialized = true
        startFirestoreListener()
    }

    private fun startFirestoreListener() {
        vehiclesListener?.remove()
        vehiclesListener = db.collection(COLLECTION_VEHICLES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { Log.w(TAG, "Firestore listen error", error); return@addSnapshotListener }
                if (snapshot != null) {
                    val firestoreVehicles = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Vehicle::class.java)?.also { v -> v.isFavorite = favoriteIds.contains(v.id) }
                    }
                    if (firestoreVehicles.isNotEmpty()) {
                        vehicles.clear()
                        vehicles.addAll(firestoreVehicles)
                        FileManager.saveVehicles(vehicles)
                        lastSyncedAt = System.currentTimeMillis()
                    }
                }
            }
    }

    fun stopListening() {
        vehiclesListener?.remove(); vehiclesListener = null
        searchPreferencesListener?.remove(); searchPreferencesListener = null
    }

    fun addVehicle(vehicle: Vehicle) {
        vehicles.add(0, vehicle)
        FileManager.saveVehicles(vehicles)
        db.collection(COLLECTION_VEHICLES).document(vehicle.id).set(vehicle)
            .addOnFailureListener { Log.e(TAG, "Firestore addVehicle failed", it) }
    }

    suspend fun addVehicleAsync(vehicle: Vehicle) {
        // Always stamp sellerId with the current user so getMyAds() filters correctly
        val currentUserId = AuthRepository.getCurrentUser()?.id ?: ""
        val stamped = if (currentUserId.isNotBlank() && vehicle.sellerId.isBlank())
            vehicle.copy(sellerId = currentUserId, isMyAd = true)
        else
            vehicle.also { it.isMyAd = true }

        vehicles.add(0, stamped)
        FileManager.saveVehicles(vehicles)
        try {
            db.collection(COLLECTION_VEHICLES).document(stamped.id).set(stamped).await()
            lastSyncedAt = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(TAG, "addVehicleAsync Firestore failed, saved locally", e)
        }
    }

    fun deleteVehicle(vehicleId: String) {
        val vehicle = vehicles.find { it.id == vehicleId }
        vehicles.removeAll { it.id == vehicleId }
        vehicle?.let {
            if (!it.imageUri.isNullOrEmpty()) FileManager.deleteImage(it.imageUri!!)
            if (it.imageList.isNotEmpty()) FileManager.deleteImages(it.imageList)
        }
        if (favoriteIds.remove(vehicleId)) FileManager.saveFavorites(favoriteIds.toList())
        FileManager.saveVehicles(vehicles)
        db.collection(COLLECTION_VEHICLES).document(vehicleId).delete()
            .addOnFailureListener { Log.e(TAG, "Firestore deleteVehicle failed", it) }
    }

    fun updateVehicle(updatedVehicle: Vehicle, context: Context? = null) {
        val index = vehicles.indexOfFirst { it.id == updatedVehicle.id }
        if (index != -1) {
            val oldVehicle = vehicles[index]
            if (updatedVehicle.price < oldVehicle.price) {
                updatedVehicle.priceHistory.addAll(oldVehicle.priceHistory)
                updatedVehicle.priceHistory.add(PriceRecord(updatedVehicle.price))
                if (updatedVehicle.isFavorite && context != null) {
                    com.example.apnivehicle.receivers.PriceDropBroadcastReceiver.sendPriceDropAlert(
                        context, updatedVehicle.id, oldVehicle.price, updatedVehicle.price
                    )
                }
            }
            vehicles[index] = updatedVehicle
            FileManager.saveVehicles(vehicles)
            db.collection(COLLECTION_VEHICLES).document(updatedVehicle.id).set(updatedVehicle)
                .addOnFailureListener { Log.e(TAG, "Firestore updateVehicle failed", it) }
        }
    }

    fun getVehicles(): List<Vehicle> = vehicles.toList()
    fun getVehicleById(vehicleId: String): Vehicle? = vehicles.find { it.id == vehicleId }
    fun getFavorites(): List<Vehicle> = vehicles.filter { it.isFavorite }
    fun getMyAds(): List<Vehicle> {
        val currentUserId = AuthRepository.getCurrentUser()?.id
        // If no user is logged in, return nothing
        if (currentUserId.isNullOrBlank()) return emptyList()
        // Return only vehicles that belong to THIS user — never rely on the isMyAd
        // flag alone because sample data has isMyAd=true for a hardcoded seller ID,
        // which would show up for every user who logs in.
        return vehicles.filter { it.sellerId == currentUserId }
    }

    fun toggleFavorite(vehicleId: String): Vehicle? {
        val vehicle = vehicles.find { it.id == vehicleId } ?: return null
        vehicle.isFavorite = !vehicle.isFavorite
        if (vehicle.isFavorite) favoriteIds.add(vehicleId) else favoriteIds.remove(vehicleId)
        FileManager.saveFavorites(favoriteIds.toList())
        val userId = AuthRepository.getCurrentUser()?.id
        if (userId != null) {
            db.collection(COLLECTION_FAVORITES).document(userId)
                .set(mapOf("ids" to favoriteIds.toList()))
                .addOnFailureListener { Log.e(TAG, "Firestore toggleFavorite failed", it) }
        }
        return vehicle
    }

    fun incrementViewCount(vehicleId: String) {
        vehicles.find { it.id == vehicleId }?.let { vehicle ->
            vehicle.viewCount++
            FileManager.saveVehicles(vehicles)
            db.collection(COLLECTION_VEHICLES).document(vehicleId)
                .update("viewCount", vehicle.viewCount)
                .addOnFailureListener { Log.e(TAG, "Firestore incrementViewCount failed", it) }
        }
    }

    fun searchVehicles(query: String, source: List<Vehicle> = vehicles): List<Vehicle> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isNotEmpty()) addSearchHistory(query)
        if (normalizedQuery.isEmpty()) return source
        return source.filter {
            it.title.lowercase().contains(normalizedQuery) ||
                it.city.lowercase().contains(normalizedQuery) ||
                it.brand.lowercase().contains(normalizedQuery) ||
                it.model.lowercase().contains(normalizedQuery)
        }
    }

    fun advancedSearch(
        query: String = "", brand: String? = null, model: String? = null,
        minPrice: Long? = null, maxPrice: Long? = null,
        minMileage: Int? = null, maxMileage: Int? = null,
        transmission: String? = null, fuelType: String? = null,
        condition: String? = null, city: String? = null,
        source: List<Vehicle> = vehicles
    ): List<Vehicle> {
        var result = source
        if (query.isNotBlank()) result = searchVehicles(query, result)
        return result.filter { v ->
            (brand.isNullOrBlank() || v.brand.equals(brand, ignoreCase = true)) &&
                (model.isNullOrBlank() || v.model.equals(model, ignoreCase = true)) &&
                (minPrice == null || v.price >= minPrice) &&
                (maxPrice == null || v.price <= maxPrice) &&
                (minMileage == null || v.mileage >= minMileage) &&
                (maxMileage == null || v.mileage <= maxMileage) &&
                (transmission.isNullOrBlank() || v.transmission.equals(transmission, ignoreCase = true)) &&
                (fuelType.isNullOrBlank() || v.fuelType.equals(fuelType, ignoreCase = true)) &&
                (condition.isNullOrBlank() || v.condition.equals(condition, ignoreCase = true)) &&
                (city.isNullOrBlank() || v.city.equals(city, ignoreCase = true))
        }
    }

    fun filterVehicles(
        city: String? = null, minPrice: Long? = null, maxPrice: Long? = null,
        brand: String? = null, year: Int? = null, source: List<Vehicle> = vehicles
    ): List<Vehicle> {
        return source.filter { v ->
            (city.isNullOrBlank() || v.city.equals(city, ignoreCase = true)) &&
                (brand.isNullOrBlank() || v.brand.equals(brand, ignoreCase = true)) &&
                (minPrice == null || v.price >= minPrice) &&
                (maxPrice == null || v.price <= maxPrice) &&
                (year == null || v.year == year)
        }
    }

    fun sortVehicles(sortOption: SortOption, source: List<Vehicle> = vehicles): List<Vehicle> {
        return when (sortOption) {
            SortOption.PRICE_LOW_HIGH -> source.sortedBy { it.price }
            SortOption.PRICE_HIGH_LOW -> source.sortedByDescending { it.price }
            SortOption.ALPHABETICAL -> source.sortedBy { it.title.lowercase() }
            SortOption.LATEST -> source.sortedByDescending { it.createdAt }
            SortOption.OLDEST -> source.sortedBy { it.createdAt }
        }
    }

    fun saveSearchPreference(preference: SearchPreference): Boolean {
        searchPreferences.removeAll { it.id == preference.id }
        searchPreferences.add(preference)
        val userId = AuthRepository.getCurrentUser()?.id ?: return true
        db.collection("users").document(userId)
            .collection("searchPreferences").document(preference.id).set(preference)
            .addOnFailureListener { Log.e(TAG, "Firestore saveSearchPreference failed", it) }
        return true
    }

    fun getSearchPreferences(): List<SearchPreference> = searchPreferences.toList()

    fun deleteSearchPreference(preferenceId: String): Boolean {
        val removed = searchPreferences.removeAll { it.id == preferenceId }
        val userId = AuthRepository.getCurrentUser()?.id ?: return removed
        db.collection("users").document(userId)
            .collection("searchPreferences").document(preferenceId).delete()
            .addOnFailureListener { Log.e(TAG, "Firestore deleteSearchPreference failed", it) }
        return removed
    }

    fun loadSearchPreferencesFromFirestore(userId: String) {
        // Remove any previous listener before attaching a new one.
        // Without this, every call (e.g. on screen rotation) stacked an additional
        // orphaned listener that could never be cleaned up.
        searchPreferencesListener?.remove()
        searchPreferencesListener = db.collection("users").document(userId)
            .collection("searchPreferences")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { Log.w(TAG, "searchPreferences listen error", error); return@addSnapshotListener }
                if (snapshot != null) {
                    val prefs = snapshot.documents.mapNotNull { it.toObject(SearchPreference::class.java) }
                    searchPreferences.clear()
                    searchPreferences.addAll(prefs)
                }
            }
    }

    fun applySearchPreference(preference: SearchPreference): List<Vehicle> {
        return advancedSearch(
            brand = preference.brand.ifEmpty { null },
            model = preference.model.ifEmpty { null },
            minPrice = if (preference.minPrice > 0) preference.minPrice else null,
            maxPrice = if (preference.maxPrice < 10000000L) preference.maxPrice else null,
            minMileage = if (preference.minMileage > 0) preference.minMileage else null,
            maxMileage = if (preference.maxMileage < 500000) preference.maxMileage else null,
            transmission = preference.transmission.ifEmpty { null },
            fuelType = preference.fuelType.ifEmpty { null },
            condition = preference.condition.ifEmpty { null },
            city = preference.city.ifEmpty { null }
        )
    }

    fun addSearchHistory(query: String) {
        if (query.isNotBlank()) {
            searchHistory.add(0, SearchHistory(query))
            if (searchHistory.size > 20) searchHistory.removeAt(searchHistory.size - 1)
        }
    }

    fun getSearchHistory(): List<SearchHistory> = searchHistory.toList()
    fun clearSearchHistory() { searchHistory.clear() }

    fun getComparisonVehicles(vehicleIds: List<String>): List<Vehicle> = vehicles.filter { it.id in vehicleIds }
    fun saveComparison(vehicleIds: List<String>) {}
    fun getSavedComparisons(): List<List<String>> = emptyList()
    fun deleteComparison(index: Int) {}

    fun recordPriceChange(vehicleId: String, newPrice: Long) {
        vehicles.find { it.id == vehicleId }?.apply {
            priceHistory.add(PriceRecord(newPrice))
            price = newPrice
        }
    }

    fun getPriceHistory(vehicleId: String): List<PriceRecord> =
        vehicles.find { it.id == vehicleId }?.priceHistory ?: emptyList()

    fun getUniqueBrands(): List<String> = vehicles.map { it.brand }.distinct().sorted()
    fun getUniqueModels(brand: String): List<String> =
        vehicles.filter { it.brand.equals(brand, ignoreCase = true) }.map { it.model }.distinct().sorted()
    fun getUniqueCities(): List<String> = vehicles.map { it.city }.distinct().sorted()
    fun getUniqueFuelTypes(): List<String> = vehicles.map { it.fuelType }.distinct().sorted()
    fun getUniqueTransmissions(): List<String> = vehicles.map { it.transmission }.distinct().sorted()
    fun getUniqueConditions(): List<String> = vehicles.map { it.condition }.distinct().sorted()
    fun getPriceRange(): Pair<Long, Long> =
        if (vehicles.isEmpty()) 0L to 10000000L else vehicles.minOf { it.price } to vehicles.maxOf { it.price }
    fun getMileageRange(): Pair<Int, Int> =
        if (vehicles.isEmpty()) 0 to 500000 else vehicles.minOf { it.mileage } to vehicles.maxOf { it.mileage }

    private fun loadSampleData() {
        vehicles.addAll(listOf(
            Vehicle(title = "Toyota Corolla XLi", price = 3200000, city = "Lahore", year = 2019,
                imageUri = "https://images.unsplash.com/photo-1623859627248-cb5f1d391f4d?q=80&w=1000",
                description = "Neat family sedan in excellent condition. Well maintained with complete service history.",
                brand = "Toyota", model = "Corolla", mileage = 45000, transmission = "Manual",
                fuelType = "Petrol", condition = "Used", color = "Silver", numberOfOwners = 1,
                sellerId = "seller-001", sellerPhone = "0300-1234567", sellerRating = 4.8f, sellerReviewCount = 12),
            Vehicle(title = "Honda Civic Oriel", price = 4200000, city = "Karachi", year = 2021,
                imageUri = "https://images.unsplash.com/photo-1594502184342-2e12f877aa73?q=80&w=1000",
                description = "Single owner, low mileage, complete documents. Imported model with sunroof.",
                brand = "Honda", model = "Civic", mileage = 25000, transmission = "Automatic",
                fuelType = "Petrol", condition = "Used", color = "Black", numberOfOwners = 1,
                sellerId = "seller-002", sellerPhone = "0321-9876543", sellerRating = 5f, sellerReviewCount = 28, isFavorite = true),
            Vehicle(title = "Suzuki Alto VXR", price = 1950000, city = "Islamabad", year = 2020,
                imageUri = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?q=80&w=1000",
                description = "Economical city car with chilled AC. Perfect for daily commute.",
                brand = "Suzuki", model = "Alto", mileage = 35000, transmission = "Manual",
                fuelType = "Petrol", condition = "Used", color = "White", numberOfOwners = 1,
                sellerId = "seller-003", sellerPhone = "0333-5555666", isMyAd = true, sellerRating = 4.5f, sellerReviewCount = 8),
            Vehicle(title = "Kia Sportage AWD", price = 5500000, city = "Karachi", year = 2022,
                imageUri = "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=1000",
                description = "Premium SUV with all features. Leather interior, panoramic sunroof.",
                brand = "Kia", model = "Sportage", mileage = 18000, transmission = "Automatic",
                fuelType = "Petrol", condition = "Used", color = "Grey", numberOfOwners = 1,
                sellerId = "seller-004", sellerPhone = "0345-7778899", sellerRating = 5f, sellerReviewCount = 35),
            Vehicle(title = "Toyota Fortuner 4x4", price = 8500000, city = "Lahore", year = 2020,
                imageUri = "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?q=80&w=1000",
                description = "Powerful 4x4 SUV. Perfect for family trips and off-road adventures.",
                brand = "Toyota", model = "Fortuner", mileage = 55000, transmission = "Automatic",
                fuelType = "Diesel", condition = "Used", color = "White", numberOfOwners = 1,
                sellerId = "seller-005", sellerPhone = "0301-2223344", sellerRating = 4.8f, sellerReviewCount = 20)
        ))
    }
}
