package com.example.apnivehicle.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.apnivehicle.R
import com.example.apnivehicle.adapters.ImagePagerAdapter
import com.example.apnivehicle.databinding.ActivityDetailBinding
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.AnalyticsManager
import com.example.apnivehicle.utils.NotificationHelper
import com.google.android.material.tabs.TabLayoutMediator
import java.text.NumberFormat
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
        private val priceFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PK"))
    }

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = ActivityDetailBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val vehicleId = intent.getStringExtra(EXTRA_VEHICLE_ID)
            if (vehicleId.isNullOrEmpty()) {
                Toast.makeText(this, "Invalid vehicle", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            
            val vehicle = VehicleRepository.getVehicleById(vehicleId)
            if (vehicle == null) {
                Toast.makeText(this, "Vehicle not found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            // Increment view count and track analytics
            try {
                VehicleRepository.incrementViewCount(vehicle.id)
                AnalyticsManager.trackVehicleView(this, vehicle.id)
            } catch (e: Exception) {
                android.util.Log.e("DetailActivity", "Error incrementing view count", e)
            }

            setupVehicleDetails(vehicle)
            setupButtons(vehicle)
        } catch (e: Exception) {
            android.util.Log.e("DetailActivity", "Fatal error in onCreate", e)
            Toast.makeText(this, "Error loading vehicle details: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupVehicleDetails(vehicle: com.example.apnivehicle.models.Vehicle) {
        try {
            binding.apply {
                // Setup toolbar
                try {
                    setSupportActionBar(toolbar)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.title = vehicle.title
                } catch (e: Exception) {
                    android.util.Log.e("DetailActivity", "Error setting up toolbar", e)
                }
                
                // Load image(s)
                try {
                    // Check if vehicle has multiple images
                    if (vehicle.imageList.isNotEmpty()) {
                        // Show ViewPager2 for multiple images
                        imageViewPager.visibility = View.VISIBLE
                        imageVehicle.visibility = View.GONE
                        
                        val adapter = ImagePagerAdapter(vehicle.imageList)
                        imageViewPager.adapter = adapter
                        
                        // Show indicator if more than 1 image
                        if (vehicle.imageList.size > 1) {
                            imageIndicator.visibility = View.VISIBLE
                            TabLayoutMediator(imageIndicator, imageViewPager) { _, _ -> }.attach()
                        } else {
                            imageIndicator.visibility = View.GONE
                        }
                    } else {
                        // Show single image
                        imageViewPager.visibility = View.GONE
                        imageVehicle.visibility = View.VISIBLE
                        imageIndicator.visibility = View.GONE
                        
                        val uri = vehicle.imageUri
                        if (!uri.isNullOrEmpty()) {
                            try {
                                imageVehicle.setImageURI(uri.toUri())
                            } catch (_: Exception) {
                                if (vehicle.image != 0) {
                                    imageVehicle.setImageResource(vehicle.image)
                                } else {
                                    imageVehicle.setImageResource(R.drawable.ic_car_rental)
                                }
                            }
                        } else if (vehicle.image != 0) {
                            imageVehicle.setImageResource(vehicle.image)
                        } else {
                            imageVehicle.setImageResource(R.drawable.ic_car_rental)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DetailActivity", "Error loading image", e)
                    imageViewPager.visibility = View.GONE
                    imageVehicle.visibility = View.VISIBLE
                    imageIndicator.visibility = View.GONE
                    imageVehicle.setImageResource(R.drawable.ic_car_rental)
                }

                // Basic info
                try {
                    textTitle.text = vehicle.title
                    textPrice.text = priceFormatter.format(vehicle.price)
                    textDescription.text = vehicle.description
                    textCity.text = vehicle.city
                } catch (e: Exception) {
                    android.util.Log.e("DetailActivity", "Error setting basic info", e)
                }
                
                // Format date
                try {
                    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    textDate.text = dateFormat.format(java.util.Date(vehicle.createdAt))
                } catch (e: Exception) {
                    android.util.Log.e("DetailActivity", "Error formatting date", e)
                    textDate.text = "N/A"
                }
                
                // Quick info grid
                try {
                    quickYear.text = vehicle.year.toString()
                    quickFuel.text = vehicle.fuelType
                    quickTrans.text = vehicle.transmission
                } catch (e: Exception) {
                    android.util.Log.e("DetailActivity", "Error setting quick info", e)
                }
                
                // Specifications
                try {
                    detailEngine.text = "Engine: ${vehicle.fuelType}"
                    detailColor.text = "Color: ${vehicle.color.ifEmpty { "N/A" }}"
                    detailAssembly.text = "Condition: ${vehicle.condition}"
                    detailMileage.text = "KM Driven: ${formatNumber(vehicle.mileage)} km"
                } catch (e: Exception) {
                    android.util.Log.e("DetailActivity", "Error setting specifications", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DetailActivity", "Fatal error in setupVehicleDetails", e)
            Toast.makeText(this, "Error displaying vehicle details", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun formatNumber(number: Int): String {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(number)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupButtons(vehicle: com.example.apnivehicle.models.Vehicle) {
        binding.apply {
            btnChat.setOnClickListener {
                try {
                    val phoneNumber = getSellerPhoneNumber(vehicle)
                    
                    if (!phoneNumber.isNullOrBlank()) {
                        // Track contact click
                        AnalyticsManager.trackContactClick(this@DetailActivity, vehicle.id)
                        
                        // Open SMS app with seller's phone number
                        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("sms:$phoneNumber")
                            putExtra("sms_body", "Hi, I'm interested in your ${vehicle.title}")
                        }
                        
                        // Check if there's an app that can handle this intent
                        if (smsIntent.resolveActivity(packageManager) != null) {
                            startActivity(smsIntent)
                        } else {
                            Toast.makeText(
                                this@DetailActivity,
                                "No messaging app found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@DetailActivity,
                            "Seller's phone number not available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DetailActivity", "Error opening SMS", e)
                    Toast.makeText(
                        this@DetailActivity,
                        "Unable to open messaging app",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            btnCall.setOnClickListener {
                try {
                    val phoneNumber = getSellerPhoneNumber(vehicle)
                    
                    if (!phoneNumber.isNullOrBlank()) {
                        // Track contact click
                        AnalyticsManager.trackContactClick(this@DetailActivity, vehicle.id)
                        
                        // Open phone dialer with seller's phone number
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phoneNumber")
                        }
                        
                        // Check if there's an app that can handle this intent
                        if (dialIntent.resolveActivity(packageManager) != null) {
                            startActivity(dialIntent)
                        } else {
                            Toast.makeText(
                                this@DetailActivity,
                                "No phone app found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@DetailActivity,
                            "Seller's phone number not available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DetailActivity", "Error opening dialer", e)
                    Toast.makeText(
                        this@DetailActivity,
                        "Unable to open phone dialer",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getSellerPhoneNumber(vehicle: com.example.apnivehicle.models.Vehicle): String? {
        // First try vehicle's seller phone
        if (!vehicle.sellerPhone.isNullOrBlank() && vehicle.sellerPhone.isNotEmpty()) {
            return vehicle.sellerPhone
        }
        
        // Fallback to seller's phone from AuthRepository
        if (vehicle.sellerId.isNotEmpty()) {
            val seller = AuthRepository.getUserById(vehicle.sellerId)
            if (!seller?.phoneNumber.isNullOrBlank() && seller?.phoneNumber?.isNotEmpty() == true) {
                return seller.phoneNumber
            }
        }
        
        // Return null if no phone number found
        return null
    }
}
