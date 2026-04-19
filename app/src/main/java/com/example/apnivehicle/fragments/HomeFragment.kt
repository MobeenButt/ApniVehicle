package com.example.apnivehicle.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apnivehicle.R
import com.example.apnivehicle.activities.DetailActivity
import com.example.apnivehicle.adapters.VehicleAdapter
import com.example.apnivehicle.databinding.FragmentHomeBinding
import com.example.apnivehicle.dialogs.VehicleDialogs
import com.example.apnivehicle.models.VehicleType
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.AnalyticsManager
import com.example.apnivehicle.utils.NotificationHelper
import com.example.apnivehicle.utils.ToolbarActionHandler

class HomeFragment : Fragment(), ToolbarActionHandler {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VehicleAdapter
    private var isGridLayout = false
    private var sortOption = VehicleRepository.SortOption.LATEST
    private var filterValues: VehicleDialogs.FilterValues? = null
    private var selectedCategory: VehicleType? = null
    private var searchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = VehicleAdapter(
            onItemClick = { vehicle ->
                startActivity(Intent(requireContext(), DetailActivity::class.java).putExtra(DetailActivity.EXTRA_VEHICLE_ID, vehicle.id))
            },
            onFavoriteClick = { vehicle ->
                VehicleRepository.toggleFavorite(vehicle.id)?.let {
                    // Track favorite analytics
                    AnalyticsManager.trackFavorite(requireContext(), vehicle.id, it.isFavorite)
                    
                    if (it.isFavorite) NotificationHelper(requireContext()).showFavoriteAdded(it.title)
                }
                loadVehicles()
            }
        )
        binding.recyclerVehicles.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerVehicles.adapter = adapter

        setupCategoryFilters()
        loadVehicles()
    }

    private fun setupCategoryFilters() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedCategory = when (checkedIds.firstOrNull()) {
                R.id.chip_cars -> VehicleType.CAR
                R.id.chip_bikes -> VehicleType.MOTORCYCLE
                R.id.chip_trucks -> VehicleType.TRUCK
                R.id.chip_buses -> VehicleType.BUS
                R.id.chip_vans -> VehicleType.VAN
                R.id.chip_jeeps -> VehicleType.JEEP
                R.id.chip_rickshaws -> VehicleType.AUTO_RICKSHAW
                R.id.chip_tractors -> VehicleType.TRACTOR
                else -> null // "All" or nothing selected
            }
            loadVehicles()
        }
    }

    override fun onResume() {
        super.onResume()
        loadVehicles()
    }

    private fun loadVehicles() {
        var vehicles = VehicleRepository.getVehicles()
        
        // Apply search filter
        if (searchQuery.isNotEmpty()) {
            vehicles = VehicleRepository.searchVehicles(searchQuery, vehicles)
        }

        // Apply Category filter from Chips
        if (selectedCategory != null) {
            vehicles = vehicles.filter { it.type == selectedCategory }
        }
        
        // Apply other filters from Dialog
        filterValues?.let { values ->
            vehicles = VehicleRepository.filterVehicles(
                city = values.city,
                minPrice = values.minPrice,
                maxPrice = values.maxPrice,
                brand = values.brand,
                year = values.year,
                source = vehicles
            )
        }
        vehicles = VehicleRepository.sortVehicles(sortOption, vehicles)
        adapter.submitList(vehicles)
        binding.textEmpty.visibility = if (vehicles.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onToolbarSearch() {
        // No longer needed - search is now in toolbar
    }

    override fun onSearchQueryChanged(query: String) {
        searchQuery = query
        loadVehicles()
    }

    override fun onToolbarSort() {
        VehicleDialogs.showSortDialog(requireContext()) {
            sortOption = it
            loadVehicles()
        }
    }

    override fun onToolbarToggleLayout() {
        isGridLayout = !isGridLayout
        binding.recyclerVehicles.layoutManager = if (isGridLayout) {
            GridLayoutManager(requireContext(), 2).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int = 1
                }
            }
        } else {
            LinearLayoutManager(requireContext())
        }
        // Refresh adapter to apply layout changes
        adapter.notifyDataSetChanged()
    }

    override fun onToolbarFilter() {
        VehicleDialogs.showFilterDialog(requireContext()) {
            filterValues = it
            loadVehicles()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
