package com.example.apnivehicle.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import com.example.apnivehicle.databinding.DialogEditVehicleBinding
import com.example.apnivehicle.models.Vehicle
import com.example.apnivehicle.repository.VehicleRepository

object VehicleDialogs {

    data class FilterValues(
        val city: String?,
        val minPrice: Long?,
        val maxPrice: Long?,
        val brand: String?,
        val year: Int?
    )

    fun showDeleteConfirmDialog(
        context: Context,
        vehicle: Vehicle,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle("Delete Vehicle")
            .setMessage("Are you sure you want to delete ${vehicle.title}?")
            .setPositiveButton("Delete") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun showSortDialog(
        context: Context,
        onSelected: (VehicleRepository.SortOption) -> Unit
    ) {
        val options = arrayOf("Price Low-High", "Price High-Low", "Alphabetical", "Latest", "Oldest")
        AlertDialog.Builder(context)
            .setTitle("Sort By")
            .setItems(options) { _, which ->
                val selected = when (which) {
                    0 -> VehicleRepository.SortOption.PRICE_LOW_HIGH
                    1 -> VehicleRepository.SortOption.PRICE_HIGH_LOW
                    2 -> VehicleRepository.SortOption.ALPHABETICAL
                    3 -> VehicleRepository.SortOption.LATEST
                    else -> VehicleRepository.SortOption.OLDEST
                }
                onSelected(selected)
            }
            .show()
    }

    fun showFilterDialog(
        context: Context,
        onApply: (FilterValues) -> Unit
    ) {
        val fields = listOf("City", "Min Price", "Max Price", "Brand", "Year")
        val edits = fields.map { hint ->
            EditText(context).apply {
                this.hint = hint
                setPadding(24, 24, 24, 24)
            }
        }

        val container = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            edits.forEach { addView(it) }
        }

        AlertDialog.Builder(context)
            .setTitle("Filter Vehicles")
            .setView(container)
            .setPositiveButton("Apply") { _, _ ->
                onApply(
                    FilterValues(
                        city = edits[0].text.toString().ifBlank { null },
                        minPrice = edits[1].text.toString().toLongOrNull(),
                        maxPrice = edits[2].text.toString().toLongOrNull(),
                        brand = edits[3].text.toString().ifBlank { null },
                        year = edits[4].text.toString().toIntOrNull()
                    )
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun showEditVehicleDialog(
        context: Context,
        vehicle: Vehicle,
        onSave: (Vehicle) -> Unit
    ) {
        val binding = DialogEditVehicleBinding.inflate(LayoutInflater.from(context))
        binding.inputTitle.setText(vehicle.title)
        binding.inputPrice.setText(vehicle.price.toString())
        binding.inputCity.setText(vehicle.city)
        binding.inputYear.setText(vehicle.year.toString())
        binding.inputDescription.setText(vehicle.description)

        AlertDialog.Builder(context)
            .setTitle("Edit Vehicle")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val updated = vehicle.copy(
                    title = binding.inputTitle.text.toString(),
                    price = binding.inputPrice.text.toString().toLongOrNull() ?: vehicle.price,
                    city = binding.inputCity.text.toString(),
                    year = binding.inputYear.text.toString().toIntOrNull() ?: vehicle.year,
                    description = binding.inputDescription.text.toString()
                )
                onSave(updated)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

