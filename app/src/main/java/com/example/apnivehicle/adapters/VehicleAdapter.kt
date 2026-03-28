package com.example.apnivehicle.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ItemVehicleCardBinding
import com.example.apnivehicle.models.Vehicle
import java.text.NumberFormat
import java.util.Locale

class VehicleAdapter(
    private var vehicles: List<Vehicle> = emptyList(),
    private val onViewDetailsClick: (Vehicle) -> Unit = {},
    private val onFavoriteClick: (Vehicle) -> Unit = {}
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    class VehicleViewHolder(
        private val binding: ItemVehicleCardBinding,
        private val onViewDetailsClick: (Vehicle) -> Unit,
        private val onFavoriteClick: (Vehicle) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(vehicle: Vehicle) {
            with(binding) {
                // Set text fields
                textTitle.text = vehicle.title
                textMileage.text = vehicle.mileage
                textLocation.text = vehicle.location
                textFuel.text = vehicle.fuelType

                // Format and set price
                val priceFormat = NumberFormat.getCurrencyInstance(Locale("ur", "PK"))
                textPrice.text = priceFormat.format(vehicle.price)

                // Show/hide verified chip
                chipVerified.visibility = if (vehicle.isVerified) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Load image (using placeholder for now)
                imageVehicle.setImageResource(R.drawable.ic_launcher_background)

                // Set up click listeners
                buttonViewDetails.setOnClickListener {
                    onViewDetailsClick(vehicle)
                }

                iconFavorite.setOnClickListener {
                    onFavoriteClick(vehicle)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemVehicleCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VehicleViewHolder(binding, onViewDetailsClick, onFavoriteClick)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(vehicles[position])
    }

    override fun getItemCount(): Int = vehicles.size

    fun updateVehicles(newVehicles: List<Vehicle>) {
        vehicles = newVehicles
        notifyDataSetChanged()
    }
}