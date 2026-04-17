package com.example.apnivehicle.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.apnivehicle.utils.NotificationHelper

/**
 * BroadcastReceiver for monitoring system events like battery status and connectivity
 * 
 * Usage:
 * - Monitors battery level and shows notification when low
 * - Monitors network connectivity changes
 * - Can be extended for other system events
 */
class SystemBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SystemBroadcastReceiver"
        private const val LOW_BATTERY_THRESHOLD = 20
        
        /**
         * Register this receiver dynamically in an Activity or Service
         */
        fun register(context: Context): SystemBroadcastReceiver? {
            return try {
                val receiver = SystemBroadcastReceiver()
                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_BATTERY_LOW)
                    addAction(Intent.ACTION_BATTERY_OKAY)
                    addAction(Intent.ACTION_POWER_CONNECTED)
                    addAction(Intent.ACTION_POWER_DISCONNECTED)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        addAction(ConnectivityManager.CONNECTIVITY_ACTION)
                    }
                }
                context.registerReceiver(receiver, filter)
                Log.d(TAG, "SystemBroadcastReceiver registered")
                receiver
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register SystemBroadcastReceiver", e)
                null
            }
        }
        
        /**
         * Unregister the receiver
         */
        fun unregister(context: Context, receiver: SystemBroadcastReceiver?) {
            if (receiver == null) return
            try {
                context.unregisterReceiver(receiver)
                Log.d(TAG, "SystemBroadcastReceiver unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        try {
            when (intent.action) {
                Intent.ACTION_BATTERY_LOW -> {
                    handleBatteryLow(context)
                }
                
                Intent.ACTION_BATTERY_OKAY -> {
                    handleBatteryOkay(context)
                }
                
                Intent.ACTION_POWER_CONNECTED -> {
                    handlePowerConnected(context)
                }
                
                Intent.ACTION_POWER_DISCONNECTED -> {
                    handlePowerDisconnected(context)
                }
                
                ConnectivityManager.CONNECTIVITY_ACTION -> {
                    handleConnectivityChange(context)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling broadcast", e)
        }
    }

    private fun handleBatteryLow(context: Context) {
        Log.d(TAG, "Battery low detected")
        
        val batteryLevel = getBatteryLevel(context)
        
        // Create NotificationHelper instance and show notification
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showBatteryLowNotification(batteryLevel)
        
        Toast.makeText(context, "Battery Low: $batteryLevel%", Toast.LENGTH_SHORT).show()
    }

    private fun handleBatteryOkay(context: Context) {
        Log.d(TAG, "Battery okay")
        Toast.makeText(context, "Battery level is okay", Toast.LENGTH_SHORT).show()
    }

    private fun handlePowerConnected(context: Context) {
        Log.d(TAG, "Power connected")
        Toast.makeText(context, "Charging started", Toast.LENGTH_SHORT).show()
    }

    private fun handlePowerDisconnected(context: Context) {
        Log.d(TAG, "Power disconnected")
        Toast.makeText(context, "Charging stopped", Toast.LENGTH_SHORT).show()
    }

    private fun handleConnectivityChange(context: Context) {
        val isConnected = isNetworkAvailable(context)
        Log.d(TAG, "Network connectivity changed: $isConnected")
        
        val message = if (isConnected) {
            "Internet connection restored"
        } else {
            "No internet connection"
        }
        
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun getBatteryLevel(context: Context): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo?.isConnected == true
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for network state access", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network availability", e)
            false
        }
    }
}
