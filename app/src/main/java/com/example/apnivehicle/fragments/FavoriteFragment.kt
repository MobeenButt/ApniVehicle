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
import com.example.apnivehicle.databinding.FragmentFavoriteBinding
import com.example.apnivehicle.dialogs.VehicleDialogs
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.NotificationHelper
import com.example.apnivehicle.utils.ToolbarActionHandler

class FavoriteFragment : Fragment(), ToolbarActionHandler {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VehicleAdapter
    private var isGridLayout = false
    private var sortOption = VehicleRepository.SortOption.LATEST
    private var searchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
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
                NotificationHelper(requireContext()).showFavoriteAdded(vehicle.title)
                loadVehicles()
            }
        )
        binding.recyclerFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFavorites.adapter = adapter
        loadVehicles()
    }

    override fun onResume() {
        super.onResume()
        loadVehicles()
    }

    private fun loadVehicles() {
        var items = VehicleRepository.getFavorites()
        
        // Apply search filter
        if (searchQuery.isNotEmpty()) {
            items = VehicleRepository.searchVehicles(searchQuery, items)
        }
        
        // Apply sorting
        items = VehicleRepository.sortVehicles(sortOption, items)
        
        adapter.submitList(items)
        binding.textEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onToolbarSearch() {
        // Search handled by onSearchQueryChanged
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
        binding.recyclerFavorites.layoutManager = if (isGridLayout) {
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
        // Favorites don't need additional filtering
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

