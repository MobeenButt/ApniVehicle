package com.example.apnivehicle.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            if (!vehicle.imageUri.isNullOrEmpty()) {
                try {
                    imageVehicle.setImageURI(android.net.Uri.parse(vehicle.imageUri))
                } catch (e: Exception) {
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
            textCity.text = "Location: ${vehicle.city}"
            textYear.text = "Year: ${vehicle.year} | Brand: ${vehicle.brand} | Model: ${vehicle.model}"
        }
    }

    private fun setupButtons(vehicle: com.example.apnivehicle.models.Vehicle) {
        binding.apply {
            buttonContact.setOnClickListener {
                Toast.makeText(
                    this@DetailActivity,
                    "Contacting seller for ${vehicle.title}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            buttonFavorite.apply {
                text = if (vehicle.isFavorite) "★ Saved" else "☆ Save"
                setOnClickListener {
                    VehicleRepository.toggleFavorite(vehicle.id)?.let {
                        if (it.isFavorite) {
                            NotificationHelper(this@DetailActivity).showFavoriteAdded(it.title)
                            buttonFavorite.text = "★ Saved"
                            Toast.makeText(
                                this@DetailActivity,
                                "Added to favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            buttonFavorite.text = "☆ Save"
                            Toast.makeText(
                                this@DetailActivity,
                                "Removed from favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}
