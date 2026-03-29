package com.example.apnivehicle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ItemVehicleCardBinding
import com.example.apnivehicle.models.Vehicle
import java.text.NumberFormat
import java.util.Locale

class VehicleAdapter(
    private val onItemClick: (Vehicle) -> Unit,
    private val onFavoriteClick: (Vehicle) -> Unit,
    private val onEditClick: (Vehicle) -> Unit = {},
    private val onDeleteClick: (Vehicle) -> Unit = {},
    private val showOwnerActions: Boolean = false
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    companion object {
        private val priceFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PK"))
    }

    private var vehicles: List<Vehicle> = emptyList()

    inner class VehicleViewHolder(
        private val binding: ItemVehicleCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(vehicle: Vehicle) {
            binding.textTitle.text = vehicle.title
            binding.textPrice.text = priceFormatter.format(vehicle.price)
            binding.textCity.text = vehicle.city
            binding.textYear.text = vehicle.year.toString()
            
            // Load image from URI or drawable
            if (!vehicle.imageUri.isNullOrEmpty()) {
                Glide.with(binding.imageVehicle.context)
                    .load(vehicle.imageUri)
                    .centerCrop()
                    .into(binding.imageVehicle)
            } else if (vehicle.image != 0) {
                binding.imageVehicle.setImageResource(vehicle.image)
            } else {
                binding.imageVehicle.setImageResource(R.drawable.ic_car_rental)
            }

            val favoriteTint = if (vehicle.isFavorite) R.color.primary else R.color.text_secondary
            binding.iconFavorite.setColorFilter(ContextCompat.getColor(binding.root.context, favoriteTint))

            binding.iconFavorite.setOnClickListener { onFavoriteClick(vehicle) }
            binding.root.setOnClickListener { onItemClick(vehicle) }

            binding.buttonEdit.visibility = if (showOwnerActions) View.VISIBLE else View.GONE
            binding.buttonDelete.visibility = if (showOwnerActions) View.VISIBLE else View.GONE
            binding.buttonEdit.setOnClickListener { onEditClick(vehicle) }
            binding.buttonDelete.setOnClickListener { onDeleteClick(vehicle) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return VehicleViewHolder(ItemVehicleCardBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(vehicles[position])
    }

    override fun getItemCount(): Int = vehicles.size

    fun submitList(items: List<Vehicle>) {
        vehicles = items
        notifyDataSetChanged()
    }

    fun updateList(items: List<Vehicle>) {
        vehicles = items
        notifyDataSetChanged()
    }
}