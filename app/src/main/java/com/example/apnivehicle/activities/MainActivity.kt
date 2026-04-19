package com.example.apnivehicle.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ActivityHomeBinding
import com.example.apnivehicle.databinding.CustomToolbarBinding
import com.example.apnivehicle.fragments.*
import com.example.apnivehicle.utils.AppNotificationManager
import com.example.apnivehicle.utils.ToolbarActionHandler
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.search.SearchView

class MainActivity : AppCompatActivity(), 
    NavigationBarView.OnItemSelectedListener, 
    AppNotificationManager.NotificationCountListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var toolbarBinding: CustomToolbarBinding
    private var priceDropReceiver: com.example.apnivehicle.receivers.PriceDropBroadcastReceiver? = null
    
    private val destinations: Map<Int, Pair<() -> Fragment, String>> = mapOf(
        R.id.nav_home to ({ HomeFragment() } to "Home"),
        R.id.nav_favorites to ({ FavoriteFragment() } to "Favorites"),
        R.id.nav_new_cars to ({ AddVehicleFragment() } to "Add Vehicle"),
        R.id.nav_bikes to ({ MyAdsFragment() } to "My Ads"),
        R.id.nav_more to ({ SettingsFragment() } to "Settings")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = ActivityHomeBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Bind custom toolbar - get the root view of the included layout
            toolbarBinding = CustomToolbarBinding.bind(binding.root.findViewById(R.id.toolbar_home))

            setupToolbar()
            requestNotificationPermissionIfNeeded()
            
            // Register notification listener
            try {
                AppNotificationManager.addListener(this)
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error registering notification listener", e)
            }
            
            // Register Price Drop Broadcast Receiver
            try {
                priceDropReceiver = com.example.apnivehicle.receivers.PriceDropBroadcastReceiver.register(this)
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error registering price drop receiver", e)
            }

            // Setup bottom navigation
            binding.bottomNavigation.setOnItemSelectedListener(this)
            
            // Setup FAB
            binding.fabPostAd.setOnClickListener {
                binding.bottomNavigation.selectedItemId = R.id.nav_new_cars
            }

            // Load initial fragment
            if (savedInstanceState == null) {
                binding.bottomNavigation.selectedItemId = R.id.nav_home
                openFragment(HomeFragment(), "Home")
            }
            
            // Update notification badge
            updateNotificationBadge(AppNotificationManager.getNotificationCount(this))
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate", e)
            android.widget.Toast.makeText(this, "Error loading app", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupToolbar() {
        try {
            // Search button click
            toolbarBinding.actionSearch.setOnClickListener {
                openFragment(SearchFragment(), "Search")
            }
            
            // Notifications button click
            toolbarBinding.actionNotifications.setOnClickListener {
                showNotifications()
            }
            
            // More options button click
            toolbarBinding.actionMore.setOnClickListener { view ->
                showMoreMenu(view)
            }
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up toolbar", e)
        }
    }
    
    private fun showMoreMenu(anchor: View) {
        try {
            val popup = PopupMenu(this, anchor)
            popup.menuInflater.inflate(R.menu.menu_toolbar_more, popup.menu)
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_advanced_search -> {
                        openFragment(AdvancedSearchFragment(), "Advanced Search")
                        true
                    }
                    R.id.action_analytics -> {
                        openFragment(AnalyticsFragment(), "Analytics")
                        true
                    }
                    R.id.action_favorites -> {
                        openFragment(FavoriteFragment(), "Favorites")
                        true
                    }
                    R.id.action_user_profile -> {
                        openFragment(UserProfileFragment(), "Profile")
                        true
                    }
                    R.id.action_comparison -> {
                        openFragment(ComparisonFragment(), "Compare")
                        true
                    }
                    R.id.action_filter -> {
                        val activeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                        (activeFragment as? ToolbarActionHandler)?.onToolbarFilter()
                        true
                    }
                    R.id.action_sort -> {
                        val activeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                        (activeFragment as? ToolbarActionHandler)?.onToolbarSort()
                        true
                    }
                    R.id.action_toggle_layout -> {
                        val activeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                        (activeFragment as? ToolbarActionHandler)?.onToolbarToggleLayout()
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error showing menu", e)
        }
    }
    
    private fun showNotifications() {
        try {
            // Clear notification count
            AppNotificationManager.clearNotificationCount(this)
            
            // TODO: Show notifications list/dialog
            android.widget.Toast.makeText(this, "Notifications cleared", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error showing notifications", e)
        }
    }
    
    private fun updateNotificationBadge(count: Int) {
        try {
            toolbarBinding.notificationBadge.apply {
                if (count > 0) {
                    visibility = View.VISIBLE
                    text = if (count > 99) "99+" else count.toString()
                } else {
                    visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error updating badge", e)
        }
    }
    
    override fun onNotificationCountChanged(count: Int) {
        runOnUiThread {
            updateNotificationBadge(count)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val destination = destinations[item.itemId] ?: return false
        openFragment(destination.first.invoke(), destination.second)
        return true
    }

    private fun openFragment(fragment: Fragment, title: String) {
        try {
            // Update toolbar title
            toolbarBinding.toolbarTitle.text = title
            
            // Replace fragment
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error opening fragment", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            // Unregister notification listener
            AppNotificationManager.removeListener(this)
            // Unregister Price Drop BroadcastReceiver
            com.example.apnivehicle.receivers.PriceDropBroadcastReceiver.unregister(this, priceDropReceiver)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onDestroy", e)
        }
    }
}
