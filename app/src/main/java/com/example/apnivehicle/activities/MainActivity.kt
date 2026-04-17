package com.example.apnivehicle.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ActivityHomeBinding
import com.example.apnivehicle.fragments.AddVehicleFragment
import com.example.apnivehicle.fragments.AdvancedSearchFragment
import com.example.apnivehicle.fragments.AnalyticsFragment
import com.example.apnivehicle.fragments.ComparisonFragment
import com.example.apnivehicle.fragments.FavoriteFragment
import com.example.apnivehicle.fragments.HomeFragment
import com.example.apnivehicle.fragments.MyAdsFragment
import com.example.apnivehicle.fragments.SearchFragment
import com.example.apnivehicle.fragments.SettingsFragment
import com.example.apnivehicle.fragments.UserProfileFragment
import com.example.apnivehicle.receivers.SystemBroadcastReceiver
import com.example.apnivehicle.utils.AppNotificationManager
import com.example.apnivehicle.utils.ToolbarActionHandler
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener, AppNotificationManager.NotificationCountListener {

    private lateinit var binding: ActivityHomeBinding
    private var systemReceiver: SystemBroadcastReceiver? = null
    private var notificationBadge: TextView? = null
    
    private val destinations: Map<Int, Pair<() -> Fragment, Int>> = mapOf(
        R.id.nav_home to ( { HomeFragment() } to R.string.nav_home ),
        R.id.nav_favorites to ( { FavoriteFragment() } to R.string.nav_favorites ),
        R.id.nav_new_cars to ( { AddVehicleFragment() } to R.string.nav_new_cars ),
        R.id.nav_bikes to ( { MyAdsFragment() } to R.string.nav_bikes ),
        R.id.nav_more to ( { SettingsFragment() } to R.string.nav_more )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = ActivityHomeBinding.inflate(layoutInflater)
            setContentView(binding.root)

            requestNotificationPermissionIfNeeded()
            setSupportActionBar(binding.toolbarHome)

            // Register notification listener
            AppNotificationManager.addListener(this)

            // Temporarily disable SystemBroadcastReceiver to prevent crashes
            // systemReceiver = SystemBroadcastReceiver.register(this)

            binding.bottomNavigation.setOnItemSelectedListener(this)
            
            binding.fabPostAd.setOnClickListener {
                binding.bottomNavigation.selectedItemId = R.id.nav_new_cars
            }

            if (savedInstanceState == null) {
                binding.bottomNavigation.selectedItemId = R.id.nav_home
                openFragment(HomeFragment(), getString(R.string.nav_home))
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate", e)
            android.widget.Toast.makeText(this, "Error loading app", android.widget.Toast.LENGTH_SHORT).show()
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

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView
        
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                val activeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                val toolbarHandler = activeFragment as? ToolbarActionHandler
                toolbarHandler?.onSearchQueryChanged(newText.orEmpty())
                return true
            }
        })
        
        // Setup notification badge
        val notificationItem = menu.findItem(R.id.action_notifications)
        setupNotificationBadge(notificationItem)
        
        return true
    }
    
    private fun setupNotificationBadge(menuItem: MenuItem) {
        val actionView = menuItem.actionView ?: run {
            // Create action view if it doesn't exist
            val view = layoutInflater.inflate(R.layout.notification_badge_layout, null)
            menuItem.actionView = view
            view
        }
        
        notificationBadge = actionView.findViewById(R.id.notification_badge)
        val iconView = actionView.findViewById<View>(R.id.notification_icon)
        
        // Set click listener on the action view
        actionView.setOnClickListener {
            onOptionsItemSelected(menuItem)
        }
        
        // Update badge with current count
        updateNotificationBadge(AppNotificationManager.getNotificationCount(this))
    }
    
    private fun updateNotificationBadge(count: Int) {
        notificationBadge?.let { badge ->
            if (count > 0) {
                badge.visibility = View.VISIBLE
                badge.text = if (count > 99) "99+" else count.toString()
            } else {
                badge.visibility = View.GONE
            }
        }
    }
    
    override fun onNotificationCountChanged(count: Int) {
        runOnUiThread {
            updateNotificationBadge(count)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val toolbarHandler = activeFragment as? ToolbarActionHandler

        when (item.itemId) {
            R.id.action_notifications -> showNotifications()
            R.id.action_sort -> toolbarHandler?.onToolbarSort()
            R.id.action_toggle_layout -> toolbarHandler?.onToolbarToggleLayout()
            R.id.action_filter -> toolbarHandler?.onToolbarFilter()
            R.id.action_advanced_search -> openFragment(AdvancedSearchFragment(), "Advanced Search")
            R.id.action_analytics -> openFragment(AnalyticsFragment(), "Analytics Dashboard")
            R.id.action_favorites -> openFragment(FavoriteFragment(), "Favorite Vehicles")
            R.id.action_user_profile -> openFragment(UserProfileFragment(), "My Profile")
            R.id.action_comparison -> openFragment(ComparisonFragment(), "Compare Vehicles")
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showNotifications() {
        // Clear notification count when user views notifications
        AppNotificationManager.clearNotificationCount(this)
        
        // TODO: Show notifications list/dialog
        android.widget.Toast.makeText(this, "Notifications cleared", android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val destination = destinations[item.itemId] ?: return false
        openFragment(destination.first.invoke(), getString(destination.second))
        return true
    }

    private fun openFragment(fragment: Fragment, title: String) {
        supportActionBar?.title = title
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Unregister notification listener
        AppNotificationManager.removeListener(this)
        // Unregister BroadcastReceiver to prevent memory leaks
        // SystemBroadcastReceiver.unregister(this, systemReceiver)
    }
}
