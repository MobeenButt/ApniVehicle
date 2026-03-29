package com.example.apnivehicle.activities

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ActivityDetailBinding
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
        val vehicle = vehicleId?.let { VehicleRepository.getVehicleById(it) } ?: run {
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
                Toast.makeText(
                    this@DetailActivity,
                    getString(R.string.contacting_seller_message, vehicle.title),
                    Toast.LENGTH_SHORT
                ).show()
            }

            btnCall.setOnClickListener {
                Toast.makeText(
                    this@DetailActivity,
                    getString(R.string.calling_seller_message, vehicle.title),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
