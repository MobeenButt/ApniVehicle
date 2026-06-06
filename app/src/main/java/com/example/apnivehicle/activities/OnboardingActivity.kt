package com.example.apnivehicle.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.apnivehicle.R
import com.example.apnivehicle.adapters.OnboardingAdapter
import com.example.apnivehicle.databinding.ActivityOnboardingBinding
import com.example.apnivehicle.models.OnboardingItem
import com.example.apnivehicle.utils.PreferenceManager
import com.example.apnivehicle.utils.setDebouncedClickListener
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var preferenceManager: PreferenceManager
    private var isFinishing = false  // Prevent multiple finishOnboarding() calls

    private val onboardingItems = listOf(
        OnboardingItem(
            title = "Find Your Dream Vehicle",
            description = "Browse thousands of cars, bikes, and more from verified sellers across Pakistan",
            imageRes = R.drawable.ic_directions_car
        ),
        OnboardingItem(
            title = "Compare & Choose",
            description = "Compare prices, features, and specifications to make the best decision",
            imageRes = R.drawable.ic_car_rental
        ),
        OnboardingItem(
            title = "Buy & Sell Easily",
            description = "Post your ad in minutes and connect with genuine buyers instantly",
            imageRes = R.drawable.ic_two_wheeler
        )
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferenceManager = PreferenceManager(this)
        
        setupViewPager()
        setupButtons()
    }
    
    private fun setupViewPager() {
        val adapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtons(position)
            }
        })
    }
    
    private fun setupButtons() {
        binding.btnNext.setDebouncedClickListener(1000L) {
            if (isFinishing) return@setDebouncedClickListener
            val currentItem = binding.viewPager.currentItem
            if (currentItem < onboardingItems.size - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                finishOnboarding()
            }
        }
        
        binding.btnSkip.setDebouncedClickListener(1000L) {
            if (isFinishing) return@setDebouncedClickListener
            finishOnboarding()
        }
    }
    
    private fun updateButtons(position: Int) {
        if (position == onboardingItems.size - 1) {
            binding.btnNext.text = getString(R.string.get_started)
            binding.btnSkip.visibility = View.GONE
        } else {
            binding.btnNext.text = getString(R.string.next)
            binding.btnSkip.visibility = View.VISIBLE
        }
    }
    
    private fun finishOnboarding() {
        if (isFinishing) return  // Prevent multiple calls
        isFinishing = true

        preferenceManager.isFirstLaunch = false
        // Clear the task so pressing back from LoginActivity doesn't return to onboarding
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
