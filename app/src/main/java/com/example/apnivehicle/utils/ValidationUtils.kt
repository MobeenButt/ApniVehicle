package com.example.apnivehicle.utils

import android.util.Patterns
import java.util.Calendar
import java.util.regex.Pattern

object ValidationUtils {
    
    // ===== Email Validation =====
    
    /**
     * Validate email format using Android Patterns
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            !isValidEmail(email) -> ValidationResult(false, "Invalid email format")
            else -> ValidationResult(true)
        }
    }
    
    // ===== Phone Validation =====
    
    /**
     * Pakistan phone number validation: 03XX-XXXXXXX or 03XXXXXXXXX
     */
    fun isValidPakistanPhone(phone: String): Boolean {
        val phonePattern = Pattern.compile("^03\\d{2}-?\\d{7}$")
        return phonePattern.matcher(phone).matches()
    }
    
    /**
     * Format phone number to 03XX-XXXXXXX
     */
    fun formatPhoneNumber(phone: String): String {
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")
        return if (digitsOnly.length == 11 && digitsOnly.startsWith("03")) {
            "${digitsOnly.substring(0, 4)}-${digitsOnly.substring(4)}"
        } else {
            phone
        }
    }
    
    fun validatePhone(phone: String): ValidationResult {
        return when {
            phone.isBlank() -> ValidationResult(false, "Phone number is required")
            !isValidPakistanPhone(phone) -> ValidationResult(false, "Format: 03XX-XXXXXXX")
            else -> ValidationResult(true)
        }
    }
    
    // ===== Password Validation =====
    
    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
    
    /**
     * Validate password with strict requirements:
     * - Min 8 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter
     * - At least 1 number
     * - At least 1 special character
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Password is required")
            password.length < Constants.MIN_PASSWORD_LENGTH -> 
                ValidationResult(false, "Password must be at least ${Constants.MIN_PASSWORD_LENGTH} characters")
            password.length > Constants.MAX_PASSWORD_LENGTH -> 
                ValidationResult(false, "Password must be less than ${Constants.MAX_PASSWORD_LENGTH} characters")
            !password.any { it.isUpperCase() } -> 
                ValidationResult(false, "Password must contain at least 1 uppercase letter")
            !password.any { it.isLowerCase() } -> 
                ValidationResult(false, "Password must contain at least 1 lowercase letter")
            !password.any { it.isDigit() } -> 
                ValidationResult(false, "Password must contain at least 1 number")
            !password.any { !it.isLetterOrDigit() } -> 
                ValidationResult(false, "Password must contain at least 1 special character")
            else -> ValidationResult(true)
        }
    }
    
    fun getPasswordStrength(password: String): PasswordStrength {
        if (password.length < Constants.MIN_PASSWORD_LENGTH) return PasswordStrength.WEAK
        
        var score = 0
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        
        return when {
            score >= 5 -> PasswordStrength.STRONG
            score >= 3 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
    
    fun getPasswordStrengthText(strength: PasswordStrength): String {
        return when (strength) {
            PasswordStrength.WEAK -> "Weak"
            PasswordStrength.MEDIUM -> "Medium"
            PasswordStrength.STRONG -> "Strong"
        }
    }
    
    fun getPasswordStrengthColor(strength: PasswordStrength): Int {
        return when (strength) {
            PasswordStrength.WEAK -> android.R.color.holo_red_dark
            PasswordStrength.MEDIUM -> android.R.color.holo_orange_dark
            PasswordStrength.STRONG -> android.R.color.holo_green_dark
        }
    }
    
    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword && password.isNotBlank()
    }
    
    fun validatePasswordMatch(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isBlank() -> ValidationResult(false, "Please confirm your password")
            password != confirmPassword -> ValidationResult(false, "Passwords do not match")
            else -> ValidationResult(true)
        }
    }
    
    // ===== Name Validation =====
    
    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Name is required")
            name.length < Constants.MIN_NAME_LENGTH -> 
                ValidationResult(false, "Name must be at least ${Constants.MIN_NAME_LENGTH} characters")
            name.length > Constants.MAX_NAME_LENGTH -> 
                ValidationResult(false, "Name must be less than ${Constants.MAX_NAME_LENGTH} characters")
            !name.matches(Regex("^[a-zA-Z\\s]+$")) -> 
                ValidationResult(false, "Name can only contain letters and spaces")
            else -> ValidationResult(true)
        }
    }
    
    // ===== Vehicle Validation =====
    
    fun validateVehicleTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult(false, "Title is required")
            title.length < 5 -> ValidationResult(false, "Title must be at least 5 characters")
            title.length > 100 -> ValidationResult(false, "Title must be less than 100 characters")
            else -> ValidationResult(true)
        }
    }
    
    fun validatePrice(price: Long?): ValidationResult {
        return when {
            price == null -> ValidationResult(false, "Price is required")
            price < Constants.MIN_PRICE -> 
                ValidationResult(false, "Price must be at least ${Constants.CURRENCY_SYMBOL} ${Constants.MIN_PRICE}")
            price > Constants.MAX_PRICE -> 
                ValidationResult(false, "Price cannot exceed ${Constants.CURRENCY_SYMBOL} ${Constants.MAX_PRICE}")
            else -> ValidationResult(true)
        }
    }
    
    fun validateYear(year: Int?): ValidationResult {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return when {
            year == null -> ValidationResult(false, "Year is required")
            year < Constants.MIN_YEAR -> 
                ValidationResult(false, "Year must be ${Constants.MIN_YEAR} or later")
            year > currentYear -> 
                ValidationResult(false, "Year cannot be in the future")
            else -> ValidationResult(true)
        }
    }
    
    fun validateMileage(mileage: Int?): ValidationResult {
        return when {
            mileage == null -> ValidationResult(false, "Mileage is required")
            mileage < Constants.MIN_MILEAGE -> 
                ValidationResult(false, "Mileage cannot be negative")
            mileage > Constants.MAX_MILEAGE -> 
                ValidationResult(false, "Mileage seems too high")
            else -> ValidationResult(true)
        }
    }
    
    fun validateDescription(description: String): ValidationResult {
        return when {
            description.isBlank() -> ValidationResult(false, "Description is required")
            description.length < Constants.MIN_DESCRIPTION_LENGTH -> 
                ValidationResult(false, "Description must be at least ${Constants.MIN_DESCRIPTION_LENGTH} characters")
            description.length > Constants.MAX_DESCRIPTION_LENGTH -> 
                ValidationResult(false, "Description must be less than ${Constants.MAX_DESCRIPTION_LENGTH} characters")
            else -> ValidationResult(true)
        }
    }
    
    fun validateCity(city: String): ValidationResult {
        return when {
            city.isBlank() -> ValidationResult(false, "City is required")
            !Constants.PAKISTANI_CITIES.contains(city) -> 
                ValidationResult(false, "Please select a valid city")
            else -> ValidationResult(true)
        }
    }
    
    fun validateImages(imageCount: Int): ValidationResult {
        return when {
            imageCount < Constants.MIN_IMAGES -> 
                ValidationResult(false, "At least ${Constants.MIN_IMAGES} image is required")
            imageCount > Constants.MAX_IMAGES -> 
                ValidationResult(false, "Maximum ${Constants.MAX_IMAGES} images allowed")
            else -> ValidationResult(true)
        }
    }
    
    // ===== Validation Result Data Class =====
    
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
}

