package com.example.apnivehicle.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apnivehicle.activities.DetailActivity
import com.example.apnivehicle.adapters.VehicleAdapter
import com.example.apnivehicle.databinding.FragmentMyAdsBinding
import com.example.apnivehicle.dialogs.VehicleDialogs
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.NetworkMonitor
import com.example.apnivehicle.utils.NotificationHelper

class MyAdsFragment : Fragment() {

    private var _binding: FragmentMyAdsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VehicleAdapter
    private var viewCreatedOnce = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAdsBinding.inflate(inflater, container, false)
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
            },
            onEditClick = { vehicle ->
                if (!NetworkMonitor.isCurrentlyOnline()) {
                    Toast.makeText(requireContext(), "You are offline. Cannot edit.", Toast.LENGTH_SHORT).show()
                    return@VehicleAdapter
                }
                VehicleDialogs.showEditVehicleDialog(requireContext(), vehicle) { updated ->
                    VehicleRepository.updateVehicle(updated, requireContext())
                    loadVehicles()
                }
            },
            onDeleteClick = { vehicle ->
                if (!NetworkMonitor.isCurrentlyOnline()) {
                    Toast.makeText(requireContext(), "You are offline. Cannot delete.", Toast.LENGTH_SHORT).show()
                    return@VehicleAdapter
                }
                VehicleDialogs.showDeleteConfirmDialog(requireContext(), vehicle) {
                    VehicleRepository.deleteVehicle(vehicle.id)
                    NotificationHelper(requireContext()).showVehicleDeleted(vehicle.title)
                    loadVehicles()
                }
            },
            showOwnerActions = true
        )

        binding.recyclerMyAds.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMyAds.adapter = adapter
        viewCreatedOnce = true

        // "Post first ad" shortcut from empty state
        try {
            binding.btnPostFirstAd.setOnClickListener {
                // Navigate to AddVehicle tab via parent activity's bottom nav
                (activity as? com.example.apnivehicle.activities.MainActivity)?.let {
                    it.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                        com.example.apnivehicle.R.id.bottom_navigation
                    )?.selectedItemId = com.example.apnivehicle.R.id.nav_new_cars
                }
            }
        } catch (_: Exception) {}

        loadVehicles()
    }

    override fun onResume() {
        super.onResume()
        // Guard against the double-load: onResume fires right after onViewCreated on first open.
        // Only reload on subsequent resumes (returning from DetailActivity, etc.).
        if (viewCreatedOnce && _binding != null) loadVehicles()
    }

    private fun loadVehicles() {
        val items = VehicleRepository.getMyAds()
        adapter.submitList(items)

        // Update count label
        try {
            binding.tvAdsCount.text = when (items.size) {
                0 -> "No active listings"
                1 -> "1 active listing"
                else -> "${items.size} active listings"
            }
        } catch (_: Exception) {}

        // Toggle empty state
        val isEmpty = items.isEmpty()
        binding.textEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        try {
            binding.layoutEmptyMyAds.visibility = if (isEmpty) View.VISIBLE else View.GONE
        } catch (_: Exception) {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewCreatedOnce = false
        _binding = null
    }
}

