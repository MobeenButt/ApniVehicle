package com.example.apnivehicle.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apnivehicle.R
import com.example.apnivehicle.activities.DetailActivity
import com.example.apnivehicle.adapters.VehicleAdapter
import com.example.apnivehicle.databinding.FragmentAdvancedSearchBinding
import com.example.apnivehicle.models.SearchPreference
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.ToolbarActionHandler
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip

class AdvancedSearchFragment : Fragment(), ToolbarActionHandler {

    private var _binding: FragmentAdvancedSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VehicleAdapter
    private var currentResults = listOf<String>()
    private val activeFilters = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdvancedSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        loadFiltersData()
        setupListeners()
    }

    private fun setupUI() {
        adapter = VehicleAdapter(
            onItemClick = { vehicle ->
                VehicleRepository.incrementViewCount(vehicle.id)
                startActivity(Intent(requireContext(), DetailActivity::class.java)
                    .putExtra(DetailActivity.EXTRA_VEHICLE_ID, vehicle.id))
            },
            onFavoriteClick = { vehicle ->
                VehicleRepository.toggleFavorite(vehicle.id)
                loadResults()
            }
        )

        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdvancedSearchFragment.adapter
        }
    }

    private fun loadFiltersData() {
        // Load unique values for dropdowns
        binding.spinnerBrand.apply {
            val brands = listOf("All Brands") + VehicleRepository.getUniqueBrands()
            setupSpinner(this, brands)
        }

        binding.spinnerTransmission.apply {
            val transmissions = listOf("All") + VehicleRepository.getUniqueTransmissions()
            setupSpinner(this, transmissions)
        }

        binding.spinnerFuelType.apply {
            val fuelTypes = listOf("All") + VehicleRepository.getUniqueFuelTypes()
            setupSpinner(this, fuelTypes)
        }

        // RadioGroup for condition is already in XML - no need to populate

        // Set price range limits
        val (minPrice, maxPrice) = VehicleRepository.getPriceRange()
        binding.sliderPrice.valueFrom = minPrice.toFloat()
        binding.sliderPrice.valueTo = maxPrice.toFloat()
        binding.sliderPrice.setValues(minPrice.toFloat(), maxPrice.toFloat())

        // Set mileage range limits
        val (minMileage, maxMileage) = VehicleRepository.getMileageRange()
        binding.sliderMileage.valueFrom = minMileage.toFloat()
        binding.sliderMileage.valueTo = maxMileage.toFloat()
        binding.sliderMileage.setValues(minMileage.toFloat(), maxMileage.toFloat())
    }

    private fun setupSpinner(spinner: android.widget.Spinner, items: List<String>) {
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupListeners() {
        binding.buttonSearch.setOnClickListener {
            performSearch()
        }

        binding.buttonSavePreference.setOnClickListener {
            saveSearchPreference()
        }
        
        // Listen to RadioGroup changes
        binding.radioGroupCondition.setOnCheckedChangeListener { _, _ ->
            updateActiveFilters()
        }
        
        // Listen to spinner changes
        binding.spinnerBrand.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateActiveFilters()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        binding.spinnerTransmission.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateActiveFilters()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        binding.spinnerFuelType.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateActiveFilters()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun performSearch() {
        val brand = if (binding.spinnerBrand.selectedItem.toString() != "All Brands") 
            binding.spinnerBrand.selectedItem.toString() else null
        
        val model = binding.inputModel.text.toString().trim().ifEmpty { null }
        
        val prices = binding.sliderPrice.values
        val minPrice = if (prices[0] > 0f) prices[0].toLong() else null
        val maxPrice = if (prices[1] < 10000000f) prices[1].toLong() else null

        val mileages = binding.sliderMileage.values
        val minMileage = if (mileages[0] > 0f) mileages[0].toInt() else null
        val maxMileage = if (mileages[1] < 500000f) mileages[1].toInt() else null

        val transmission = if (binding.spinnerTransmission.selectedItem.toString() != "All")
            binding.spinnerTransmission.selectedItem.toString() else null
        
        val fuelType = if (binding.spinnerFuelType.selectedItem.toString() != "All")
            binding.spinnerFuelType.selectedItem.toString() else null
        
        // Get condition from RadioGroup
        val condition = getSelectedCondition()

        val results = VehicleRepository.advancedSearch(
            brand = brand,
            model = model,
            minPrice = minPrice,
            maxPrice = maxPrice,
            minMileage = minMileage,
            maxMileage = maxMileage,
            transmission = transmission,
            fuelType = fuelType,
            condition = condition
        )

        currentResults = results.map { it.id }
        loadResults()
        updateActiveFilters()
        Toast.makeText(requireContext(), "Found ${results.size} vehicles", Toast.LENGTH_SHORT).show()
    }
    
    private fun getSelectedCondition(): String? {
        return when (binding.radioGroupCondition.checkedRadioButtonId) {
            R.id.radio_new -> "New"
            R.id.radio_used -> "Used"
            R.id.radio_certified -> "Certified"
            else -> null // All
        }
    }
    
    private fun updateActiveFilters() {
        activeFilters.clear()
        
        // Add brand filter
        val brand = binding.spinnerBrand.selectedItem.toString()
        if (brand != "All Brands") {
            activeFilters["Brand"] = brand
        }
        
        // Add model filter
        val model = binding.inputModel.text.toString().trim()
        if (model.isNotEmpty()) {
            activeFilters["Model"] = model
        }
        
        // Add transmission filter
        val transmission = binding.spinnerTransmission.selectedItem.toString()
        if (transmission != "All") {
            activeFilters["Transmission"] = transmission
        }
        
        // Add fuel type filter
        val fuelType = binding.spinnerFuelType.selectedItem.toString()
        if (fuelType != "All") {
            activeFilters["Fuel"] = fuelType
        }
        
        // Add condition filter from RadioGroup
        val condition = getSelectedCondition()
        if (condition != null) {
            activeFilters["Condition"] = condition
        }
        
        // Update FlexboxLayout with chips
        updateFilterChips()
    }
    
    private fun updateFilterChips() {
        binding.flexboxFilters.removeAllViews()
        
        if (activeFilters.isEmpty()) {
            binding.textActiveFiltersLabel.visibility = View.GONE
            binding.flexboxFilters.visibility = View.GONE
            return
        }
        
        binding.textActiveFiltersLabel.visibility = View.VISIBLE
        binding.flexboxFilters.visibility = View.VISIBLE
        
        activeFilters.forEach { (key, value) ->
            val chip = Chip(requireContext()).apply {
                text = "$key: $value"
                isCloseIconVisible = true
                setChipBackgroundColorResource(R.color.primary)
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                
                // Add margin
                val params = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(8, 4, 8, 4)
                layoutParams = params
                
                setOnCloseIconClickListener {
                    removeFilter(key)
                }
            }
            binding.flexboxFilters.addView(chip)
        }
    }
    
    private fun removeFilter(filterKey: String) {
        when (filterKey) {
            "Brand" -> binding.spinnerBrand.setSelection(0)
            "Model" -> binding.inputModel.text?.clear()
            "Transmission" -> binding.spinnerTransmission.setSelection(0)
            "Fuel" -> binding.spinnerFuelType.setSelection(0)
            "Condition" -> binding.radioGroupCondition.check(R.id.radio_all_condition)
        }
        updateActiveFilters()
    }

    private fun loadResults() {
        val results = VehicleRepository.getVehicles().filter { it.id in currentResults }
        adapter.updateList(results)
    }

    private fun saveSearchPreference() {
        val name = binding.inputPreferenceName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a name for this search", Toast.LENGTH_SHORT).show()
            return
        }

        val brand = if (binding.spinnerBrand.selectedItem.toString() != "All Brands")
            binding.spinnerBrand.selectedItem.toString() else ""
        
        val model = binding.inputModel.text.toString().trim()
        
        val prices = binding.sliderPrice.values
        val minPrice = prices[0].toLong()
        val maxPrice = prices[1].toLong()

        val mileages = binding.sliderMileage.values
        val minMileage = mileages[0].toInt()
        val maxMileage = mileages[1].toInt()

        val transmission = if (binding.spinnerTransmission.selectedItem.toString() != "All")
            binding.spinnerTransmission.selectedItem.toString() else ""
        
        val fuelType = if (binding.spinnerFuelType.selectedItem.toString() != "All")
            binding.spinnerFuelType.selectedItem.toString() else ""
        
        // Get condition from RadioGroup
        val condition = getSelectedCondition() ?: ""

        val preference = SearchPreference(
            name = name,
            brand = brand,
            model = model,
            minPrice = minPrice,
            maxPrice = maxPrice,
            minMileage = minMileage,
            maxMileage = maxMileage,
            transmission = transmission,
            fuelType = fuelType,
            condition = condition
        )

        VehicleRepository.saveSearchPreference(preference)
        Toast.makeText(requireContext(), "Search preference saved", Toast.LENGTH_SHORT).show()
        binding.inputPreferenceName.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

