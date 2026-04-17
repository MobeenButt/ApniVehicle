package com.example.apnivehicle.utils

/**
 * Application-wide constants
 */
object Constants {
    
    // App Info
    const val APP_VERSION = "2.0"
    const val APP_NAME = "ApniVehicle"
    
    // Validation Limits
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 50
    const val MIN_NAME_LENGTH = 3
    const val MAX_NAME_LENGTH = 50
    const val MIN_DESCRIPTION_LENGTH = 20
    const val MAX_DESCRIPTION_LENGTH = 1000
    
    // Vehicle Limits
    const val MIN_PRICE = 1L
    const val MAX_PRICE = 999_999_999L
    const val MIN_YEAR = 1980
    const val MIN_MILEAGE = 0
    const val MAX_MILEAGE = 999_999
    const val MAX_IMAGES = 8
    const val MIN_IMAGES = 1
    
    // Image Settings
    const val IMAGE_QUALITY = 80
    const val MAX_IMAGE_WIDTH = 1920
    const val MAX_IMAGE_HEIGHT = 1920
    
    // Pakistani Cities (Major Cities)
    val PAKISTANI_CITIES = listOf(
        "Karachi",
        "Lahore",
        "Islamabad",
        "Rawalpindi",
        "Faisalabad",
        "Multan",
        "Peshawar",
        "Quetta",
        "Sialkot",
        "Gujranwala",
        "Hyderabad",
        "Bahawalpur",
        "Sargodha",
        "Sukkur",
        "Larkana",
        "Sheikhupura",
        "Jhang",
        "Rahim Yar Khan",
        "Gujrat",
        "Kasur",
        "Mardan",
        "Mingora",
        "Sahiwal",
        "Nawabshah",
        "Okara",
        "Abbottabad",
        "Mirpur Khas",
        "Chiniot",
        "Sadiqabad",
        "Burewala"
    ).sorted()
    
    // Vehicle Makes (Popular in Pakistan)
    val VEHICLE_MAKES = listOf(
        "Toyota",
        "Honda",
        "Suzuki",
        "Hyundai",
        "KIA",
        "Nissan",
        "Daihatsu",
        "Mitsubishi",
        "Mercedes-Benz",
        "BMW",
        "Audi",
        "Changan",
        "MG",
        "Proton",
        "FAW",
        "Prince",
        "United",
        "Mazda",
        "Volkswagen",
        "Chevrolet",
        "Ford",
        "Land Rover",
        "Lexus",
        "Porsche",
        "Other"
    ).sorted()
    
    // Fuel Types
    val FUEL_TYPES = listOf(
        "Petrol",
        "Diesel",
        "CNG",
        "Hybrid",
        "Electric",
        "LPG"
    )
    
    // Transmission Types
    val TRANSMISSION_TYPES = listOf(
        "Manual",
        "Automatic",
        "Semi-Automatic"
    )
    
    // Vehicle Conditions
    val VEHICLE_CONDITIONS = listOf(
        "New",
        "Used",
        "Certified Pre-Owned",
        "Accidental"
    )
    
    // Colors
    val VEHICLE_COLORS = listOf(
        "White",
        "Black",
        "Silver",
        "Grey",
        "Red",
        "Blue",
        "Green",
        "Brown",
        "Gold",
        "Beige",
        "Yellow",
        "Orange",
        "Purple",
        "Other"
    )
    
    // Features
    val VEHICLE_FEATURES = listOf(
        "Air Conditioning",
        "Power Steering",
        "Power Windows",
        "ABS",
        "Airbags",
        "Alloy Rims",
        "Sunroof",
        "Leather Seats",
        "Navigation System",
        "Rear Camera",
        "Parking Sensors",
        "Cruise Control",
        "Keyless Entry",
        "Push Start",
        "Climate Control",
        "Heated Seats",
        "DVD Player",
        "USB Port",
        "Bluetooth",
        "Immobilizer"
    )
    
    // Date Formats
    const val DATE_FORMAT_FULL = "dd MMM yyyy, hh:mm a"
    const val DATE_FORMAT_SHORT = "dd MMM yyyy"
    const val DATE_FORMAT_TIME = "hh:mm a"
    
    // Currency
    const val CURRENCY_SYMBOL = "PKR"
    const val CURRENCY_LOCALE = "en-PK"
    
    // Notification Channels
    const val NOTIFICATION_CHANNEL_ID = "apnivehicle_channel"
    const val NOTIFICATION_CHANNEL_NAME = "ApniVehicle Notifications"
    
    // Shared Preferences Keys
    const val PREF_THEME = "theme_preference"
    const val PREF_LANGUAGE = "language_preference"
    const val PREF_NOTIFICATIONS = "notifications_enabled"
    
    // Languages
    const val LANG_ENGLISH = "en"
    const val LANG_URDU = "ur"
    
    // Request Codes
    const val REQUEST_IMAGE_PICK = 1001
    const val REQUEST_CAMERA = 1002
    const val REQUEST_PERMISSIONS = 1003
    
    // Intents
    const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
    const val EXTRA_USER_ID = "extra_user_id"
    
    // Error Messages
    const val ERROR_NETWORK = "Network error. Please check your connection."
    const val ERROR_GENERIC = "Something went wrong. Please try again."
    const val ERROR_NO_DATA = "No data available."
    
    // Success Messages
    const val SUCCESS_VEHICLE_ADDED = "Vehicle added successfully!"
    const val SUCCESS_VEHICLE_UPDATED = "Vehicle updated successfully!"
    const val SUCCESS_VEHICLE_DELETED = "Vehicle deleted successfully!"
    const val SUCCESS_LOGIN = "Welcome back!"
    const val SUCCESS_SIGNUP = "Account created successfully!"
    const val SUCCESS_LOGOUT = "Logged out successfully!"
}
