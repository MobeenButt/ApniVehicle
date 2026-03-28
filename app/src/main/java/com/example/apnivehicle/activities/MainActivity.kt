package com.example.apnivehicle.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ActivityHomeBinding
import com.example.apnivehicle.fragments.AddVehicleFragment
import com.example.apnivehicle.fragments.FavoriteFragment
import com.example.apnivehicle.fragments.HomeFragment
import com.example.apnivehicle.fragments.MyAdsFragment
import com.example.apnivehicle.fragments.SearchFragment
import com.example.apnivehicle.fragments.SettingsFragment
import com.example.apnivehicle.utils.ToolbarActionHandler
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    private lateinit var binding: ActivityHomeBinding
    private val destinations: Map<Int, Pair<() -> Fragment, Int>> = mapOf(
        R.id.nav_home to ( { HomeFragment() } to R.string.nav_home ),
        R.id.nav_used_cars to ( { SearchFragment() } to R.string.nav_used_cars ),
        R.id.nav_new_cars to ( { AddVehicleFragment() } to R.string.nav_new_cars ),
        R.id.nav_bikes to ( { MyAdsFragment() } to R.string.nav_bikes ),
        R.id.nav_more to ( { SettingsFragment() } to R.string.nav_more )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermissionIfNeeded()
        setSupportActionBar(binding.toolbarHome)

        binding.bottomNavigation.setOnItemSelectedListener(this)
        
        binding.fabPostAd.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.nav_new_cars
        }

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            openFragment(HomeFragment(), getString(R.string.nav_home))
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val toolbarHandler = activeFragment as? ToolbarActionHandler

        when (item.itemId) {
            R.id.action_search -> toolbarHandler?.onToolbarSearch()
            R.id.action_notifications -> showNotifications()
            R.id.action_sort -> toolbarHandler?.onToolbarSort()
            R.id.action_toggle_layout -> toolbarHandler?.onToolbarToggleLayout()
            R.id.action_filter -> toolbarHandler?.onToolbarFilter()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showNotifications() {
        // TODO: Implement notifications
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
}
