package com.example.apnivehicle.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.apnivehicle.models.*
import com.example.apnivehicle.repository.VehicleRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

object AnalyticsManager {
    
    private const val PREFS_NAME = "analytics_prefs"
    private const val KEY_VEHICLE_ANALYTICS = "vehicle_analytics"
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Track vehicle view
    fun trackVehicleView(context: Context, vehicleId: String) {
        val analytics = getVehicleAnalytics(context)
        val vehicleAnalytics = analytics[vehicleId] ?: VehicleAnalytics(vehicleId)
        
        val today = dateFormat.format(Date())
        val dailyViews = vehicleAnalytics.dailyViews.toMutableMap()
        dailyViews[today] = (dailyViews[today] ?: 0) + 1
        
        val updated = vehicleAnalytics.copy(
            viewCount = vehicleAnalytics.viewCount + 1,
            dailyViews = dailyViews,
            lastUpdated = System.currentTimeMillis()
        )
        
        analytics[vehicleId] = updated
        saveVehicleAnalytics(context, analytics)
    }
    
    // Track favorite
    fun trackFavorite(context: Context, vehicleId: String, isFavorite: Boolean) {
        val analytics = getVehicleAnalytics(context)
        val vehicleAnalytics = analytics[vehicleId] ?: VehicleAnalytics(vehicleId)
        
        val updated = vehicleAnalytics.copy(
            favoriteCount = if (isFavorite) 
                vehicleAnalytics.favoriteCount + 1 
            else 
                maxOf(0, vehicleAnalytics.favoriteCount - 1),
            lastUpdated = System.currentTimeMillis()
        )
        
        analytics[vehicleId] = updated
        saveVehicleAnalytics(context, analytics)
    }
    
    // Track contact click (call/message)
    fun trackContactClick(context: Context, vehicleId: String) {
        val analytics = getVehicleAnalytics(context)
        val vehicleAnalytics = analytics[vehicleId] ?: VehicleAnalytics(vehicleId)
        
        val updated = vehicleAnalytics.copy(
            contactClicks = vehicleAnalytics.contactClicks + 1,
            lastUpdated = System.currentTimeMillis()
        )
        
        analytics[vehicleId] = updated
        saveVehicleAnalytics(context, analytics)
    }
    
    // Track share
    fun trackShare(context: Context, vehicleId: String) {
        val analytics = getVehicleAnalytics(context)
        val vehicleAnalytics = analytics[vehicleId] ?: VehicleAnalytics(vehicleId)
        
        val updated = vehicleAnalytics.copy(
            shareCount = vehicleAnalytics.shareCount + 1,
            lastUpdated = System.currentTimeMillis()
        )
        
        analytics[vehicleId] = updated
        saveVehicleAnalytics(context, analytics)
    }
    
    // Get analytics for specific vehicle
    fun getVehicleAnalyticsData(context: Context, vehicleId: String): VehicleAnalytics {
        val analytics = getVehicleAnalytics(context)
        return analytics[vehicleId] ?: VehicleAnalytics(vehicleId)
    }
    
    // Get seller analytics
    fun getSellerAnalytics(context: Context): SellerAnalytics {
        val myVehicles = VehicleRepository.getMyAds()
        val analytics = getVehicleAnalytics(context)
        
        if (myVehicles.isEmpty()) {
            return SellerAnalytics()
        }
        
        var totalViews = 0
        var totalFavorites = 0
        var totalContacts = 0
        var totalPrice = 0L
        var mostViewed: Vehicle? = null
        var leastViewed: Vehicle? = null
        var maxViews = 0
        var minViews = Int.MAX_VALUE
        
        val categoryViews = mutableMapOf<String, Int>()
        
        myVehicles.forEach { vehicle ->
            val vehicleAnalytics = analytics[vehicle.id] ?: VehicleAnalytics(vehicle.id)
            
            totalViews += vehicleAnalytics.viewCount
            totalFavorites += vehicleAnalytics.favoriteCount
            totalContacts += vehicleAnalytics.contactClicks
            totalPrice += vehicle.price
            
            // Track most/least viewed
            if (vehicleAnalytics.viewCount > maxViews) {
                maxViews = vehicleAnalytics.viewCount
                mostViewed = vehicle
            }
            if (vehicleAnalytics.viewCount < minViews) {
                minViews = vehicleAnalytics.viewCount
                leastViewed = vehicle
            }
            
            // Track category performance
            val category = vehicle.type.name
            categoryViews[category] = (categoryViews[category] ?: 0) + vehicleAnalytics.viewCount
        }
        
        val averagePrice = if (myVehicles.isNotEmpty()) totalPrice / myVehicles.size else 0L
        val bestCategory = categoryViews.maxByOrNull { it.value }?.key ?: "N/A"
        
        // Calculate performance score (0-100)
        val avgViewsPerVehicle = if (myVehicles.isNotEmpty()) totalViews.toFloat() / myVehicles.size else 0f
        val avgFavoritesPerVehicle = if (myVehicles.isNotEmpty()) totalFavorites.toFloat() / myVehicles.size else 0f
        val avgContactsPerVehicle = if (myVehicles.isNotEmpty()) totalContacts.toFloat() / myVehicles.size else 0f
        
        val performanceScore = minOf(100f, 
            (avgViewsPerVehicle * 0.4f) + 
            (avgFavoritesPerVehicle * 2f) + 
            (avgContactsPerVehicle * 5f)
        )
        
        // Market position
        val allVehicles = VehicleRepository.getVehicles()
        val marketAverage = if (allVehicles.isNotEmpty()) 
            allVehicles.sumOf { it.price } / allVehicles.size 
        else 0L
        
        val marketPosition = when {
            averagePrice < marketAverage * 0.9 -> "Below Market"
            averagePrice > marketAverage * 1.1 -> "Above Market"
            else -> "At Market Average"
        }
        
        return SellerAnalytics(
            totalViews = totalViews,
            totalListings = myVehicles.size,
            activeListings = myVehicles.size,
            totalFavorites = totalFavorites,
            totalContacts = totalContacts,
            averagePrice = averagePrice,
            mostViewedVehicle = mostViewed,
            leastViewedVehicle = leastViewed,
            performanceScore = performanceScore,
            bestPerformingCategory = bestCategory,
            marketPosition = marketPosition
        )
    }
    
    // Get market trends
    fun getMarketTrends(context: Context): List<MarketTrend> {
        val allVehicles = VehicleRepository.getVehicles()
        val analytics = getVehicleAnalytics(context)
        
        val trends = mutableListOf<MarketTrend>()
        
        // Group by vehicle type
        val groupedVehicles = allVehicles.groupBy { it.type.name }
        
        groupedVehicles.forEach { (category, vehicles) ->
            val avgPrice = if (vehicles.isNotEmpty()) 
                vehicles.sumOf { it.price } / vehicles.size 
            else 0L
            
            val totalViews = vehicles.sumOf { vehicle ->
                analytics[vehicle.id]?.viewCount ?: 0
            }
            
            val avgViews = if (vehicles.isNotEmpty()) totalViews / vehicles.size else 0
            
            // Simple trend calculation (can be enhanced with historical data)
            val trendDirection = when {
                avgViews > 50 -> TrendDirection.UP
                avgViews < 20 -> TrendDirection.DOWN
                else -> TrendDirection.STABLE
            }
            
            trends.add(MarketTrend(
                category = category,
                averagePrice = avgPrice,
                totalListings = vehicles.size,
                averageViews = avgViews,
                trendDirection = trendDirection
            ))
        }
        
        return trends.sortedByDescending { it.averageViews }
    }
    
    // Get price comparison for a vehicle
    fun getPriceComparison(context: Context, vehicleId: String): PriceComparison? {
        val vehicle = VehicleRepository.getVehicleById(vehicleId) ?: return null
        val similarVehicles = VehicleRepository.getVehicles().filter { 
            it.type == vehicle.type && it.id != vehicleId 
        }
        
        if (similarVehicles.isEmpty()) {
            return PriceComparison(
                yourPrice = vehicle.price,
                marketAverage = vehicle.price,
                lowestPrice = vehicle.price,
                highestPrice = vehicle.price,
                pricePosition = PricePosition.AT_MARKET,
                recommendation = "No similar vehicles to compare"
            )
        }
        
        val prices = similarVehicles.map { it.price }
        val avgPrice = prices.average().toLong()
        val minPrice = prices.minOrNull() ?: 0L
        val maxPrice = prices.maxOrNull() ?: 0L
        
        val position = when {
            vehicle.price < avgPrice * 0.85 -> PricePosition.BELOW_MARKET
            vehicle.price > avgPrice * 1.15 -> PricePosition.OVERPRICED
            vehicle.price > avgPrice * 1.05 -> PricePosition.ABOVE_MARKET
            else -> PricePosition.AT_MARKET
        }
        
        val recommendation = when (position) {
            PricePosition.BELOW_MARKET -> "Your price is competitive! Consider increasing slightly."
            PricePosition.AT_MARKET -> "Your price is well-positioned in the market."
            PricePosition.ABOVE_MARKET -> "Your price is slightly high. Monitor competition."
            PricePosition.OVERPRICED -> "Consider reducing price to attract more buyers."
        }
        
        return PriceComparison(
            yourPrice = vehicle.price,
            marketAverage = avgPrice,
            lowestPrice = minPrice,
            highestPrice = maxPrice,
            pricePosition = position,
            recommendation = recommendation
        )
    }
    
    // Get best time to sell analysis
    fun getBestTimeToSell(context: Context, vehicleId: String): String {
        val analytics = getVehicleAnalyticsData(context, vehicleId)
        
        if (analytics.dailyViews.isEmpty()) {
            return "Not enough data yet. Keep your listing active!"
        }
        
        // Analyze daily views pattern
        val viewsByDay = analytics.dailyViews.entries
            .sortedByDescending { it.value }
            .take(3)
        
        return if (viewsByDay.isNotEmpty()) {
            val bestDay = viewsByDay.first()
            "Peak interest detected! Your listing gets most views recently. " +
            "Current momentum is good for selling."
        } else {
            "Maintain your listing and respond quickly to inquiries."
        }
    }
    
    // Private helper methods
    private fun getVehicleAnalytics(context: Context): MutableMap<String, VehicleAnalytics> {
        val json = getPrefs(context).getString(KEY_VEHICLE_ANALYTICS, null) ?: return mutableMapOf()
        val type = object : TypeToken<MutableMap<String, VehicleAnalytics>>() {}.type
        return try {
            gson.fromJson(json, type) ?: mutableMapOf()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }
    
    private fun saveVehicleAnalytics(context: Context, analytics: Map<String, VehicleAnalytics>) {
        val json = gson.toJson(analytics)
        getPrefs(context).edit().putString(KEY_VEHICLE_ANALYTICS, json).apply()
    }
    
    // Clear analytics (for testing)
    fun clearAnalytics(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
