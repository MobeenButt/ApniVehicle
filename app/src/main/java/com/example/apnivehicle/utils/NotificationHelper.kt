package com.example.apnivehicle.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.apnivehicle.R

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "vehicle_events"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vehicle Events",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showVehicleAdded(title: String) {
        val msg = "$title has been posted successfully."
        showNotification(101, "Vehicle Added", msg)
        AppNotificationManager.addNotification(context, "Vehicle Added", msg)
    }

    fun showVehicleDeleted(title: String) {
        val msg = "$title has been removed from listings."
        showNotification(102, "Vehicle Deleted", msg)
        AppNotificationManager.addNotification(context, "Vehicle Deleted", msg)
    }

    fun showFavoriteAdded(title: String) {
        val msg = "$title saved to your favorites."
        showNotification(103, "Favorite Added", msg)
        AppNotificationManager.addNotification(context, "Favorite Added", msg)
    }

    fun showBatteryLowNotification(batteryLevel: Int) {
        val msg = "Battery level is at $batteryLevel%. Consider charging your device."
        showNotification(104, "Battery Low", msg)
        AppNotificationManager.addNotification(context, "Battery Low", msg)
    }

    fun showSystemNotification(title: String, message: String, notificationId: Int = 105) {
        showNotification(notificationId, title, message)
        AppNotificationManager.addNotification(context, title, message)
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(notificationId: Int, title: String, body: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}

