package com.example.apnivehicle.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.apnivehicle.models.Vehicle
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.NotificationHelper

/**
 * Custom BroadcastReceiver for Price Drop Alerts
 * 
 * Features:
 * - Monitors vehicle price changes
 * - Sends notifications when favorite vehicle prices drop
 * - Tracks price history
 * - Provides price drop percentage
 */
class PriceDropBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PriceDropReceiver"
        
        // Custom action for price drop
        const val ACTION_PRICE_DROP = "com.example.apnivehicle.ACTION_PRICE_DROP"
        const val ACTION_CHECK_PRICE_DROPS = "com.example.apnivehicle.ACTION_CHECK_PRICE_DROPS"
        
        // Extras
        const val EXTRA_VEHICLE_ID = "vehicle_id"
        const val EXTRA_OLD_PRICE = "old_price"
        const val EXTRA_NEW_PRICE = "new_price"
        const val EXTRA_DROP_PERCENTAGE = "drop_percentage"
        
        /**
         * Register this receiver dynamically
         */
        fun register(context: Context): PriceDropBroadcastReceiver {
            val receiver = PriceDropBroadcastReceiver()
            val filter = IntentFilter().apply {
                addAction(ACTION_PRICE_DROP)
                addAction(ACTION_CHECK_PRICE_DROPS)
            }
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            Log.d(TAG, "PriceDropBroadcastReceiver registered")
            return receiver
        }
        
        /**
         * Unregister the receiver
         */
        fun unregister(context: Context, receiver: PriceDropBroadcastReceiver?) {
            if (receiver == null) return
            try {
                context.unregisterReceiver(receiver)
                Log.d(TAG, "PriceDropBroadcastReceiver unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
        }
        
        /**
         * Send price drop broadcast
         */
        fun sendPriceDropAlert(
            context: Context,
            vehicleId: String,
            oldPrice: Long,
            newPrice: Long
        ) {
            val dropPercentage = ((oldPrice - newPrice).toFloat() / oldPrice * 100).toInt()
            
            val intent = Intent(ACTION_PRICE_DROP).apply {
                putExtra(EXTRA_VEHICLE_ID, vehicleId)
                putExtra(EXTRA_OLD_PRICE, oldPrice)
                putExtra(EXTRA_NEW_PRICE, newPrice)
                putExtra(EXTRA_DROP_PERCENTAGE, dropPercentage)
            }
            
            context.sendBroadcast(intent)
            Log.d(TAG, "Price drop broadcast sent for vehicle: $vehicleId")
        }
        
        /**
         * Trigger check for all price drops
         */
        fun checkAllPriceDrops(context: Context) {
            val intent = Intent(ACTION_CHECK_PRICE_DROPS)
            context.sendBroadcast(intent)
            Log.d(TAG, "Check price drops broadcast sent")
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        try {
            when (intent.action) {
                ACTION_PRICE_DROP -> handlePriceDrop(context, intent)
                ACTION_CHECK_PRICE_DROPS -> handleCheckPriceDrops(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling broadcast", e)
        }
    }

    private fun handlePriceDrop(context: Context, intent: Intent) {
        val vehicleId = intent.getStringExtra(EXTRA_VEHICLE_ID) ?: return
        val oldPrice = intent.getLongExtra(EXTRA_OLD_PRICE, 0)
        val newPrice = intent.getLongExtra(EXTRA_NEW_PRICE, 0)
        val dropPercentage = intent.getIntExtra(EXTRA_DROP_PERCENTAGE, 0)
        
        Log.d(TAG, "Price drop detected: $vehicleId, $dropPercentage% off")
        
        // Get vehicle details
        val vehicle = VehicleRepository.getVehicleById(vehicleId)
        if (vehicle == null) {
            Log.w(TAG, "Vehicle not found: $vehicleId")
            return
        }
        
        // Check if user has favorited this vehicle
        if (vehicle.isFavorite) {
            // Send notification
            val notificationHelper = NotificationHelper(context)
            val title = "🔥 Price Drop Alert!"
            val message = "${vehicle.title} - ${dropPercentage}% OFF! " +
                    "Now PKR ${formatPrice(newPrice)} (was PKR ${formatPrice(oldPrice)})"
            
            notificationHelper.showSystemNotification(
                title = title,
                message = message,
                notificationId = vehicleId.hashCode()
            )
            
            Log.d(TAG, "Price drop notification sent for: ${vehicle.title}")
        } else {
            Log.d(TAG, "Vehicle not in favorites, skipping notification")
        }
    }

    private fun handleCheckPriceDrops(context: Context) {
        Log.d(TAG, "Checking all vehicles for price drops...")
        
        val favorites = VehicleRepository.getFavorites()
        var dropsFound = 0
        
        favorites.forEach { vehicle ->
            // Check if vehicle has price history
            if (vehicle.priceHistory.size >= 2) {
                val currentPrice = vehicle.price
                val previousPrice = vehicle.priceHistory[vehicle.priceHistory.size - 2].price
                
                // If price dropped by at least 5%
                if (currentPrice < previousPrice) {
                    val dropPercentage = ((previousPrice - currentPrice).toFloat() / previousPrice * 100).toInt()
                    
                    if (dropPercentage >= 5) {
                        sendPriceDropAlert(context, vehicle.id, previousPrice, currentPrice)
                        dropsFound++
                    }
                }
            }
        }
        
        Log.d(TAG, "Price drop check complete. Found $dropsFound drops.")
        
        if (dropsFound > 0) {
            // Show summary notification
            val notificationHelper = NotificationHelper(context)
            notificationHelper.showSystemNotification(
                title = "Price Drops Found!",
                message = "$dropsFound of your favorite vehicles have price drops. Check them out!",
                notificationId = 9999
            )
        }
    }

    private fun formatPrice(price: Long): String {
        return when {
            price >= 10000000 -> String.format("%.1fCr", price / 10000000.0)
            price >= 100000 -> String.format("%.1fL", price / 100000.0)
            else -> String.format("%,d", price)
        }
    }
}
