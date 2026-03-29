package com.example.apnivehicle.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apnivehicle.adapters.VehicleAdapter
import com.example.apnivehicle.databinding.FragmentComparisonBinding
import com.example.apnivehicle.models.Vehicle
import com.example.apnivehicle.repository.VehicleRepository
import java.text.NumberFormat
import java.util.Locale

class ComparisonFragment : Fragment() {

    private var _binding: FragmentComparisonBinding? = null
    private val binding get() = _binding!!

    private val selectedVehicles = mutableListOf<Vehicle>()
    private val priceFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PK"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComparisonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadVehicles()
        setupListeners()
        displayComparison()
    }

    private fun loadVehicles() {
        selectedVehicles.clear()
        selectedVehicles.addAll(VehicleRepository.getFavorites().take(3))
    }

    private fun setupListeners() {
        binding.btnAddToComparison.setOnClickListener {
            showVehicleSelectionDialog()
        }

        binding.btnClearComparison.setOnClickListener {
            selectedVehicles.clear()
            displayComparison()
            Toast.makeText(requireContext(), "Comparison cleared", Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveComparison.setOnClickListener {
            VehicleRepository.saveComparison(selectedVehicles.map { it.id })
            Toast.makeText(requireContext(), "Comparison saved", Toast.LENGTH_SHORT).show()
        }

        binding.btnExportComparison.setOnClickListener {
            exportComparison()
        }
    }

    private fun displayComparison() {
        if (selectedVehicles.isEmpty()) {
            binding.comparisonContainer.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
            return
        }

        binding.comparisonContainer.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE

        binding.tvVehicleCount.text = "Comparing ${selectedVehicles.size} vehicles"

        // Clear previous items
        binding.comparisonList.removeAllViews()

        // Add comparison rows
        addComparisonRow("Title", selectedVehicles.map { it.title })
        addComparisonRow("Price", selectedVehicles.map { priceFormatter.format(it.price) })
        addComparisonRow("Year", selectedVehicles.map { it.year.toString() })
        addComparisonRow("Brand", selectedVehicles.map { it.brand })
        addComparisonRow("Model", selectedVehicles.map { it.model })
        addComparisonRow("Mileage", selectedVehicles.map { "${it.mileage} km" })
        addComparisonRow("Transmission", selectedVehicles.map { it.transmission })
        addComparisonRow("Fuel Type", selectedVehicles.map { it.fuelType })
        addComparisonRow("Condition", selectedVehicles.map { it.condition })
        addComparisonRow("Location", selectedVehicles.map { it.city })
        addComparisonRow("Seller Rating", selectedVehicles.map { "${it.sellerRating}/5.0" })
    }

    private fun addComparisonRow(label: String, values: List<String>) {
        val rowView = android.widget.LinearLayout(requireContext()).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = android.widget.LinearLayout.HORIZONTAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(
                if (binding.comparisonList.childCount % 2 == 0)
                    android.graphics.Color.parseColor("#F5F5F5")
                else
                    android.graphics.Color.WHITE
            )
        }

        // Label column
        val labelView = android.widget.TextView(requireContext()).apply {
            text = label
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#424242"))
            typeface = android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD)
            setPadding(8, 0, 8, 0)
        }
        rowView.addView(labelView)

        // Value columns
        values.forEach { value ->
            val valueView = android.widget.TextView(requireContext()).apply {
                text = value
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
                setPadding(8, 0, 8, 0)
            }
            rowView.addView(valueView)
        }

        binding.comparisonList.addView(rowView)
    }

    private fun showVehicleSelectionDialog() {
        val availableVehicles = VehicleRepository.getVehicles()
            .filter { it.id !in selectedVehicles.map { v -> v.id } }

        if (availableVehicles.isEmpty()) {
            Toast.makeText(requireContext(), "No more vehicles available", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedVehicles.size >= 3) {
            Toast.makeText(requireContext(), "Maximum 3 vehicles can be compared", Toast.LENGTH_SHORT).show()
            return
        }

        val vehicleNames = availableVehicles.map { it.title }.toTypedArray()
        val checkedItems = BooleanArray(vehicleNames.size) { false }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Vehicles to Compare")
            .setMultiChoiceItems(vehicleNames, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Add") { _, _ ->
                checkedItems.forEachIndexed { index, isChecked ->
                    if (isChecked && selectedVehicles.size < 3) {
                        selectedVehicles.add(availableVehicles[index])
                    }
                }
                displayComparison()
                Toast.makeText(requireContext(), "Vehicle added to comparison", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportComparison() {
        if (selectedVehicles.isEmpty()) {
            Toast.makeText(requireContext(), "Please select vehicles to compare", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()
        sb.append("ApniVehicle - Vehicle Comparison\n\n")

        val headers = mutableListOf("Specification")
        headers.addAll(selectedVehicles.map { it.title })
        sb.append(headers.joinToString(" | ") + "\n")
        sb.append("-".repeat(80) + "\n")

        sb.append("Price|${selectedVehicles.map { it.price }.joinToString("|")}\n")
        sb.append("Year|${selectedVehicles.map { it.year }.joinToString("|")}\n")
        sb.append("Mileage|${selectedVehicles.map { it.mileage }.joinToString("|")}\n")
        sb.append("Transmission|${selectedVehicles.map { it.transmission }.joinToString("|")}\n")
        sb.append("Fuel Type|${selectedVehicles.map { it.fuelType }.joinToString("|")}\n")
        sb.append("Condition|${selectedVehicles.map { it.condition }.joinToString("|")}\n")
        sb.append("Location|${selectedVehicles.map { it.city }.joinToString("|")}\n")
        sb.append("Seller Rating|${selectedVehicles.map { it.sellerRating }.joinToString("|")}\n")

        Toast.makeText(
            requireContext(),
            "Comparison data prepared (Share functionality in production)",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

