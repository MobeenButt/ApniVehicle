package com.example.apnivehicle.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object FormatUtils {
    
    // Format price with PKR symbol
    fun formatPrice(price: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("en").setRegion("PK").build())
        return "PKR ${formatter.format(price)}"
    }
    
    // Format mileage with KM suffix
    fun formatMileage(mileage: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("en").setRegion("PK").build())
        return "${formatter.format(mileage)} KM"
    }
    
    // Format relative time (e.g., "2 hours ago", "3 days ago")
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days ${if (days == 1L) "day" else "days"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(30) -> {
                val weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7
                "$weeks ${if (weeks == 1L) "week" else "weeks"} ago"
            }
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            }
        }
    }
    
    // Pakistan cities list
    fun getPakistanCities(): List<String> {
        return listOf(
            "Karachi", "Lahore", "Islamabad", "Rawalpindi", "Faisalabad",
            "Multan", "Peshawar", "Quetta", "Sialkot", "Gujranwala",
            "Hyderabad", "Abbottabad", "Sargodha", "Bahawalpur", "Sukkur",
            "Mardan", "Mingora", "Sheikhupura", "Jhang", "Rahim Yar Khan",
            "Gujrat", "Kasur", "Dera Ghazi Khan", "Sahiwal", "Nawabshah",
            "Okara", "Mirpur Khas", "Chiniot", "Kamoke", "Mandi Bahauddin"
        ).sorted()
    }
    
    // Vehicle makes/brands
    fun getVehicleMakes(): List<String> {
        return listOf(
            "Toyota", "Honda", "Suzuki", "Daihatsu", "Nissan",
            "Mitsubishi", "Hyundai", "KIA", "Mercedes-Benz", "BMW",
            "Audi", "Mazda", "Changan", "MG", "Proton",
            "Prince", "United", "FAW", "DFSK", "Haval"
        ).sorted()
    }
    
    // Get models for a specific make (sample data)
    fun getModelsForMake(make: String): List<String> {
        return when (make.lowercase()) {
            "toyota" -> listOf("Corolla", "Yaris", "Fortuner", "Hilux", "Land Cruiser", "Prado", "Camry", "Vitz", "Aqua")
            "honda" -> listOf("Civic", "City", "Accord", "BR-V", "HR-V", "Vezel", "Fit", "N-Box")
            "suzuki" -> listOf("Alto", "Cultus", "Swift", "Wagon R", "Bolan", "Ravi", "Jimny", "Vitara")
            "hyundai" -> listOf("Elantra", "Sonata", "Tucson", "Santa Fe", "Grand Starex", "Ioniq")
            "kia" -> listOf("Sportage", "Picanto", "Stonic", "Sorento", "Carnival")
            else -> emptyList()
        }.sorted()
    }
}
