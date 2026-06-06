package com.example.apnivehicle.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages in-app notification count AND persisted notification message list.
 */
object AppNotificationManager {

    private const val PREFS_NAME = "notification_prefs"
    private const val KEY_NOTIFICATION_COUNT = "notification_count"
    private const val KEY_NOTIFICATION_LIST = "notification_list"
    private const val MAX_NOTIFICATIONS = 50

    private var listeners = mutableListOf<NotificationCountListener>()
    private val gson = Gson()

    interface NotificationCountListener {
        fun onNotificationCountChanged(count: Int)
    }

    data class NotificationItem(
        val id: Long = System.currentTimeMillis(),
        val title: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun addListener(listener: NotificationCountListener) {
        if (!listeners.contains(listener)) listeners.add(listener)
    }

    fun removeListener(listener: NotificationCountListener) {
        listeners.remove(listener)
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Count ────────────────────────────────────────────────────────────────

    fun getNotificationCount(context: Context): Int =
        getPrefs(context).getInt(KEY_NOTIFICATION_COUNT, 0)

    fun clearNotificationCount(context: Context) {
        getPrefs(context).edit().putInt(KEY_NOTIFICATION_COUNT, 0).apply()
        notifyListeners(0)
    }

    fun decrementNotificationCount(context: Context) {
        val current = getNotificationCount(context)
        if (current > 0) {
            val newCount = current - 1
            getPrefs(context).edit().putInt(KEY_NOTIFICATION_COUNT, newCount).apply()
            notifyListeners(newCount)
        }
    }

    // ── Message List ─────────────────────────────────────────────────────────

    fun addNotification(context: Context, title: String, message: String) {
        val list = getNotifications(context).toMutableList()
        list.add(0, NotificationItem(title = title, message = message))
        // Cap the list
        val trimmed = if (list.size > MAX_NOTIFICATIONS) list.take(MAX_NOTIFICATIONS) else list
        saveNotifications(context, trimmed)

        // Increment badge count
        val newCount = getNotificationCount(context) + 1
        getPrefs(context).edit().putInt(KEY_NOTIFICATION_COUNT, newCount).apply()
        notifyListeners(newCount)
    }

    fun getNotifications(context: Context): List<NotificationItem> {
        val json = getPrefs(context).getString(KEY_NOTIFICATION_LIST, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<NotificationItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun removeNotification(context: Context, id: Long) {
        val list = getNotifications(context).toMutableList()
        val removed = list.removeAll { it.id == id }
        if (removed) {
            saveNotifications(context, list)
            decrementNotificationCount(context)
        }
    }

    fun clearAllNotifications(context: Context) {
        saveNotifications(context, emptyList())
        getPrefs(context).edit().putInt(KEY_NOTIFICATION_COUNT, 0).apply()
        notifyListeners(0)
    }

    private fun saveNotifications(context: Context, list: List<NotificationItem>) {
        getPrefs(context).edit().putString(KEY_NOTIFICATION_LIST, gson.toJson(list)).apply()
    }

    // ── Legacy helpers kept for backwards compat ──────────────────────────────

    fun incrementNotificationCount(context: Context) {
        val newCount = getNotificationCount(context) + 1
        getPrefs(context).edit().putInt(KEY_NOTIFICATION_COUNT, newCount).apply()
        notifyListeners(newCount)
    }

    private fun notifyListeners(count: Int) {
        listeners.forEach { it.onNotificationCountChanged(count) }
    }
}
