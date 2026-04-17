package com.example.apnivehicle.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.apnivehicle.databinding.ActivitySplashBinding
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.PreferenceManager
import com.example.apnivehicle.utils.ThemeManager

class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            // Apply theme before super.onCreate
            ThemeManager.applyTheme(this)
            
            super.onCreate(savedInstanceState)
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize repositories and file manager
            try {
                AuthRepository.init(this)
                VehicleRepository.init(this)
                preferenceManager = PreferenceManager(this)
            } catch (e: Exception) {
                android.util.Log.e("SplashActivity", "Error initializing repositories", e)
                // Continue anyway with default values
                preferenceManager = PreferenceManager(this)
            }
            
            // Animate car as loading indicator
            animateCarLoading()

            Handler(Looper.getMainLooper()).postDelayed({
                navigateToNextScreen()
            }, 3000)
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Fatal error in onCreate", e)
            // Try to navigate to login as fallback
            try {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } catch (ex: Exception) {
                android.util.Log.e("SplashActivity", "Cannot recover from crash", ex)
            }
        }
    }
    
    private fun animateCarLoading() {
        // Make everything visible immediately
        binding.tvAppName.alpha = 1f
        binding.tvTagline.alpha = 1f
        binding.tvVersion.alpha = 1f
        
        // Calculate the distance the car needs to travel
        binding.loadingContainer.post {
            val containerWidth = binding.loadingContainer.width.toFloat()
            val carWidth = binding.ivLogo.width.toFloat()
            val travelDistance = containerWidth - carWidth - 64 // 64 for margins
            
            // Start car at the left edge
            binding.ivLogo.translationX = 0f
            
            // Animate car moving from left to right smoothly (representing loading progress)
            ObjectAnimator.ofFloat(binding.ivLogo, View.TRANSLATION_X, 0f, travelDistance).apply {
                duration = 3000 // Match the splash duration
                interpolator = LinearInterpolator() // Smooth constant speed
                start()
            }
        }
    }
    
    private fun navigateToNextScreen() {
        val nextActivity = when {
            preferenceManager.isFirstLaunch -> OnboardingActivity::class.java
            AuthRepository.isUserLoggedIn() -> MainActivity::class.java
            else -> LoginActivity::class.java
        }
        startActivity(Intent(this, nextActivity))
        finish()
    }
}
