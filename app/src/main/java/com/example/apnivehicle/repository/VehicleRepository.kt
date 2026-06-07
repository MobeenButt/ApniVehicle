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
    // Increment this when sample data changes — forces re-upload to Firestore
    private const val SAMPLE_DATA_VERSION = 2
    private const val PREFS_NAME = "vehicle_repo_prefs"
    private const val KEY_SAMPLE_VERSION = "sample_data_version"

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

        val prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val storedVersion = prefs.getInt(KEY_SAMPLE_VERSION, 0)
        val needsSampleRefresh = storedVersion < SAMPLE_DATA_VERSION

        val savedVehicles = FileManager.loadVehicles()

        if (!needsSampleRefresh && savedVehicles.size >= 10) {
            // Cache is current — use it directly
            vehicles.clear()
            vehicles.addAll(savedVehicles)
        } else {
            // First launch OR sample data version bumped — reload all 30 samples,
            // clear local cache, and push to Firestore so all clients get the new data
            Log.d(TAG, "Sample data refresh needed (storedVersion=$storedVersion, current=$SAMPLE_DATA_VERSION)")
            vehicles.clear()
            loadSampleData()
            FileManager.saveVehicles(vehicles)
            uploadSampleDataToFirestore()
            prefs.edit().putInt(KEY_SAMPLE_VERSION, SAMPLE_DATA_VERSION).apply()
        }

        val savedFavorites = FileManager.loadFavorites()
        favoriteIds.clear()
        favoriteIds.addAll(savedFavorites)
        vehicles.forEach { it.isFavorite = favoriteIds.contains(it.id) }
        isInitialized = true
        startFirestoreListener()
    }

    /**
     * Uploads all 30 sample vehicles to Firestore in a single batch write.
     * Only vehicles with IDs starting with "sample-" are written this way —
     * real user ads are always written individually via addVehicleAsync().
     */
    private fun uploadSampleDataToFirestore() {
        val batch = db.batch()
        vehicles.filter { it.id.startsWith("sample-") }.forEach { vehicle ->
            val ref = db.collection(COLLECTION_VEHICLES).document(vehicle.id)
            batch.set(ref, vehicle)
        }
        batch.commit()
            .addOnSuccessListener { Log.d(TAG, "Sample data uploaded to Firestore successfully") }
            .addOnFailureListener { Log.w(TAG, "Sample data upload failed (offline? will retry next launch)", it) }
    }

    private fun startFirestoreListener() {
        vehiclesListener?.remove()
        vehiclesListener = db.collection(COLLECTION_VEHICLES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Firestore listen error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val firestoreVehicles = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Vehicle::class.java)
                            ?.also { v -> v.isFavorite = favoriteIds.contains(v.id) }
                    }

                    // Merge strategy:
                    // - Keep all Firestore vehicles (real user ads + uploaded sample data)
                    // - Also keep any local-only vehicles (offline ads not yet synced)
                    //   that don't exist in Firestore yet
                    val firestoreIds = firestoreVehicles.map { it.id }.toSet()
                    val localOnlyVehicles = vehicles.filter { it.id !in firestoreIds }

                    vehicles.clear()
                    vehicles.addAll(firestoreVehicles)
                    // Append any local-only offline ads at the front
                    if (localOnlyVehicles.isNotEmpty()) {
                        localOnlyVehicles.forEach { vehicles.add(0, it) }
                    }

                    FileManager.saveVehicles(vehicles)
                    lastSyncedAt = System.currentTimeMillis()
                    Log.d(TAG, "Firestore sync: ${firestoreVehicles.size} cloud + ${localOnlyVehicles.size} local-only = ${vehicles.size} total")
                }
                // If Firestore returns empty (new project / no internet), keep local data as-is
            }
    }

    fun stopListening() {
        vehiclesListener?.remove(); vehiclesListener = null
        searchPreferencesListener?.remove(); searchPreferencesListener = null
    }

    /** Call this to force a full re-init (e.g. after logout/login) */
    fun reset() {
        stopListening()
        vehicles.clear()
        favoriteIds.clear()
        searchPreferences.clear()
        searchHistory.clear()
        isInitialized = false
        lastSyncedAt = 0L
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
        // Clean up local image files
        vehicle?.let {
            val allPaths = it.imageList.toMutableList()
            if (!it.imageUri.isNullOrEmpty() && it.imageUri !in allPaths) allPaths.add(it.imageUri!!)
            com.example.apnivehicle.utils.ImageSaver.deleteImages(allPaths)
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
        val now = System.currentTimeMillis()
        vehicles.addAll(listOf(

            // ── TOYOTA ────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-001",
                title = "Toyota Corolla GLi 1.3 Manual",
                price = 2_850_000, city = "Lahore", year = 2018,
                imageUri = "https://images.unsplash.com/photo-1623859627248-cb5f1d391f4d?q=80&w=1000",
                description = "Well maintained GLi in original condition. Complete service history from Toyota. Genuine 48,000 km, one owner. All documents clear. No accident history.",
                brand = "Toyota", model = "Corolla GLi", mileage = 48000,
                transmission = "Manual", fuelType = "Petrol",
                condition = "Used — Good", color = "White",
                numberOfOwners = 1, sellerId = "seller-001", sellerPhone = "0300-1234567",
                sellerRating = 4.8f, sellerReviewCount = 12, viewCount = 245,
                createdAt = now - 2 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-002",
                title = "Toyota Corolla Altis X 1.6 CVT",
                price = 4_100_000, city = "Karachi", year = 2021,
                imageUri = "https://images.unsplash.com/photo-1550355291-bbee04a92027?q=80&w=1000",
                description = "Top-of-the-line Altis X with push start, leather seats, 7-inch touchscreen. Full factory warranty remaining. Only 22,000 km driven.",
                brand = "Toyota", model = "Corolla Altis", mileage = 22000,
                transmission = "CVT (Auto)", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Silver",
                numberOfOwners = 1, sellerId = "seller-002", sellerPhone = "0321-9876543",
                sellerRating = 5.0f, sellerReviewCount = 28, viewCount = 430,
                createdAt = now - 1 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-003",
                title = "Toyota Fortuner 2.8 Diesel Automatic",
                price = 9_200_000, city = "Islamabad", year = 2021,
                imageUri = "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?q=80&w=1000",
                description = "Full option Fortuner with 4WD, leather interior, JBL sound, sunroof. Perfect for family and off-road use. Fully maintained at Toyota authorised workshop.",
                brand = "Toyota", model = "Fortuner", mileage = 38000,
                transmission = "Automatic", fuelType = "Diesel",
                condition = "Used — Excellent", color = "Pearl White",
                numberOfOwners = 1, sellerId = "seller-003", sellerPhone = "0333-5555666",
                sellerRating = 4.5f, sellerReviewCount = 8, viewCount = 780,
                createdAt = now - 3 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-004",
                title = "Toyota Yaris ATIV CVT",
                price = 2_450_000, city = "Faisalabad", year = 2022,
                imageUri = "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?q=80&w=1000",
                description = "Brand new model in immaculate condition. Auto gear, push start, cruise control. Only 12,000 km. Ideal city car with excellent fuel economy.",
                brand = "Toyota", model = "Yaris", mileage = 12000,
                transmission = "CVT (Auto)", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Beige",
                numberOfOwners = 1, sellerId = "seller-004", sellerPhone = "0345-7778899",
                sellerRating = 4.7f, sellerReviewCount = 15, viewCount = 320,
                createdAt = now - 5 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-005",
                title = "Toyota Hilux Revo Double Cab 2.8D",
                price = 11_500_000, city = "Peshawar", year = 2022,
                imageUri = "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?q=80&w=1000",
                description = "Powerful Revo with full option — leather, navigation, alloy rims. Perfect for business and off-road. 4x4 with diff lock.",
                brand = "Toyota", model = "Hilux Revo", mileage = 45000,
                transmission = "Automatic", fuelType = "Diesel",
                condition = "Used — Good", color = "Grey",
                numberOfOwners = 1, sellerId = "seller-005", sellerPhone = "0301-2223344",
                sellerRating = 4.6f, sellerReviewCount = 10, viewCount = 610,
                createdAt = now - 7 * 24 * 3600_000L
            ),

            // ── HONDA ────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-006",
                title = "Honda Civic Oriel 1.8 Prosmatec",
                price = 3_800_000, city = "Lahore", year = 2019,
                imageUri = "https://images.unsplash.com/photo-1594502184342-2e12f877aa73?q=80&w=1000",
                description = "Fully loaded Civic Oriel with sunroof, climate control, leather seats. Clean car with no accident history. All genuine parts.",
                brand = "Honda", model = "Civic Oriel", mileage = 55000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Good", color = "Black",
                numberOfOwners = 1, sellerId = "seller-001", sellerPhone = "0300-1234567",
                sellerRating = 4.8f, sellerReviewCount = 12, viewCount = 390,
                isFavorite = false,
                createdAt = now - 4 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-007",
                title = "Honda City 1.5 Aspire CVT",
                price = 2_950_000, city = "Rawalpindi", year = 2023,
                imageUri = "https://images.unsplash.com/photo-1580273916550-e323be2ae537?q=80&w=1000",
                description = "Latest gen City with new sporty look. Full option — push start, rear camera, heated mirrors. Only 8,000 km. In showroom condition.",
                brand = "Honda", model = "City Aspire", mileage = 8000,
                transmission = "CVT (Auto)", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Red",
                numberOfOwners = 1, sellerId = "seller-002", sellerPhone = "0321-9876543",
                sellerRating = 5.0f, sellerReviewCount = 28, viewCount = 520,
                createdAt = now - 1 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-008",
                title = "Honda BR-V i-VTEC S CVT",
                price = 3_650_000, city = "Multan", year = 2020,
                imageUri = "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=1000",
                description = "7-seater BR-V in excellent condition. Third row seating, cruise control, lane assist. Ideal family car. Genuine 40,000 km.",
                brand = "Honda", model = "BR-V", mileage = 40000,
                transmission = "CVT (Auto)", fuelType = "Petrol",
                condition = "Used — Good", color = "Dark Blue",
                numberOfOwners = 1, sellerId = "seller-003", sellerPhone = "0333-5555666",
                sellerRating = 4.5f, sellerReviewCount = 8, viewCount = 290,
                createdAt = now - 6 * 24 * 3600_000L
            ),

            // ── SUZUKI ────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-009",
                title = "Suzuki Alto VXR AGS 660cc",
                price = 1_980_000, city = "Karachi", year = 2023,
                imageUri = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?q=80&w=1000",
                description = "Suzuki Alto AGS (auto gear shift) in showroom condition. Best fuel economy in Pakistan at 20km/litre. Perfect for daily commute. First owner.",
                brand = "Suzuki", model = "Alto AGS", mileage = 6000,
                transmission = "Semi-Automatic (AGS)", fuelType = "Petrol",
                condition = "Used — Excellent", color = "White",
                numberOfOwners = 1, sellerId = "seller-004", sellerPhone = "0345-7778899",
                sellerRating = 4.7f, sellerReviewCount = 15, viewCount = 870,
                createdAt = now - 2 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-010",
                title = "Suzuki Swift GL 1.3 Manual",
                price = 2_300_000, city = "Islamabad", year = 2021,
                imageUri = "https://images.unsplash.com/photo-1590362891991-f776e747a588?q=80&w=1000",
                description = "Sporty Swift GL, original paint, original engine. Rear spoiler, alloy rims, LED DRLs. Single owner, non-accidental.",
                brand = "Suzuki", model = "Swift GL", mileage = 32000,
                transmission = "Manual", fuelType = "Petrol",
                condition = "Used — Good", color = "Red",
                numberOfOwners = 1, sellerId = "seller-005", sellerPhone = "0301-2223344",
                sellerRating = 4.6f, sellerReviewCount = 10, viewCount = 410,
                createdAt = now - 3 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-011",
                title = "Suzuki Wagon R VXL AGS",
                price = 2_100_000, city = "Lahore", year = 2022,
                imageUri = "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?q=80&w=1000",
                description = "Latest Wagon R VXL with auto gear shift. Keyless entry, alloy rims, rear sensor. Very economical. Ideal for city use.",
                brand = "Suzuki", model = "Wagon R VXL", mileage = 18000,
                transmission = "Semi-Automatic (AGS)", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Silver",
                numberOfOwners = 1, sellerId = "seller-001", sellerPhone = "0300-1234567",
                sellerRating = 4.8f, sellerReviewCount = 12, viewCount = 330,
                createdAt = now - 5 * 24 * 3600_000L
            ),

            // ── KIA ────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-012",
                title = "KIA Sportage AWD 2.0 Alpha",
                price = 6_800_000, city = "Karachi", year = 2023,
                imageUri = "https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?q=80&w=1000",
                description = "Fully loaded Sportage AWD with panoramic roof, 360 camera, wireless charging, cooled seats. Zero accident, company maintained.",
                brand = "KIA", model = "Sportage AWD", mileage = 15000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Gravity Grey",
                numberOfOwners = 1, sellerId = "seller-002", sellerPhone = "0321-9876543",
                sellerRating = 5.0f, sellerReviewCount = 28, viewCount = 920,
                createdAt = now - 1 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-013",
                title = "KIA Picanto 1.0 Automatic",
                price = 1_950_000, city = "Rawalpindi", year = 2022,
                imageUri = "https://images.unsplash.com/photo-1617469767564-2b7d5e4d9b17?q=80&w=1000",
                description = "Fun-sized city car with auto transmission. Rear camera, alloys, dual airbags. Only 14,000 km. Perfect for a student or first car.",
                brand = "KIA", model = "Picanto", mileage = 14000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Beige",
                numberOfOwners = 1, sellerId = "seller-003", sellerPhone = "0333-5555666",
                sellerRating = 4.5f, sellerReviewCount = 8, viewCount = 480,
                createdAt = now - 4 * 24 * 3600_000L
            ),

            // ── HYUNDAI ────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-014",
                title = "Hyundai Tucson 2.0 AWD Ultimate",
                price = 7_500_000, city = "Islamabad", year = 2022,
                imageUri = "https://images.unsplash.com/photo-1568844293986-8d0400bd4745?q=80&w=1000",
                description = "Top Ultimate trim with ventilated seats, heads-up display, BOSE audio, 8-speed auto. Perfect luxury SUV at a great price.",
                brand = "Hyundai", model = "Tucson AWD", mileage = 28000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Phantom Black",
                numberOfOwners = 1, sellerId = "seller-004", sellerPhone = "0345-7778899",
                sellerRating = 4.7f, sellerReviewCount = 15, viewCount = 680,
                createdAt = now - 2 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-015",
                title = "Hyundai Elantra GLS 2.0 Auto",
                price = 4_500_000, city = "Lahore", year = 2020,
                imageUri = "https://images.unsplash.com/photo-1556189250-72ba954cfc2b?q=80&w=1000",
                description = "Elegant executive sedan. Leather, sunroof, navigation, lane assist. One careful owner, all services done at Hyundai dealer.",
                brand = "Hyundai", model = "Elantra GLS", mileage = 42000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Good", color = "Polar White",
                numberOfOwners = 1, sellerId = "seller-005", sellerPhone = "0301-2223344",
                sellerRating = 4.6f, sellerReviewCount = 10, viewCount = 375,
                createdAt = now - 8 * 24 * 3600_000L
            ),

            // ── CHANGAN ────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-016",
                title = "Changan Alsvin Lumiere CVT",
                price = 2_750_000, city = "Faisalabad", year = 2022,
                imageUri = "https://images.unsplash.com/photo-1614200179396-2bdb77ebf81b?q=80&w=1000",
                description = "Feature-packed Chinese sedan with auto, rear camera, push start, cruise control. Excellent build quality. 5-star safety rating.",
                brand = "Changan", model = "Alsvin Lumiere", mileage = 25000,
                transmission = "CVT (Auto)", fuelType = "Petrol",
                condition = "Used — Good", color = "Champagne Gold",
                numberOfOwners = 1, sellerId = "seller-001", sellerPhone = "0300-1234567",
                sellerRating = 4.8f, sellerReviewCount = 12, viewCount = 290,
                createdAt = now - 6 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-017",
                title = "Changan Oshan X7 Plus 1.5T AWD",
                price = 8_900_000, city = "Karachi", year = 2023,
                imageUri = "https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?q=80&w=1000",
                description = "7-seater luxury SUV with panoramic roof, 12.3-inch infotainment, NAPPA leather, L2 driver assist. Pakistan's best value premium SUV.",
                brand = "Changan", model = "Oshan X7 Plus", mileage = 10000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Haze Blue",
                numberOfOwners = 1, sellerId = "seller-002", sellerPhone = "0321-9876543",
                sellerRating = 5.0f, sellerReviewCount = 28, viewCount = 750,
                createdAt = now - 1 * 24 * 3600_000L
            ),

            // ── MG ────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-018",
                title = "MG HS Exclusive 1.5T Auto",
                price = 6_200_000, city = "Lahore", year = 2022,
                imageUri = "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=1000",
                description = "MG HS Exclusive with panoramic roof, adaptive cruise, lane keeping, 360 camera. Imported quality at a local price.",
                brand = "MG", model = "HS Exclusive", mileage = 20000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Volcano Orange",
                numberOfOwners = 1, sellerId = "seller-003", sellerPhone = "0333-5555666",
                sellerRating = 4.5f, sellerReviewCount = 8, viewCount = 560,
                createdAt = now - 3 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-019",
                title = "MG ZS EV Standard Range",
                price = 8_500_000, city = "Islamabad", year = 2023,
                imageUri = "https://images.unsplash.com/photo-1593941707882-a5bba14938c7?q=80&w=1000",
                description = "Fully electric SUV. 0-100 in 8.5 seconds. 320 km range per charge. No fuel, no oil changes. The future of driving in Pakistan.",
                brand = "MG", model = "ZS EV", mileage = 5000,
                transmission = "Automatic", fuelType = "Electric",
                condition = "Used — Excellent", color = "Dover White",
                numberOfOwners = 1, sellerId = "seller-004", sellerPhone = "0345-7778899",
                sellerRating = 4.7f, sellerReviewCount = 15, viewCount = 1200,
                createdAt = now - 2 * 24 * 3600_000L
            ),

            // ── HAVAL ─────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-020",
                title = "Haval H6 HEV 1.5T Hybrid",
                price = 9_800_000, city = "Karachi", year = 2023,
                imageUri = "https://images.unsplash.com/photo-1568844293986-8d0400bd4745?q=80&w=1000",
                description = "Pakistan's best-selling hybrid SUV. Panoramic roof, NAPPA leather, 12.3\" dual screens, Kenwood sound. Certified stock by HMPL.",
                brand = "Haval", model = "H6 HEV", mileage = 12000,
                transmission = "DCT", fuelType = "Hybrid (Petrol+Electric)",
                condition = "Used — Excellent", color = "Snow White Pearl",
                numberOfOwners = 1, sellerId = "seller-005", sellerPhone = "0301-2223344",
                sellerRating = 4.6f, sellerReviewCount = 10, viewCount = 890,
                createdAt = now - 1 * 24 * 3600_000L
            ),

            // ── NISSAN ─────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-021",
                title = "Nissan Dayz 660cc Automatic",
                price = 1_650_000, city = "Multan", year = 2019,
                imageUri = "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?q=80&w=1000",
                description = "Japanese imported kei car. Very low maintenance, excellent fuel economy. Auto gear, power windows, AC. Perfect for small family.",
                brand = "Nissan", model = "Dayz", mileage = 55000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Good", color = "White",
                numberOfOwners = 2, sellerId = "seller-001", sellerPhone = "0300-1234567",
                sellerRating = 4.8f, sellerReviewCount = 12, viewCount = 340,
                createdAt = now - 9 * 24 * 3600_000L
            ),

            // ── DAIHATSU ─────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-022",
                title = "Daihatsu Mira ES 660cc Auto",
                price = 1_550_000, city = "Sialkot", year = 2018,
                imageUri = "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?q=80&w=1000",
                description = "Imported Mira ES with auto, push start, rear sensor. Very economical at 22 km/litre. Clean body, no rust. Original auction sheet available.",
                brand = "Daihatsu", model = "Mira ES", mileage = 65000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Good", color = "White",
                numberOfOwners = 1, sellerId = "seller-002", sellerPhone = "0321-9876543",
                sellerRating = 5.0f, sellerReviewCount = 28, viewCount = 460,
                createdAt = now - 10 * 24 * 3600_000L
            ),

            // ── MITSUBISHI ─────────────────────────────────────────────────────
            Vehicle(
                id = "sample-023",
                title = "Mitsubishi Pajero 3.5 V6 Full Option",
                price = 7_200_000, city = "Lahore", year = 2008,
                imageUri = "https://images.unsplash.com/photo-1625231336566-c0a8d41bb73e?q=80&w=1000",
                description = "Classic Pajero in original condition. 3.5L V6 petrol engine with 4WD super select. Lifted suspension, all-terrain tyres. Perfect for adventure.",
                brand = "Mitsubishi", model = "Pajero", mileage = 120000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Good", color = "Silver",
                numberOfOwners = 2, sellerId = "seller-003", sellerPhone = "0333-5555666",
                sellerRating = 4.5f, sellerReviewCount = 8, viewCount = 520,
                createdAt = now - 12 * 24 * 3600_000L
            ),

            // ── BIKES / MOTORCYCLES ─────────────────────────────────────────────
            Vehicle(
                id = "sample-024",
                title = "Honda CB150F Euro II",
                price = 280_000, city = "Lahore", year = 2022,
                imageUri = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?q=80&w=1000",
                description = "Honda CB150F sports commuter in perfect condition. Only 8,000 km. Original tool kit and documents. Never dropped or accidental.",
                brand = "Honda", model = "CB150F", mileage = 8000,
                transmission = "Manual", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Black",
                type = com.example.apnivehicle.models.VehicleType.MOTORCYCLE,
                numberOfOwners = 1, sellerId = "seller-004", sellerPhone = "0345-7778899",
                sellerRating = 4.7f, sellerReviewCount = 15, viewCount = 620,
                createdAt = now - 4 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-025",
                title = "Suzuki GR150 Sport Bike",
                price = 350_000, city = "Karachi", year = 2023,
                imageUri = "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?q=80&w=1000",
                description = "Sporty GR150 with fully digital instrument cluster, mono-shock suspension, disc brakes front and rear. Barely used — only 3,000 km.",
                brand = "Suzuki", model = "GR150", mileage = 3000,
                transmission = "Manual", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Blue",
                type = com.example.apnivehicle.models.VehicleType.MOTORCYCLE,
                numberOfOwners = 1, sellerId = "seller-005", sellerPhone = "0301-2223344",
                sellerRating = 4.6f, sellerReviewCount = 10, viewCount = 430,
                createdAt = now - 3 * 24 * 3600_000L
            ),

            // ── JEEP / SUV ─────────────────────────────────────────────────────
            Vehicle(
                id = "sample-026",
                title = "Land Rover Defender 90 V8",
                price = 42_000_000, city = "Lahore", year = 2022,
                imageUri = "https://images.unsplash.com/photo-1617469767564-2b7d5e4d9b17?q=80&w=1000",
                description = "Rare Defender 90 V8 in Carpathian Grey. 518 hp, 0-100 in 4.9s. Full option — surround camera, air suspension, pivi pro. Just 10,000 km.",
                brand = "Land Rover", model = "Defender", mileage = 10000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Carpathian Grey",
                type = com.example.apnivehicle.models.VehicleType.JEEP,
                numberOfOwners = 1, sellerId = "seller-001", sellerPhone = "0300-1234567",
                sellerRating = 4.8f, sellerReviewCount = 12, viewCount = 1850,
                createdAt = now - 1 * 24 * 3600_000L
            ),
            Vehicle(
                id = "sample-027",
                title = "Jeep Wrangler Unlimited Rubicon",
                price = 18_500_000, city = "Islamabad", year = 2020,
                imageUri = "https://images.unsplash.com/photo-1625231336566-c0a8d41bb73e?q=80&w=1000",
                description = "Iconic Wrangler Rubicon 4-door. 3.6L Pentastar V6. Soft top + hard top both included. Rock-Trac 4WD, electric sway bar. Import via personal baggage.",
                brand = "Jeep", model = "Wrangler", mileage = 35000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Good", color = "Firecracker Red",
                type = com.example.apnivehicle.models.VehicleType.JEEP,
                numberOfOwners = 1, sellerId = "seller-002", sellerPhone = "0321-9876543",
                sellerRating = 5.0f, sellerReviewCount = 28, viewCount = 2100,
                createdAt = now - 5 * 24 * 3600_000L
            ),

            // ── VAN ─────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-028",
                title = "Toyota Hiace GL Grand Cabin",
                price = 7_800_000, city = "Karachi", year = 2019,
                imageUri = "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?q=80&w=1000",
                description = "Grand Cabin 14-seater in excellent condition. Converted to luxury seating with individual AC vents. Used for corporate travel. All books clear.",
                brand = "Toyota", model = "Hiace", mileage = 85000,
                transmission = "Manual", fuelType = "Diesel",
                condition = "Used — Good", color = "White",
                type = com.example.apnivehicle.models.VehicleType.VAN,
                numberOfOwners = 1, sellerId = "seller-003", sellerPhone = "0333-5555666",
                sellerRating = 4.5f, sellerReviewCount = 8, viewCount = 310,
                createdAt = now - 14 * 24 * 3600_000L
            ),

            // ── TRUCK ─────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-029",
                title = "Isuzu D-Max 4x4 LS-U AT",
                price = 8_200_000, city = "Lahore", year = 2021,
                imageUri = "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?q=80&w=1000",
                description = "Double-cab pickup 4x4. Full option with leather, navigation, 9-speed auto. Perfect for business or adventure. Only 30,000 km driven.",
                brand = "Isuzu", model = "D-Max", mileage = 30000,
                transmission = "Automatic", fuelType = "Diesel",
                condition = "Used — Excellent", color = "White",
                type = com.example.apnivehicle.models.VehicleType.TRUCK,
                numberOfOwners = 1, sellerId = "seller-004", sellerPhone = "0345-7778899",
                sellerRating = 4.7f, sellerReviewCount = 15, viewCount = 440,
                createdAt = now - 8 * 24 * 3600_000L
            ),

            // ── LUXURY ─────────────────────────────────────────────────────────
            Vehicle(
                id = "sample-030",
                title = "Mercedes-Benz C200 AMG Line 2.0T",
                price = 16_500_000, city = "Lahore", year = 2021,
                imageUri = "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?q=80&w=1000",
                description = "C200 AMG Line with Burmester sound, panoramic roof, 64-colour ambient lighting, MBUX. Pakistan registered, fully maintained at MCPL.",
                brand = "Mercedes-Benz", model = "C200", mileage = 28000,
                transmission = "Automatic", fuelType = "Petrol",
                condition = "Used — Excellent", color = "Obsidian Black",
                numberOfOwners = 1, sellerId = "seller-005", sellerPhone = "0301-2223344",
                sellerRating = 4.6f, sellerReviewCount = 10, viewCount = 1450,
                createdAt = now - 2 * 24 * 3600_000L
            )
        ))
    }
}
