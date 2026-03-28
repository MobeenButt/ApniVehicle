package com.example.apnivehicle.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        val vehicle = VehicleRepository.getVehicles().find { it.id == vehicleId }

        if (vehicle == null) {
            finish()
            return
        }

        binding.imageVehicle.setImageResource(vehicle.image)
        binding.textTitle.text = vehicle.title
        binding.textPrice.text = priceFormatter.format(vehicle.price)
        binding.textDescription.text = vehicle.description
        binding.textCity.text = vehicle.city
        binding.textYear.text = vehicle.year.toString()

        binding.buttonContact.setOnClickListener {
            Toast.makeText(this, "Contact action for ${vehicle.title}", Toast.LENGTH_SHORT).show()
        }

        binding.buttonFavorite.setOnClickListener {
            VehicleRepository.toggleFavorite(vehicle.id)?.let {
                if (it.isFavorite) {
                    NotificationHelper(this).showFavoriteAdded(it.title)
                    Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

