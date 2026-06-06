package com.example.apnivehicle.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.apnivehicle.R
import com.example.apnivehicle.activities.MainActivity
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.utils.AppNotificationManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ApniVehicleFcmService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "ApniVehicleFcmService"
        const val CHANNEL_CHAT = "channel_chat"
        const val CHANNEL_PRICE_DROP = "channel_price_drop"
        const val CHANNEL_SAVED_SEARCH = "channel_saved_search"
        const val CHANNEL_VERIFICATION = "channel_verification"
        const val CHANNEL_GENERAL = "channel_general"

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channels = listOf(
                    NotificationChannel(CHANNEL_CHAT, "Chat Messages", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "New chat messages from buyers/sellers"
                    },
                    NotificationChannel(CHANNEL_PRICE_DROP, "Price Drops", NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = "Price drops on favorited vehicles"
                    },
                    NotificationChannel(CHANNEL_SAVED_SEARCH, "Saved Search Alerts", NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = "New vehicles matching your saved searches"
                    },
                    NotificationChannel(CHANNEL_VERIFICATION, "Verification Updates", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Seller verification status changes"
                    },
                    NotificationChannel(CHANNEL_GENERAL, "General", NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = "General app notifications"
                    }
                )
                channels.forEach { manager.createNotificationChannel(it) }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed: $token")
        // Save token to user profile
        AuthRepository.saveFcmToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = data["title"] ?: notification?.title ?: "ApniVehicle"
        val body = data["body"] ?: notification?.body ?: ""
        val type = data["type"] ?: "general"
        val targetId = data["targetId"] ?: ""

        val channelId = when (type) {
            "chat" -> CHANNEL_CHAT
            "price_drop" -> CHANNEL_PRICE_DROP
            "saved_search" -> CHANNEL_SAVED_SEARCH
            "verification" -> CHANNEL_VERIFICATION
            else -> CHANNEL_GENERAL
        }

        showNotification(title, body, channelId, targetId, type)
        AppNotificationManager.incrementNotificationCount(applicationContext)
    }

    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        targetId: String,
        type: String
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", type)
            putExtra("target_id", targetId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = System.currentTimeMillis().toInt()

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(this).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission not granted", e)
        }
    }
}
