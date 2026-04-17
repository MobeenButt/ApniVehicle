package com.example.apnivehicle.models

data class VehicleAnalytics(
    val vehicleId: String,
    val viewCount: Int = 0,
    val favoriteCount: Int = 0,
    val contactClicks: Int = 0,
    val shareCount: Int = 0,
    val dailyViews: MutableMap<String, Int> = mutableMapOf(), // date -> count
    val lastUpdated: Long = System.currentTimeMillis()
)

data class SellerAnalytics(
    val totalViews: Int = 0,
    val totalListings: Int = 0,
    val activeListings: Int = 0,
    val totalFavorites: Int = 0,
    val totalContacts: Int = 0,
    val averagePrice: Long = 0,
    val mostViewedVehicle: Vehicle? = null,
    val leastViewedVehicle: Vehicle? = null,
    val performanceScore: Float = 0f, // 0-100
    val bestPerformingCategory: String = "",
    val marketPosition: String = "" // Above/Below/At market average
)

data class MarketTrend(
    val category: String,
    val averagePrice: Long,
    val totalListings: Int,
    val averageViews: Int,
    val trendDirection: TrendDirection
)

enum class TrendDirection {
    UP, DOWN, STABLE
}

data class PriceComparison(
    val yourPrice: Long,
    val marketAverage: Long,
    val lowestPrice: Long,
    val highestPrice: Long,
    val pricePosition: PricePosition,
    val recommendation: String
)

enum class PricePosition {
    BELOW_MARKET, AT_MARKET, ABOVE_MARKET, OVERPRICED
}
