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
 * VehicleDataRepository
 *
 * Strategy (in priority order):
 * 1. Constants.VEHICLE_MODELS / Constants.VEHICLE_MAKES — instant, always available.
 * 2. Room cache (24-hour TTL) — persists data from previous successful API call.
 * 3. CarQuery API — fetches fresh data when cache is stale and network is available.
 *
 * For makes/models the Constants data is the most reliable source for the Pakistan
 * market. The API is still tried so we can surface less-common makes.
 */
class VehicleDataRepository(context: Context) {

    private val TAG = "VehicleDataRepository"
    private val db  = VehicleDataCache.getInstance(context)
    private val api = ApiClient.carQueryApi
    private val TTL_MS = TimeUnit.HOURS.toMillis(24)

    // ── Makes ─────────────────────────────────────────────────────────────────

    suspend fun getMakes(): List<String> {
        // Always start with our curated Pakistan list
        val constantsMakes = Constants.VEHICLE_MAKES.toMutableSet()

        return try {
            val lastCached = db.makeDao().getLastCachedAt() ?: 0L
            val isStale    = System.currentTimeMillis() - lastCached > TTL_MS

            if (!isStale) {
                val cached = db.makeDao().getAllMakes()
                if (cached.isNotEmpty()) {
                    // Merge cache with constants so we never lose Pakistan-specific makes
                    val merged = (constantsMakes + cached.map { it.makeDisplay }).toSortedSet()
                    return merged.toList()
                }
            }

            // Fetch from CarQuery
            val response = api.getMakes()
            val apiMakes = response.makes
            if (apiMakes.isNotEmpty()) {
                db.makeDao().clearAll()
                db.makeDao().insertAll(apiMakes.map { CachedMake(it.makeId, it.makeDisplay, it.makeCountry) })
                // Merge API results with our curated Pakistan list
                (constantsMakes + apiMakes.map { it.makeDisplay }).toSortedSet().toList()
            } else {
                constantsMakes.toSortedSet().toList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "getMakes API failed, using cache/Constants fallback", e)
            try {
                val cached = db.makeDao().getAllMakes()
                if (cached.isNotEmpty()) {
                    (constantsMakes + cached.map { it.makeDisplay }).toSortedSet().toList()
                } else {
                    constantsMakes.toSortedSet().toList()
                }
            } catch (_: Exception) {
                constantsMakes.toSortedSet().toList()
            }
        }
    }

    // ── Models ────────────────────────────────────────────────────────────────

    /**
     * Returns models for a given make.
     * Instantly returns from Constants if available (covers all major Pakistan brands),
     * then tries Room cache and CarQuery API to supplement with less-common models.
     */
    suspend fun getModels(make: String): List<String> {
        // Instant result from our curated map — no network needed
        val constantsModels = getLocalModels(make).toMutableSet()

        return try {
            val makeId     = make.lowercase().replace(" ", "_")
            val lastCached = db.modelDao().getLastCachedAt(makeId) ?: 0L
            val isStale    = System.currentTimeMillis() - lastCached > TTL_MS

            if (!isStale) {
                val cached = db.modelDao().getModelsForMake(makeId)
                if (cached.isNotEmpty()) {
                    return (constantsModels + cached.map { it.modelName }).toSortedSet().toList()
                }
            }

            val response = api.getModels(make)
            val apiModels = response.models
            if (apiModels.isNotEmpty()) {
                db.modelDao().clearForMake(makeId)
                db.modelDao().insertAll(
                    apiModels.map { CachedModel("${makeId}_${it.modelName}", makeId, it.modelName) }
                )
                (constantsModels + apiModels.map { it.modelName }).toSortedSet().toList()
            } else {
                constantsModels.toSortedSet().toList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "getModels API failed for $make, using cache/Constants", e)
            try {
                val makeId = make.lowercase().replace(" ", "_")
                val cached = db.modelDao().getModelsForMake(makeId)
                if (cached.isNotEmpty()) {
                    (constantsModels + cached.map { it.modelName }).toSortedSet().toList()
                } else {
                    constantsModels.toSortedSet().toList()
                }
            } catch (_: Exception) {
                constantsModels.toSortedSet().toList()
            }
        }
    }

    /**
     * Synchronous instant lookup — returns models from Constants without any
     * network or database call. Use this for immediate dropdown population.
     */
    fun getLocalModels(make: String): List<String> {
        // Try exact key first
        Constants.VEHICLE_MODELS[make]?.let { return it.sorted() }
        // Try case-insensitive match
        val key = Constants.VEHICLE_MODELS.keys.firstOrNull {
            it.equals(make, ignoreCase = true)
        }
        return Constants.VEHICLE_MODELS[key]?.sorted() ?: emptyList()
    }

    // ── Years ─────────────────────────────────────────────────────────────────

    fun getYears(): List<Int> {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return (currentYear downTo Constants.MIN_YEAR).toList()
    }

    // ── Makes that have locally known models ──────────────────────────────────

    fun getMakesWithLocalModels(): List<String> = Constants.VEHICLE_MODELS.keys.sorted()
}
