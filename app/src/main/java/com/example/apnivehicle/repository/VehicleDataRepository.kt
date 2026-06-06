package com.example.apnivehicle.repository

import android.content.Context
import android.util.Log
import com.example.apnivehicle.api.ApiClient
import com.example.apnivehicle.db.CachedMake
import com.example.apnivehicle.db.CachedModel
import com.example.apnivehicle.db.VehicleDataCache
import com.example.apnivehicle.utils.Constants
import java.util.concurrent.TimeUnit

/**
 * VehicleDataRepository — fetches makes/models from CarQuery API with 24-hour Room cache.
 * Falls back to Constants.VEHICLE_MAKES when API and cache are both unavailable.
 */
class VehicleDataRepository(context: Context) {

    private val TAG = "VehicleDataRepository"
    private val db = VehicleDataCache.getInstance(context)
    private val api = ApiClient.carQueryApi
    private val TTL_MS = TimeUnit.HOURS.toMillis(24)

    // ===== Makes =====

    suspend fun getMakes(): List<String> {
        return try {
            val lastCached = db.makeDao().getLastCachedAt() ?: 0L
            val isStale = System.currentTimeMillis() - lastCached > TTL_MS

            if (!isStale) {
                val cached = db.makeDao().getAllMakes()
                if (cached.isNotEmpty()) return cached.map { it.makeDisplay }.sorted()
            }

            // Fetch from API
            val response = api.getMakes()
            val makes = response.makes
            if (makes.isNotEmpty()) {
                db.makeDao().clearAll()
                db.makeDao().insertAll(makes.map { CachedMake(it.makeId, it.makeDisplay, it.makeCountry) })
                makes.map { it.makeDisplay }.sorted()
            } else {
                fallbackMakes()
            }
        } catch (e: Exception) {
            Log.w(TAG, "getMakes API failed, using cache/fallback", e)
            val cached = db.makeDao().getAllMakes()
            if (cached.isNotEmpty()) cached.map { it.makeDisplay }.sorted()
            else fallbackMakes()
        }
    }

    // ===== Models =====

    suspend fun getModels(make: String): List<String> {
        return try {
            val makeId = make.lowercase().replace(" ", "_")
            val lastCached = db.modelDao().getLastCachedAt(makeId) ?: 0L
            val isStale = System.currentTimeMillis() - lastCached > TTL_MS

            if (!isStale) {
                val cached = db.modelDao().getModelsForMake(makeId)
                if (cached.isNotEmpty()) return cached.map { it.modelName }.distinct().sorted()
            }

            val response = api.getModels(make)
            val models = response.models
            if (models.isNotEmpty()) {
                db.modelDao().clearForMake(makeId)
                db.modelDao().insertAll(models.map { CachedModel("${makeId}_${it.modelName}", makeId, it.modelName) })
                models.map { it.modelName }.distinct().sorted()
            } else {
                fallbackModels(make)
            }
        } catch (e: Exception) {
            Log.w(TAG, "getModels API failed for $make, using cache/fallback", e)
            val makeId = make.lowercase().replace(" ", "_")
            val cached = db.modelDao().getModelsForMake(makeId)
            if (cached.isNotEmpty()) cached.map { it.modelName }.distinct().sorted()
            else fallbackModels(make)
        }
    }

    // ===== Years =====

    fun getYears(): List<Int> {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return (currentYear downTo 1980).toList()
    }

    // ===== Fallbacks =====

    private fun fallbackMakes(): List<String> = Constants.VEHICLE_MAKES

    private fun fallbackModels(make: String): List<String> {
        return when (make.lowercase()) {
            "toyota" -> listOf("Corolla", "Yaris", "Fortuner", "Hilux", "Land Cruiser", "Prado", "Camry", "Vitz", "Aqua", "Prius")
            "honda" -> listOf("Civic", "City", "Accord", "BR-V", "HR-V", "Vezel", "Fit", "N-Box", "Jazz")
            "suzuki" -> listOf("Alto", "Cultus", "Swift", "Wagon R", "Bolan", "Ravi", "Jimny", "Vitara", "Mehran")
            "hyundai" -> listOf("Elantra", "Sonata", "Tucson", "Santa Fe", "Grand Starex", "Ioniq", "Creta")
            "kia" -> listOf("Sportage", "Picanto", "Stonic", "Sorento", "Carnival", "Seltos")
            "nissan" -> listOf("Sunny", "Dayz", "Juke", "X-Trail", "Patrol", "Navara")
            "mitsubishi" -> listOf("Lancer", "Pajero", "Outlander", "Eclipse Cross", "L200")
            "daihatsu" -> listOf("Mira", "Move", "Cuore", "Terios", "Hijet")
            "mg" -> listOf("HS", "ZS", "RX5", "GT", "5")
            "changan" -> listOf("Alsvin", "Oshan X7", "Karvaan", "M9")
            else -> emptyList()
        }.sorted()
    }
}
