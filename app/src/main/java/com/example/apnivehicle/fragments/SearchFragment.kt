package com.example.apnivehicle.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apnivehicle.activities.DetailActivity
import com.example.apnivehicle.adapters.VehicleAdapter
import com.example.apnivehicle.databinding.FragmentSearchBinding
import com.example.apnivehicle.dialogs.VehicleDialogs
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.ToolbarActionHandler

class SearchFragment : Fragment(), ToolbarActionHandler {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VehicleAdapter
    private var query: String = ""
    private var sortOption = VehicleRepository.SortOption.LATEST
    private var filterValues: VehicleDialogs.FilterValues? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = VehicleAdapter(
            onItemClick = { vehicle ->
                startActivity(Intent(requireContext(), DetailActivity::class.java).putExtra(DetailActivity.EXTRA_VEHICLE_ID, vehicle.id))
            },
            onFavoriteClick = { vehicle ->
                VehicleRepository.toggleFavorite(vehicle.id)
                loadVehicles()
            }
        )

        binding.recyclerSearch.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSearch.adapter = adapter

        loadVehicles()
    }

    private fun loadVehicles() {
        var items = VehicleRepository.searchVehicles(query)
        filterValues?.let { values ->
            items = VehicleRepository.filterVehicles(
                city = values.city,
                minPrice = values.minPrice,
                maxPrice = values.maxPrice,
                brand = values.brand,
                year = values.year,
                source = items
            )
        }
        items = VehicleRepository.sortVehicles(sortOption, items)
        adapter.submitList(items)
        binding.textEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onToolbarSearch() {
        // No longer needed - search is now in toolbar
    }

    override fun onSearchQueryChanged(query: String) {
        this.query = query
        loadVehicles()
    }

    override fun onToolbarSort() {
        VehicleDialogs.showSortDialog(requireContext()) {
            sortOption = it
            loadVehicles()
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

