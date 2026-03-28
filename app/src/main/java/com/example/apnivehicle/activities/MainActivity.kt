package com.example.apnivehicle.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apnivehicle.R
import com.example.apnivehicle.adapters.VehicleAdapter
import com.example.apnivehicle.databinding.ActivityHomeBinding
import com.example.apnivehicle.repository.VehicleRepository
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val repository = VehicleRepository()
    private lateinit var featuredAdapter: VehicleAdapter
    private lateinit var recentAdapter: VehicleAdapter
    private var selectedCategory = "cars"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupAdapters()
        setupRecyclerViews()
        setupCategoryChips()
        setupBottomNavigation()
        setupFAB()
        loadVehicles()
    }

    private fun setupAdapters() {
        featuredAdapter = VehicleAdapter(
            onViewDetailsClick = { vehicle ->
                Toast.makeText(this, "View ${vehicle.title} details", Toast.LENGTH_SHORT).show()
            },
            onFavoriteClick = { vehicle ->
                Toast.makeText(this, "Added ${vehicle.title} to favorites", Toast.LENGTH_SHORT).show()
            }
        )

        recentAdapter = VehicleAdapter(
            onViewDetailsClick = { vehicle ->
                Toast.makeText(this, "View ${vehicle.title} details", Toast.LENGTH_SHORT).show()
            },
            onFavoriteClick = { vehicle ->
                Toast.makeText(this, "Added ${vehicle.title} to favorites", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupRecyclerViews() {
        binding.recyclerFeatured.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = featuredAdapter
        }

        binding.recyclerRecent.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = recentAdapter
        }
    }

    private fun setupCategoryChips() {
        // Set cars as default selected
        binding.chipCategoryCars.isChecked = true

        // Set up chip change listeners
        listOf(
            binding.chipCategoryCars to "cars",
            binding.chipCategoryBikes to "bikes",
            binding.chipCategoryTrucks to "trucks",
            binding.chipCategoryParts to "parts",
            binding.chipCategoryAccessories to "accessories"
        ).forEach { (chip, category) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategory = category
                    // Filter vehicles by category (in a real app)
                    // For now, just show the same data
                    Toast.makeText(this, "Category: $category", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_used_cars -> {
                    Toast.makeText(this, "Used Cars", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_new_cars -> {
                    Toast.makeText(this, "New Cars", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_bikes -> {
                    Toast.makeText(this, "Bikes", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_more -> {
                    Toast.makeText(this, "More", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFAB() {
        binding.fabPostAd.setOnClickListener {
            Toast.makeText(this, "Post New Ad", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadVehicles() {
        val featured = repository.getFeaturedVehicles()
        val recent = repository.getRecentlyAddedVehicles()
        
        featuredAdapter.updateVehicles(featured)
        recentAdapter.updateVehicles(recent)
    }
}