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
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var preferenceManager: PreferenceManager
    
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
        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < onboardingItems.size - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                finishOnboarding()
            }
        }
        
        binding.btnSkip.setOnClickListener {
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
        preferenceManager.isFirstLaunch = false
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
