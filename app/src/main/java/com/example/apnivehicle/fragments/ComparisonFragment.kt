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

        try {
            // Create PDF document
            val pdfDocument = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = android.graphics.Paint()

            // Title
            paint.textSize = 20f
            paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            canvas.drawText("Vehicle Comparison Report", 50f, 50f, paint)

            // Date
            paint.textSize = 12f
            paint.typeface = android.graphics.Typeface.DEFAULT
            val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            canvas.drawText("Generated: ${dateFormat.format(java.util.Date())}", 50f, 75f, paint)

            // Draw comparison table
            var yPos = 120f
            paint.textSize = 10f
            paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)

            // Headers
            canvas.drawText("Specification", 50f, yPos, paint)
            selectedVehicles.forEachIndexed { index, vehicle ->
                canvas.drawText(vehicle.title.take(15), 200f + (index * 150f), yPos, paint)
            }
            yPos += 25f

            // Data rows
            paint.typeface = android.graphics.Typeface.DEFAULT
            val rows = listOf(
                "Price" to selectedVehicles.map { priceFormatter.format(it.price) },
                "Year" to selectedVehicles.map { it.year.toString() },
                "Brand" to selectedVehicles.map { it.brand },
                "Model" to selectedVehicles.map { it.model },
                "Mileage" to selectedVehicles.map { "${it.mileage} km" },
                "Transmission" to selectedVehicles.map { it.transmission },
                "Fuel Type" to selectedVehicles.map { it.fuelType },
                "Condition" to selectedVehicles.map { it.condition },
                "Location" to selectedVehicles.map { it.city },
                "Rating" to selectedVehicles.map { "${it.sellerRating}/5" }
            )

            rows.forEach { (label, values) ->
                canvas.drawText(label, 50f, yPos, paint)
                values.forEachIndexed { index, value ->
                    canvas.drawText(value.take(15), 200f + (index * 150f), yPos, paint)
                }
                yPos += 20f
            }

            pdfDocument.finishPage(page)

            // Save PDF
            val fileName = "vehicle_comparison_${System.currentTimeMillis()}.pdf"
            val file = java.io.File(requireContext().getExternalFilesDir(null), fileName)
            
            pdfDocument.writeTo(java.io.FileOutputStream(file))
            pdfDocument.close()

            // Share PDF
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(android.content.Intent.createChooser(shareIntent, "Share Comparison PDF"))
            Toast.makeText(requireContext(), "PDF exported successfully", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

