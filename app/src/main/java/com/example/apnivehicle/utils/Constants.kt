package com.example.apnivehicle.utils

/**
 * Application-wide constants — vehicle data is Pakistan-market focused.
 * CarQuery API is tried first; this data is the reliable offline fallback.
 */
object Constants {

    // App Info
    const val APP_VERSION = "2.0"
    const val APP_NAME = "ApniVehicle"

    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PASSWORD_LENGTH = 50
    const val MIN_NAME_LENGTH = 3
    const val MAX_NAME_LENGTH = 50
    const val MIN_DESCRIPTION_LENGTH = 20
    const val MAX_DESCRIPTION_LENGTH = 1000

    // Vehicle limits
    const val MIN_PRICE = 1L
    const val MAX_PRICE = 999_999_999L
    const val MIN_YEAR = 1980
    const val MIN_MILEAGE = 0
    const val MAX_MILEAGE = 999_999
    const val MAX_IMAGES = 8
    const val MIN_IMAGES = 1

    // Image settings
    const val IMAGE_QUALITY = 80
    const val MAX_IMAGE_WIDTH = 1920
    const val MAX_IMAGE_HEIGHT = 1920

    // ── Pakistani Cities ───────────────────────────────────────────────────────
    val PAKISTANI_CITIES = listOf(
        "Karachi", "Lahore", "Islamabad", "Rawalpindi", "Faisalabad",
        "Multan", "Peshawar", "Quetta", "Sialkot", "Gujranwala",
        "Hyderabad", "Bahawalpur", "Sargodha", "Sukkur", "Larkana",
        "Sheikhupura", "Jhang", "Rahim Yar Khan", "Gujrat", "Kasur",
        "Mardan", "Mingora", "Sahiwal", "Nawabshah", "Okara",
        "Abbottabad", "Mirpur Khas", "Chiniot", "Sadiqabad", "Burewala",
        "Muzaffarabad", "Gilgit", "Skardu", "Turbat", "Khuzdar",
        "Jacobabad", "Shikarpur", "Khairpur", "Dadu", "Mirpur (AJK)",
        "Hafizabad", "Mandi Bahauddin", "Narowal", "Pakpattan", "Vehari",
        "Lodhran", "Khanewal", "Muzaffargarh", "Layyah", "Bhakkar",
        "Chakwal", "Jhelum", "Attock", "Mianwali", "Toba Tek Singh",
        "Wah Cantonment", "Taxila", "Nowshera", "Charsadda", "Swabi",
        "Mansehra", "Haripur", "Swat", "Dir", "Bannu", "Kohat", "Hangu",
        "Dera Ismail Khan", "Tank", "Zhob", "Loralai", "Hub", "Gwadar",
        "Other"
    ).sorted()

    // ── Vehicle Makes (Pakistan market) ───────────────────────────────────────
    val VEHICLE_MAKES = listOf(
        "Toyota", "Honda", "Suzuki", "Hyundai", "KIA",
        "Nissan", "Daihatsu", "Mitsubishi", "Mercedes-Benz", "BMW",
        "Audi", "Changan", "MG", "Proton", "FAW",
        "Prince", "United", "Mazda", "Volkswagen", "Chevrolet",
        "Ford", "Land Rover", "Lexus", "Porsche", "Isuzu",
        "Hino", "Fiat", "Renault", "Peugeot", "Volvo",
        "Jeep", "Dodge", "RAM", "Chrysler", "DFSK",
        "Revo", "Chery", "Haval", "Geely", "BYD",
        "Other"
    ).sorted()

    // ── Models per make (Pakistan-specific) ───────────────────────────────────
    val VEHICLE_MODELS: Map<String, List<String>> = mapOf(
        "Toyota" to listOf(
            "Corolla GLi", "Corolla XLi", "Corolla Altis", "Corolla Grande",
            "Corolla 1.8", "Yaris", "Vitz", "Aqua", "Prius", "Prius V",
            "Fortuner", "Fortuner Sigma 4", "Fortuner Legender",
            "Hilux Single Cab", "Hilux Double Cab", "Hilux Revo",
            "Land Cruiser", "Land Cruiser V8", "Land Cruiser 200",
            "Prado", "Prado TX", "Prado TZ",
            "Camry", "Crown", "Alphard", "Hiace", "Coaster",
            "Rush", "Raize", "C-HR", "RAV4"
        ),
        "Honda" to listOf(
            "Civic Oriel", "Civic VTi", "Civic VTi Oriel", "Civic RS",
            "Civic 1.5 Turbo", "Civic 1.8", "Civic Prosmatec",
            "City Aspire", "City 1.3", "City 1.5", "City eHEV",
            "Accord", "BR-V", "HR-V", "Vezel", "WR-V",
            "CR-V", "Pilot", "Fit", "Jazz", "N-Box",
            "Freed", "Stepwgn", "Odyssey", "Stream", "Shuttle"
        ),
        "Suzuki" to listOf(
            "Alto VX", "Alto VXR", "Alto AGS", "Alto 660cc",
            "Cultus VXR", "Cultus VXL", "Cultus AGS",
            "Swift DLX", "Swift GL", "Swift GLX", "Swift Automatic",
            "Wagon R VXR", "Wagon R VXL", "Wagon R AGS",
            "Bolan VX", "Bolan VXR",
            "Ravi", "Khyber", "Mehran VX", "Mehran VXR",
            "Jimny", "Vitara", "S-Presso", "Ertiga",
            "Every", "Carry"
        ),
        "Hyundai" to listOf(
            "Elantra", "Elantra GLS", "Elantra GS",
            "Sonata", "Tucson 2WD", "Tucson AWD", "Tucson Ultimate",
            "Santa Fe", "Grand Starex", "Staria",
            "Ioniq 5", "Ioniq 6", "Creta",
            "i10", "i20", "i30", "Accent", "Verna"
        ),
        "KIA" to listOf(
            "Sportage Alpha", "Sportage FWD", "Sportage AWD",
            "Sportage GT-Line", "Sportage 2023",
            "Picanto", "Picanto Automatic",
            "Stonic", "Sorento", "Carnival",
            "Seltos", "Cerato", "Rio", "Stinger", "EV6"
        ),
        "Nissan" to listOf(
            "Sunny", "Dayz", "Juke", "X-Trail",
            "Patrol", "Navara", "Frontier",
            "March", "Note", "Leaf", "Kicks", "Terra"
        ),
        "Daihatsu" to listOf(
            "Mira", "Mira X", "Mira ES",
            "Move", "Move Custom",
            "Cuore", "Terios", "Hijet",
            "Boon", "Tanto", "Cast"
        ),
        "Mitsubishi" to listOf(
            "Lancer", "Lancer GLX", "Lancer GT",
            "Pajero", "Pajero Sport", "Outlander",
            "Eclipse Cross", "L200", "Galant", "Colt", "Mirage"
        ),
        "Mercedes-Benz" to listOf(
            "C180", "C200", "C220", "C300", "C43 AMG", "C63 AMG",
            "E200", "E220", "E300", "E350", "E400", "E63 AMG",
            "S350", "S400", "S450", "S500", "S560",
            "GLA 200", "GLC 200", "GLC 300", "GLE 400", "GLS 450",
            "A180", "A200", "CLA 200", "CLA 250",
            "G63 AMG", "GTS AMG"
        ),
        "BMW" to listOf(
            "316i", "318i", "320i", "325i", "330i", "340i",
            "520i", "528i", "530i", "540i", "M5",
            "730i", "740i", "750i",
            "X1", "X3", "X4", "X5", "X6", "X7",
            "M3", "M4", "M8", "Z4"
        ),
        "Audi" to listOf(
            "A3", "A4", "A5", "A6", "A7", "A8",
            "Q3", "Q5", "Q7", "Q8",
            "TT", "R8", "e-tron", "S3", "S4", "RS5"
        ),
        "Changan" to listOf(
            "Alsvin", "Alsvin Lumiere", "Alsvin Comfort",
            "Oshan X7", "Oshan X7 Plus",
            "Karvaan", "Karvaan Plus",
            "M9", "CS35 Plus", "CS75 Plus", "Hunter"
        ),
        "MG" to listOf(
            "HS", "HS Exclusive", "HS Trophy",
            "ZS", "ZS EV",
            "RX5", "GT", "5",
            "Gloster", "Extender"
        ),
        "Proton" to listOf(
            "Saga", "Persona", "X50", "X70", "Ertiga"
        ),
        "FAW" to listOf(
            "V2", "V2 Comfort", "V2 Cruise",
            "D60", "Sirius S80",
            "Carrier", "X-PV"
        ),
        "Prince" to listOf(
            "Pearl", "DFSK Glory 580", "DFSK Glory 500"
        ),
        "United" to listOf(
            "Bravo", "Alpha", "Aria 800cc"
        ),
        "Mazda" to listOf(
            "2", "3", "6", "CX-3", "CX-5", "CX-8", "CX-9",
            "MX-5", "BT-50"
        ),
        "Volkswagen" to listOf(
            "Polo", "Golf", "Passat", "Tiguan", "Touareg", "Phaeton"
        ),
        "Land Rover" to listOf(
            "Defender", "Discovery", "Discovery Sport",
            "Range Rover", "Range Rover Sport", "Range Rover Velar",
            "Freelander"
        ),
        "Lexus" to listOf(
            "ES 250", "ES 300h", "IS 300", "GS 350",
            "RX 350", "NX 300", "LX 570", "LX 600",
            "GX 460", "UX 200"
        ),
        "Haval" to listOf(
            "H6", "H6 HEV", "Jolion", "Jolion HEV",
            "H9", "Dargo"
        ),
        "Jeep" to listOf(
            "Wrangler", "Grand Cherokee", "Cherokee",
            "Renegade", "Compass", "Gladiator"
        ),
        "Isuzu" to listOf(
            "D-Max", "D-Max 4x4", "MU-X",
            "Forward", "NPR", "NPS"
        ),
        "Ford" to listOf(
            "Ranger", "Ranger XL", "Ranger XLT",
            "F-150", "Explorer", "Escape", "Edge",
            "EcoSport", "Everest"
        ),
        "Chevrolet" to listOf(
            "Trailblazer", "Captiva", "Cruze",
            "Aveo", "Spark", "Optra", "Lanos"
        ),
        "DFSK" to listOf(
            "Glory 580", "Glory 500", "Glory 560",
            "EC31", "EC35"
        ),
        "BYD" to listOf(
            "Atto 3", "Han", "Tang", "Seal", "Dolphin"
        ),
        "Geely" to listOf(
            "Coolray", "Atlas Pro", "Emgrand",
            "Tugella", "Okavango"
        )
    )

    // ── Fuel Types ────────────────────────────────────────────────────────────
    val FUEL_TYPES = listOf(
        "Petrol", "Diesel", "CNG", "LPG",
        "Hybrid (Petrol+Electric)", "Plug-in Hybrid",
        "Electric", "Petrol+CNG"
    )

    // ── Transmission Types ────────────────────────────────────────────────────
    val TRANSMISSION_TYPES = listOf(
        "Manual", "Automatic", "CVT (Auto)", "Semi-Automatic (AGS)", "DCT"
    )

    // ── Vehicle Conditions ────────────────────────────────────────────────────
    val VEHICLE_CONDITIONS = listOf(
        "Brand New", "Used — Excellent", "Used — Good",
        "Used — Fair", "Certified Pre-Owned", "Accidental / Flood"
    )

    // ── Colors ────────────────────────────────────────────────────────────────
    val VEHICLE_COLORS = listOf(
        "White", "Pearl White", "Black", "Silver", "Grey",
        "Red", "Maroon", "Blue", "Dark Blue", "Sky Blue",
        "Green", "Dark Green", "Brown", "Beige", "Champagne",
        "Gold", "Yellow", "Orange", "Purple", "Other"
    )

    // ── Features ─────────────────────────────────────────────────────────────
    val VEHICLE_FEATURES = listOf(
        "Air Conditioning", "Climate Control", "Power Steering",
        "Power Windows", "Power Mirrors", "ABS Brakes",
        "Airbags (Driver)", "Airbags (Dual)", "Airbags (Full)",
        "Alloy Rims", "Sunroof", "Panoramic Roof",
        "Leather Seats", "Heated Seats", "Ventilated Seats",
        "Rear AC Vents", "Navigation / GPS", "Rear Camera",
        "Front Camera", "360° Camera", "Parking Sensors",
        "Cruise Control", "Adaptive Cruise Control",
        "Keyless Entry", "Push Start", "Start/Stop Button",
        "DVD / Multimedia", "Android Auto / Apple CarPlay",
        "USB Ports", "Bluetooth", "Immobilizer",
        "Lane Assist", "Blind Spot Monitor",
        "Automatic Headlights", "LED Headlights", "Fog Lights",
        "Third Row Seats", "Foldable Rear Seats",
        "Roof Rails", "Tow Bar"
    )

    // ── Date / currency / prefs ───────────────────────────────────────────────
    const val DATE_FORMAT_FULL  = "dd MMM yyyy, hh:mm a"
    const val DATE_FORMAT_SHORT = "dd MMM yyyy"
    const val DATE_FORMAT_TIME  = "hh:mm a"
    const val CURRENCY_SYMBOL   = "PKR"
    const val CURRENCY_LOCALE   = "en-PK"

    // ── Notification channels ─────────────────────────────────────────────────
    const val NOTIFICATION_CHANNEL_ID   = "apnivehicle_channel"
    const val NOTIFICATION_CHANNEL_NAME = "ApniVehicle Notifications"

    // ── SharedPreferences keys ────────────────────────────────────────────────
    const val PREF_THEME         = "theme_preference"
    const val PREF_LANGUAGE      = "language_preference"
    const val PREF_NOTIFICATIONS = "notifications_enabled"

    const val LANG_ENGLISH = "en"
    const val LANG_URDU    = "ur"

    // ── Request codes ─────────────────────────────────────────────────────────
    const val REQUEST_IMAGE_PICK  = 1001
    const val REQUEST_CAMERA      = 1002
    const val REQUEST_PERMISSIONS = 1003

    // ── Extras / Intents ─────────────────────────────────────────────────────
    const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
    const val EXTRA_USER_ID    = "extra_user_id"

    // ── Messages ──────────────────────────────────────────────────────────────
    const val ERROR_NETWORK         = "Network error. Please check your connection."
    const val ERROR_GENERIC         = "Something went wrong. Please try again."
    const val ERROR_NO_DATA         = "No data available."
    const val SUCCESS_VEHICLE_ADDED   = "Your ad is live! 🎉"
    const val SUCCESS_VEHICLE_UPDATED = "Vehicle updated successfully!"
    const val SUCCESS_VEHICLE_DELETED = "Vehicle deleted successfully!"
    const val SUCCESS_LOGIN  = "Welcome back!"
    const val SUCCESS_SIGNUP = "Account created successfully!"
    const val SUCCESS_LOGOUT = "Logged out successfully!"
}
