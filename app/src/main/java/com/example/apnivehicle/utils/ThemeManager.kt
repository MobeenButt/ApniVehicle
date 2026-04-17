package com.example.apnivehicle.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Manages app theme (Light/Dark mode)
 */
object ThemeManager {
    
    private const val TAG = "ThemeManager"
    
    /**
     * Apply theme based on saved preference
     */
    fun applyTheme(context: Context) {
        val preferenceManager = PreferenceManager(context)
        val isDarkTheme = preferenceManager.isDarkTheme
        
        val mode = if (isDarkTheme) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    /**
     * Toggle theme and save preference
     */
    fun toggleTheme(context: Context) {
        val preferenceManager = PreferenceManager(context)
        val newTheme = !preferenceManager.isDarkTheme
        preferenceManager.isDarkTheme = newTheme
        
        val mode = if (newTheme) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    /**
     * Set theme explicitly
     */
    fun setTheme(context: Context, isDark: Boolean) {
        val preferenceManager = PreferenceManager(context)
        preferenceManager.isDarkTheme = isDark
        
        val mode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    /**
     * Check if dark theme is enabled
     */
    fun isDarkTheme(context: Context): Boolean {
        val preferenceManager = PreferenceManager(context)
        return preferenceManager.isDarkTheme
    }
}
