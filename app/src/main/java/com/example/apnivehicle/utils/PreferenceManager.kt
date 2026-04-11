package com.example.apnivehicle.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferenceManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val regularPrefs: SharedPreferences = 
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    // Theme preference
    var isDarkTheme: Boolean
        get() = regularPrefs.getBoolean(KEY_DARK_THEME, false)
        set(value) = regularPrefs.edit().putBoolean(KEY_DARK_THEME, value).apply()
    
    // Language preference
    var language: String
        get() = regularPrefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = regularPrefs.edit().putString(KEY_LANGUAGE, value).apply()
    
    // Notifications enabled
    var notificationsEnabled: Boolean
        get() = regularPrefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = regularPrefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()
    
    // Remember me
    var rememberMe: Boolean
        get() = encryptedPrefs.getBoolean(KEY_REMEMBER_ME, false)
        set(value) = encryptedPrefs.edit().putBoolean(KEY_REMEMBER_ME, value).apply()
    
    // Saved email (encrypted)
    var savedEmail: String?
        get() = encryptedPrefs.getString(KEY_SAVED_EMAIL, null)
        set(value) = encryptedPrefs.edit().putString(KEY_SAVED_EMAIL, value).apply()
    
    // Current user ID
    var currentUserId: String?
        get() = encryptedPrefs.getString(KEY_CURRENT_USER_ID, null)
        set(value) = encryptedPrefs.edit().putString(KEY_CURRENT_USER_ID, value).apply()
    
    // First launch
    var isFirstLaunch: Boolean
        get() = regularPrefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = regularPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
    
    fun clearAll() {
        regularPrefs.edit().clear().apply()
        encryptedPrefs.edit().clear().apply()
    }
    
    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_SAVED_EMAIL = "saved_email"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }
}
