package com.example.apnivehicle.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages notification count and badge display
 */
object AppNotificationManager {
    
    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_NOTIFICATION_COUNT = "notification_count"
    
    private var listeners = mutableListOf<NotificationCountListener>()
    
    interface NotificationCountListener {
        fun onNotificationCountChanged(count: Int)
    }
    
    fun addListener(listener: NotificationCountListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }
    
    fun removeListener(listener: NotificationCountListener) {
        listeners.remove(listener)
    }
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun getNotificationCount(context: Context): Int {
        return getPrefs(context).getInt(KEY_NOTIFICATION_COUNT, 0)
    }
    
    fun incrementNotificationCount(context: Context) {
        val currentCount = getNotificationCount(context)
        val newCount = currentCount + 1
        getPrefs(context).edit().putInt(KEY_NOTIFICATION_COUNT, newCount).apply()
        notifyListeners(newCount)
    }
    
    fun clearNotificationCount(context: Context) {
        getPrefs(context).edit().putInt(KEY_NOTIFICATION_COUNT, 0).apply()
        notifyListeners(0)
    }
    
    fun decrementNotificationCount(context: Context) {
        val currentCount = getNotificationCount(context)
        if (currentCount > 0) {
            val newCount = currentCount - 1
            getPrefs(context).edit().putInt(KEY_NOTIFICATION_COUNT, newCount).apply()
            notifyListeners(newCount)
        }
    }
    
    private fun notifyListeners(count: Int) {
        listeners.forEach { it.onNotificationCountChanged(count) }
    }
}
