package com.example.apnivehicle.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apnivehicle.R
import com.example.apnivehicle.activities.DetailActivity
import com.example.apnivehicle.adapters.VehicleAdapter
import com.example.apnivehicle.databinding.FragmentAdvancedSearchBinding
import com.example.apnivehicle.models.SearchPreference
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.Constants
import com.example.apnivehicle.utils.NetworkMonitor
import com.example.apnivehicle.utils.ToolbarActionHandler
import com.example.apnivehicle.utils.setDebouncedClickListener
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.Locale

class AdvancedSearchFragment : Fragment(), ToolbarActionHandler {

    companion object {
        private const val ARG_PREFERENCE = "arg_preference"
        private const val MIN_YEAR = 1990
        private val priceFormatter = NumberFormat.getNumberInstance(Locale.getDefault())

        fun newInstance(preference: SearchPreference? = null): AdvancedSearchFragment {
            return AdvancedSearchFragment().apply {
                if (preference != null) {
                    arguments = Bundle().apply {
                        putString(ARG_PREFERENCE, com.google.gson.Gson().toJson(preference))
                    }
                }
            }
        }
    }

    private var _binding: FragmentAdvancedSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VehicleAdapter
    private var currentResultIds = listOf<String>()
    private val activeFilters = mutableMapOf<String, String>()
    private var hasSearched = false

    // slider bounds (set once in loadFiltersData)
    private var priceMin = 0f
    private var priceMax = 10_000_000f
    private var mileageMin = 0f
    private var mileageMax = 500_000f
    private val yearMin = MIN_YEAR.toFloat()
    private val yearMax = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toFloat()

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdvancedSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        loadFilterOptions()
        setupSliderLabels()
        setupButtons()

        // Pre-fill from saved preference if launched from SavedSearches
        arguments?.getString(ARG_PREFERENCE)?.let { json ->
            try {
                val pref = com.google.gson.Gson().fromJson(json, SearchPreference::class.java)
                prefillFromPreference(pref)
            } catch (_: Exception) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    private fun setupRecycler() {
        adapter = VehicleAdapter(
            onItemClick = { vehicle ->
                VehicleRepository.incrementViewCount(vehicle.id)
                startActivity(
                    Intent(requireContext(), DetailActivity::class.java)
                        .putExtra(DetailActivity.EXTRA_VEHICLE_ID, vehicle.id)
                )
            },
            onFavoriteClick = { vehicle ->
                VehicleRepository.toggleFavorite(vehicle.id)
                renderResults(currentResultIds)
            }
        )
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AdvancedSearchFragment.adapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadFilterOptions() {
        // City dropdown
        val cities = listOf("Any City") + Constants.PAKISTANI_CITIES
        setupDropdown(binding.spinnerCity, cities)

        // Brand dropdown  — live data from repo + constants fallback
        val repoBrands = VehicleRepository.getUniqueBrands()
        val allBrands = listOf("Any Brand") +
                if (repoBrands.isNotEmpty()) repoBrands else Constants.VEHICLE_MAKES
        setupDropdown(binding.spinnerBrand, allBrands)

        // Transmission
        val transmissions = listOf("Any") + Constants.TRANSMISSION_TYPES
        setupDropdown(binding.spinnerTransmission, transmissions)

        // Fuel type
        val fuels = listOf("Any") + Constants.FUEL_TYPES
        setupDropdown(binding.spinnerFuelType, fuels)

        // Price slider
        val (rawMin, rawMax) = VehicleRepository.getPriceRange()
        priceMin = rawMin.toFloat()
        priceMax = if (rawMax.toFloat() > priceMin) rawMax.toFloat() else priceMin + 10_000_000f
        with(binding.sliderPrice) {
            valueFrom = priceMin
            valueTo   = priceMax
            setValues(priceMin, priceMax)
        }

        // Year slider
        with(binding.sliderYear) {
            valueFrom = yearMin
            valueTo   = yearMax
            setValues(yearMin, yearMax)
        }

        // Mileage slider
        val (rawMinM, rawMaxM) = VehicleRepository.getMileageRange()
        mileageMin = rawMinM.toFloat()
        mileageMax = if (rawMaxM.toFloat() > mileageMin) rawMaxM.toFloat() else mileageMin + 300_000f
        with(binding.sliderMileage) {
            valueFrom = mileageMin
            valueTo   = mileageMax
            setValues(mileageMin, mileageMax)
        }

        updateAllLabels()
    }

    private fun setupDropdown(view: AutoCompleteTextView, items: List<String>) {
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        view.setAdapter(adapter)
        view.setText(items.firstOrNull() ?: "", false)
        view.setOnItemClickListener { _, _, _, _ -> updateActiveFilterChips() }
    }

    private fun setupSliderLabels() {
        binding.sliderPrice.addOnChangeListener { _, _, _ ->
            updateAllLabels()
            updateActiveFilterChips()
        }
        binding.sliderYear.addOnChangeListener { _, _, _ ->
            updateAllLabels()
            updateActiveFilterChips()
        }
        binding.sliderMileage.addOnChangeListener { _, _, _ ->
            updateAllLabels()
            updateActiveFilterChips()
        }
        binding.chipGroupCondition.setOnCheckedStateChangeListener { _, _ ->
            updateActiveFilterChips()
        }
    }

    private fun updateAllLabels() {
        val b = _binding ?: return

        val prices = b.sliderPrice.values
        b.tvPriceRangeLabel.text = if (prices[0] == priceMin && prices[1] == priceMax) {
            "Any price"
        } else {
            "PKR ${priceFormatter.format(prices[0].toLong())} – PKR ${priceFormatter.format(prices[1].toLong())}"
        }

        val years = b.sliderYear.values
        b.tvYearRangeLabel.text = if (years[0] == yearMin && years[1] == yearMax) {
            "Any year"
        } else {
            "${years[0].toInt()} – ${years[1].toInt()}"
        }

        val miles = b.sliderMileage.values
        b.tvMileageRangeLabel.text = if (miles[0] == mileageMin && miles[1] == mileageMax) {
            "Any mileage"
        } else {
            "${priceFormatter.format(miles[0].toLong())} km – ${priceFormatter.format(miles[1].toLong())} km"
        }
    }

    private fun setupButtons() {
        binding.buttonSearch.setDebouncedClickListener(600L) { performSearch() }

        binding.buttonSavePreference.setDebouncedClickListener(800L) { saveSearchPreference() }

        binding.btnResetFilters.setOnClickListener { resetAllFilters() }
    }

    // ── Search ───────────────────────────────────────────────────────────────

    private fun performSearch() {
        val city = binding.spinnerCity.text.toString().trim()
            .takeIf { it.isNotEmpty() && it != "Any City" }

        val brand = binding.spinnerBrand.text.toString().trim()
            .takeIf { it.isNotEmpty() && it != "Any Brand" }

        val model = binding.inputModel.text.toString().trim().ifEmpty { null }

        val prices = binding.sliderPrice.values
        val minPrice = if (prices[0] > priceMin) prices[0].toLong() else null
        val maxPrice = if (prices[1] < priceMax) prices[1].toLong() else null

        val years = binding.sliderYear.values
        val minYear = if (years[0] > yearMin) years[0].toInt() else null
        val maxYear = if (years[1] < yearMax) years[1].toInt() else null

        val mileages = binding.sliderMileage.values
        val minMileage = if (mileages[0] > mileageMin) mileages[0].toInt() else null
        val maxMileage = if (mileages[1] < mileageMax) mileages[1].toInt() else null

        val transmission = binding.spinnerTransmission.text.toString().trim()
            .takeIf { it.isNotEmpty() && it != "Any" }

        val fuelType = binding.spinnerFuelType.text.toString().trim()
            .takeIf { it.isNotEmpty() && it != "Any" }

        val condition = getSelectedCondition()

        // Run search — city and year passed together
        var results = VehicleRepository.advancedSearch(
            brand        = brand,
            model        = model,
            city         = city,
            minPrice     = minPrice,
            maxPrice     = maxPrice,
            minMileage   = minMileage,
            maxMileage   = maxMileage,
            transmission = transmission,
            fuelType     = fuelType,
            condition    = condition
        )

        // Year filter (not in advancedSearch signature)
        if (minYear != null) results = results.filter { it.year >= minYear }
        if (maxYear != null) results = results.filter { it.year <= maxYear }

        currentResultIds = results.map { it.id }
        hasSearched = true
        updateActiveFilterChips()
        renderResults(currentResultIds)
    }

    private fun getSelectedCondition(): String? {
        return when (binding.chipGroupCondition.checkedChipId) {
            R.id.chip_condition_new       -> "New"
            R.id.chip_condition_used      -> "Used"
            R.id.chip_condition_certified -> "Certified"
            else                          -> null
        }
    }

    private fun renderResults(ids: List<String>) {
        val b = _binding ?: return
        val results = VehicleRepository.getVehicles().filter { it.id in ids }

        val count = results.size
        b.tvResultsCount.text = if (hasSearched) {
            if (count == 0) "No vehicles found" else "$count vehicle${if (count == 1) "" else "s"} found"
        } else {
            "Results will appear here"
        }

        if (results.isEmpty()) {
            b.rvSearchResults.visibility = View.GONE
            b.layoutEmptyState.visibility = View.VISIBLE
            b.tvEmptyMessage.text = if (hasSearched) {
                "No vehicles match your filters.\nTry broadening your search."
            } else {
                "Set your filters and tap Search"
            }
        } else {
            b.rvSearchResults.visibility = View.VISIBLE
            b.layoutEmptyState.visibility = View.GONE
            adapter.submitList(results)
        }
    }

    // ── Active filter chips ───────────────────────────────────────────────────

    private fun updateActiveFilterChips() {
        activeFilters.clear()

        binding.spinnerCity.text.toString().trim()
            .takeIf { it.isNotEmpty() && it != "Any City" }
            ?.let { activeFilters["City"] = it }

        binding.spinnerBrand.text.toString().trim()
            .takeIf { it.isNotEmpty() && it != "Any Brand" }
            ?.let { activeFilters["Brand"] = it }

        binding.inputModel.text.toString().trim()
            .takeIf { it.isNotEmpty() }
            ?.let { activeFilters["Model"] = it }

        binding.spinnerTransmission.text.toString().trim()
            .takeIf { it.isNotEmpty() && it != "Any" }
            ?.let { activeFilters["Transmission"] = it }

        binding.spinnerFuelType.text.toString().trim()
            .takeIf { it.isNotEmpty() && it != "Any" }
            ?.let { activeFilters["Fuel"] = it }

        getSelectedCondition()?.let { activeFilters["Condition"] = it }

        val prices = binding.sliderPrice.values
        if (prices[0] > priceMin || prices[1] < priceMax) {
            activeFilters["Price"] =
                "PKR ${priceFormatter.format(prices[0].toLong())}–${priceFormatter.format(prices[1].toLong())}"
        }

        val years = binding.sliderYear.values
        if (years[0] > yearMin || years[1] < yearMax) {
            activeFilters["Year"] = "${years[0].toInt()}–${years[1].toInt()}"
        }

        val miles = binding.sliderMileage.values
        if (miles[0] > mileageMin || miles[1] < mileageMax) {
            activeFilters["Mileage"] =
                "${priceFormatter.format(miles[0].toLong())}–${priceFormatter.format(miles[1].toLong())} km"
        }

        renderFilterChips()
    }

    private fun renderFilterChips() {
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
                setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        requireContext(), android.R.color.white
                    )
                )
                val lp = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 8, 8) }
                layoutParams = lp
                setOnCloseIconClickListener { removeFilter(key) }
            }
            binding.flexboxFilters.addView(chip)
        }
    }

    private fun removeFilter(key: String) {
        when (key) {
            "City"         -> binding.spinnerCity.setText("Any City", false)
            "Brand"        -> binding.spinnerBrand.setText("Any Brand", false)
            "Model"        -> binding.inputModel.text?.clear()
            "Transmission" -> binding.spinnerTransmission.setText("Any", false)
            "Fuel"         -> binding.spinnerFuelType.setText("Any", false)
            "Condition"    -> binding.chipGroupCondition.check(R.id.chip_condition_all)
            "Price"        -> binding.sliderPrice.setValues(priceMin, priceMax)
            "Year"         -> binding.sliderYear.setValues(yearMin, yearMax)
            "Mileage"      -> binding.sliderMileage.setValues(mileageMin, mileageMax)
        }
        updateAllLabels()
        updateActiveFilterChips()
    }

    // ── Reset ────────────────────────────────────────────────────────────────

    private fun resetAllFilters() {
        binding.spinnerCity.setText("Any City", false)
        binding.spinnerBrand.setText("Any Brand", false)
        binding.inputModel.text?.clear()
        binding.spinnerTransmission.setText("Any", false)
        binding.spinnerFuelType.setText("Any", false)
        binding.chipGroupCondition.check(R.id.chip_condition_all)
        binding.sliderPrice.setValues(priceMin, priceMax)
        binding.sliderYear.setValues(yearMin, yearMax)
        binding.sliderMileage.setValues(mileageMin, mileageMax)
        binding.inputPreferenceName.text?.clear()
        updateAllLabels()
        updateActiveFilterChips()

        // Clear results
        hasSearched = false
        currentResultIds = emptyList()
        renderResults(emptyList())
        Toast.makeText(requireContext(), "Filters reset", Toast.LENGTH_SHORT).show()
    }

    // ── Save preference ──────────────────────────────────────────────────────

    private fun saveSearchPreference() {
        val name = binding.inputPreferenceName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilPreferenceName.error = "Please enter a name for this search"
            binding.tilPreferenceName.requestFocus()
            return
        }
        binding.tilPreferenceName.error = null

        val city         = binding.spinnerCity.text.toString().trim().takeIf { it != "Any City" } ?: ""
        val brand        = binding.spinnerBrand.text.toString().trim().takeIf { it != "Any Brand" } ?: ""
        val model        = binding.inputModel.text.toString().trim()
        val transmission = binding.spinnerTransmission.text.toString().trim().takeIf { it != "Any" } ?: ""
        val fuelType     = binding.spinnerFuelType.text.toString().trim().takeIf { it != "Any" } ?: ""
        val condition    = getSelectedCondition() ?: ""

        val prices    = binding.sliderPrice.values
        val mileages  = binding.sliderMileage.values

        val preference = SearchPreference(
            name        = name,
            brand       = brand,
            model       = model,
            city        = city,
            minPrice    = prices[0].toLong(),
            maxPrice    = prices[1].toLong(),
            minMileage  = mileages[0].toInt(),
            maxMileage  = mileages[1].toInt(),
            transmission = transmission,
            fuelType    = fuelType,
            condition   = condition
        )

        VehicleRepository.saveSearchPreference(preference)
        binding.inputPreferenceName.text?.clear()

        Toast.makeText(requireContext(), "\"$name\" saved to Saved Searches", Toast.LENGTH_SHORT).show()
    }

    // ── Pre-fill from saved preference ──────────────────────────────────────

    private fun prefillFromPreference(pref: SearchPreference) {
        if (pref.city.isNotEmpty())         binding.spinnerCity.setText(pref.city, false)
        if (pref.brand.isNotEmpty())        binding.spinnerBrand.setText(pref.brand, false)
        if (pref.model.isNotEmpty())        binding.inputModel.setText(pref.model)
        if (pref.transmission.isNotEmpty()) binding.spinnerTransmission.setText(pref.transmission, false)
        if (pref.fuelType.isNotEmpty())     binding.spinnerFuelType.setText(pref.fuelType, false)

        when (pref.condition) {
            "New"       -> binding.chipGroupCondition.check(R.id.chip_condition_new)
            "Used"      -> binding.chipGroupCondition.check(R.id.chip_condition_used)
            "Certified" -> binding.chipGroupCondition.check(R.id.chip_condition_certified)
        }

        // Only set sliders if values differ from defaults
        if (pref.minPrice > 0 || pref.maxPrice < 10_000_000L) {
            val safeMin = pref.minPrice.toFloat().coerceIn(priceMin, priceMax)
            val safeMax = pref.maxPrice.toFloat().coerceIn(priceMin, priceMax)
            if (safeMin < safeMax) binding.sliderPrice.setValues(safeMin, safeMax)
        }
        if (pref.minMileage > 0 || pref.maxMileage < 500_000) {
            val safeMin = pref.minMileage.toFloat().coerceIn(mileageMin, mileageMax)
            val safeMax = pref.maxMileage.toFloat().coerceIn(mileageMin, mileageMax)
            if (safeMin < safeMax) binding.sliderMileage.setValues(safeMin, safeMax)
        }

        updateAllLabels()
        updateActiveFilterChips()
        // Auto-run search with pre-filled values
        performSearch()
    }

    // ── ToolbarActionHandler ─────────────────────────────────────────────────

    override fun onToolbarSearch() {}
    override fun onSearchQueryChanged(query: String) {
        binding.inputModel.setText(query)
        performSearch()
    }
}
