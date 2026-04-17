package com.example.apnivehicle.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ActivityDetailBinding
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.NotificationHelper
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

        // Increment view count
        VehicleRepository.incrementViewCount(vehicle.id)

        setupVehicleDetails(vehicle)
        setupButtons(vehicle)
    }

    private fun setupVehicleDetails(vehicle: com.example.apnivehicle.models.Vehicle) {
        binding.apply {
            val uri = vehicle.imageUri // Avoid smart cast issue with mutable property
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

            textTitle.text = vehicle.title
            textPrice.text = priceFormatter.format(vehicle.price)
            textDescription.text = vehicle.description
            textCity.text = getString(R.string.location_format, vehicle.city)
            quickYear.text = vehicle.year.toString()
        }
    }

    private fun setupButtons(vehicle: com.example.apnivehicle.models.Vehicle) {
        binding.apply {
            btnChat.setOnClickListener {
                try {
                    val phoneNumber = getSellerPhoneNumber(vehicle)
                    
                    if (!phoneNumber.isNullOrBlank()) {
                        // Open SMS app with seller's phone number
                        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("sms:$phoneNumber")
                            putExtra("sms_body", "Hi, I'm interested in your ${vehicle.title}")
                        }
                        startActivity(smsIntent)
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
                        // Open phone dialer with seller's phone number
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phoneNumber")
                        }
                        startActivity(dialIntent)
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
