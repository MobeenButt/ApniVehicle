package com.example.apnivehicle

import android.app.Application
import android.util.Log
import com.example.apnivehicle.services.ApniVehicleFcmService
import com.example.apnivehicle.utils.NetworkMonitor
import com.example.apnivehicle.utils.FileManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class ApniVehicleApp : Application() {

    companion object {
        private const val TAG = "ApniVehicleApp"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)

            // Enable Firestore offline persistence
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings

            Log.d(TAG, "Firebase initialized with offline persistence")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed", e)
        }

        // Create FCM notification channels
        try {
            ApniVehicleFcmService.createNotificationChannels(this)
        } catch (e: Exception) {
            Log.e(TAG, "FCM channel creation failed", e)
        }

        // Start network monitoring
        try {
            NetworkMonitor.init(this)
        } catch (e: Exception) {
            Log.e(TAG, "NetworkMonitor init failed", e)
        }

        // Ensure FileManager has application context early to avoid uninitialized access
        try {
            FileManager.init(this)
        } catch (e: Exception) {
            Log.e(TAG, "FileManager init failed", e)
        }
    }
}
