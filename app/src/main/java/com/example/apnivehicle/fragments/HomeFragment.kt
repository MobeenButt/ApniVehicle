package com.example.apnivehicle.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apnivehicle.activities.DetailActivity
import com.example.apnivehicle.adapters.VehicleAdapter
import com.example.apnivehicle.databinding.FragmentHomeBinding
import com.example.apnivehicle.dialogs.VehicleDialogs
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.NotificationHelper
import com.example.apnivehicle.utils.ToolbarActionHandler

class HomeFragment : Fragment(), ToolbarActionHandler {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VehicleAdapter
    private var isGridLayout = false
    private var sortOption = VehicleRepository.SortOption.LATEST
    private var filterValues: VehicleDialogs.FilterValues? = null

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
                    if (it.isFavorite) NotificationHelper(requireContext()).showFavoriteAdded(it.title)
                }
                loadVehicles()
            }
        )
        binding.recyclerVehicles.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerVehicles.adapter = adapter

        loadVehicles()
    }

    override fun onResume() {
        super.onResume()
        loadVehicles()
    }

    private fun loadVehicles() {
        var vehicles = VehicleRepository.getVehicles()
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
        // Home screen does not have inline search; user can switch to Search from drawer.
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
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
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

