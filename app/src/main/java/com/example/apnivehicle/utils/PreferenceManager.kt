package com.example.apnivehicle.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferenceManager(context: Context) {

    companion object {
        private const val TAG = "PreferenceManager"
        private const val ENCRYPTED_PREFS_FILE = "secure_prefs"
        private const val REGULAR_PREFS_FILE = "app_prefs"

        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_SAVED_EMAIL = "saved_email"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }

    private val appContext = context.applicationContext

    /**
     * EncryptedSharedPreferences crashes on some devices / after reinstall when the
     * Android Keystore entry is missing or corrupt. We try to create it; on any failure
     * we delete the stale file and fall back to plain SharedPreferences so the app
     * doesn't crash on every launch.
     *
     * Security note: the fallback prefs are unencrypted. For a production app you would
     * want to re-prompt the user, but for a study project "works reliably" beats "crashes".
     */
    private val encryptedPrefs: SharedPreferences = createEncryptedPrefs(appContext)

    private fun createEncryptedPrefs(ctx: Context): SharedPreferences {
        // First attempt — normal path
        try {
            val masterKey = MasterKey.Builder(ctx)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                ctx,
                ENCRYPTED_PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "EncryptedSharedPreferences failed (first attempt), deleting and retrying", e)
        }

        // Second attempt — delete the stale/corrupt file and try once more
        try {
            ctx.deleteSharedPreferences(ENCRYPTED_PREFS_FILE)
            val masterKey = MasterKey.Builder(ctx)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                ctx,
                ENCRYPTED_PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "EncryptedSharedPreferences failed (second attempt), falling back to plain prefs", e)
        }

        // Final fallback — unencrypted prefs so the app at least runs
        Log.w(TAG, "Using unencrypted fallback SharedPreferences")
        return ctx.getSharedPreferences("${ENCRYPTED_PREFS_FILE}_fallback", Context.MODE_PRIVATE)
    }

    private val regularPrefs: SharedPreferences =
        appContext.getSharedPreferences(REGULAR_PREFS_FILE, Context.MODE_PRIVATE)

    // ===== Preferences =====

    var isDarkTheme: Boolean
        get() = regularPrefs.getBoolean(KEY_DARK_THEME, false)
        set(value) { regularPrefs.edit().putBoolean(KEY_DARK_THEME, value).apply() }

    var language: String
        get() = regularPrefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) { regularPrefs.edit().putString(KEY_LANGUAGE, value).apply() }

    var notificationsEnabled: Boolean
        get() = regularPrefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) { regularPrefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply() }

    var rememberMe: Boolean
        get() = try { encryptedPrefs.getBoolean(KEY_REMEMBER_ME, false) } catch (e: Exception) { false }
        set(value) { try { encryptedPrefs.edit().putBoolean(KEY_REMEMBER_ME, value).apply() } catch (e: Exception) { Log.e(TAG, "rememberMe set failed", e) } }

    var savedEmail: String?
        get() = try { encryptedPrefs.getString(KEY_SAVED_EMAIL, null) } catch (e: Exception) { null }
        set(value) { try { encryptedPrefs.edit().putString(KEY_SAVED_EMAIL, value).apply() } catch (e: Exception) { Log.e(TAG, "savedEmail set failed", e) } }

    var currentUserId: String?
        get() = try { encryptedPrefs.getString(KEY_CURRENT_USER_ID, null) } catch (e: Exception) { null }
        set(value) { try { encryptedPrefs.edit().putString(KEY_CURRENT_USER_ID, value).apply() } catch (e: Exception) { Log.e(TAG, "currentUserId set failed", e) } }

    var isFirstLaunch: Boolean
        get() = regularPrefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) { regularPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply() }

    fun clearAll() {
        regularPrefs.edit().clear().apply()
        try { encryptedPrefs.edit().clear().apply() } catch (e: Exception) { Log.e(TAG, "clearAll encrypted failed", e) }
    }
}
