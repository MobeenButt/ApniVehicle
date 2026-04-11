package com.example.apnivehicle.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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
        // Apply theme before super.onCreate
        ThemeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repositories and file manager
        AuthRepository.init(this)
        VehicleRepository.init(this)
        preferenceManager = PreferenceManager(this)
        
        // Animate logo
        animateLogo()

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2000)
    }
    
    private fun animateLogo() {
        binding.ivLogo.alpha = 0f
        binding.tvTagline.alpha = 0f
        
        ObjectAnimator.ofFloat(binding.ivLogo, View.ALPHA, 0f, 1f).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        ObjectAnimator.ofFloat(binding.tvTagline, View.ALPHA, 0f, 1f).apply {
            duration = 1000
            startDelay = 500
            interpolator = AccelerateDecelerateInterpolator()
            start()
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
