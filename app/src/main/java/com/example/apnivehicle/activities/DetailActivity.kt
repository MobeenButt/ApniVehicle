package com.example.apnivehicle.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ActivityDetailBinding
import com.example.apnivehicle.repository.VehicleRepository
import java.text.NumberFormat
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
    }

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        val vehicleId = intent.getStringExtra(EXTRA_VEHICLE_ID)
        val vehicle = VehicleRepository.getVehicles().find { it.id == vehicleId }

        if (vehicle == null) {
            Toast.makeText(this, "Vehicle not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Image handling
        if (vehicle.imageUri != null) {
            binding.imageVehicle.setImageURI(Uri.parse(vehicle.imageUri))
        } else {
            binding.imageVehicle.setImageResource(if (vehicle.image != 0) vehicle.image else R.drawable.ic_directions_car)
        }

        // PakWheels Features: Verification & Inspection
        if (vehicle.isVerified) {
            binding.badgeVerified.visibility = View.VISIBLE
            binding.layoutInspection.visibility = View.VISIBLE
            binding.inspectionProgress.progress = vehicle.inspectionScore * 10
            binding.textInspectionScore.text = "${vehicle.inspectionScore}/10"
        } else {
            binding.badgeVerified.visibility = View.GONE
            binding.layoutInspection.visibility = View.GONE
        }

        // Basic Info
        binding.textTitle.text = vehicle.title
        binding.textPrice.text = "PKR ${NumberFormat.getNumberInstance(Locale.US).format(vehicle.price)}"
        binding.textCity.text = vehicle.city
        binding.textDescription.text = vehicle.description
        binding.textDate.text = "Listed 1 day ago"

        // PakWheels-style Quick Info
        binding.quickYear.text = vehicle.year.toString()
        binding.quickFuel.text = vehicle.fuelType
        binding.quickTrans.text = vehicle.transmission

        // Detailed Specs
        binding.detailEngine.text = "Engine: ${vehicle.engineCapacity}"
        binding.detailColor.text = "Color: ${vehicle.color}"
        binding.detailAssembly.text = "Assembly: ${vehicle.assembly}"
        binding.detailMileage.text = "KM Driven: ${NumberFormat.getNumberInstance(Locale.US).format(vehicle.mileage)}"

        // Actions
        binding.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${vehicle.sellerPhone}")
            startActivity(intent)
        }

        binding.btnChat.setOnClickListener {
            Toast.makeText(this, "Opening chat with ${vehicle.sellerName}...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
