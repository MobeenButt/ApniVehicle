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
import com.example.apnivehicle.databinding.FragmentFavoriteBinding
import com.example.apnivehicle.repository.VehicleRepository

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VehicleAdapter

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
        val items = VehicleRepository.getFavorites()
        adapter.submitList(items)
        binding.textEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

