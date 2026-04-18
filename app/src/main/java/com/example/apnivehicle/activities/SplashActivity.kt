package com.example.apnivehicle.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.example.apnivehicle.databinding.ActivitySplashBinding
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.PreferenceManager

class SplashActivity : AppCompatActivity() {
    
    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!
    
    private var preferenceManager: PreferenceManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private val splashDuration = 3000L // 3 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            _binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            Log.d("SplashActivity", "Splash screen started")
            
            // Initialize repositories in background
            initializeApp()
            
            // Start animations
            startAnimations()
            
            // Navigate after splash duration
            handler.postDelayed({
                navigateToNextScreen()
            }, splashDuration)
            
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error in onCreate", e)
            // Fallback: navigate immediately
            handler.postDelayed({ navigateToLogin() }, 1000)
        }
    }
    
    private fun initializeApp() {
        Thread {
            try {
                // Initialize repositories
                AuthRepository.init(this)
                VehicleRepository.init(this)
                preferenceManager = PreferenceManager(this)
                
                Log.d("SplashActivity", "App initialized successfully")
            } catch (e: Exception) {
                Log.e("SplashActivity", "Error initializing app", e)
                runOnUiThread {
                    try {
                        preferenceManager = PreferenceManager(this)
                    } catch (ex: Exception) {
                        Log.e("SplashActivity", "Failed to create PreferenceManager", ex)
                    }
                }
            }
        }.start()
    }
    
    private fun startAnimations() {
        try {
            // 1. Animate logo card - scale in with bounce
            animateLogoCard()
            
            // 2. Animate app name - fade in and slide up
            animateAppName()
            
            // 3. Animate tagline - fade in and slide up
            animateTagline()
            
            // 4. Animate progress bar
            animateProgressBar()
            
            // 5. Animate loading text
            animateLoadingText()
            
            // 6. Animate version text
            animateVersionText()
            
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error in animations", e)
        }
    }
    
    private fun animateLogoCard() {
        try {
            // Start invisible and small
            binding.logoCard.alpha = 0f
            binding.logoCard.scaleX = 0.3f
            binding.logoCard.scaleY = 0.3f
            
            // Animate to visible and full size with bounce
            binding.logoCard.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setStartDelay(200)
                .setInterpolator(OvershootInterpolator(1.5f))
                .start()
                
            // Subtle pulse animation
            handler.postDelayed({
                if (!isFinishing) {
                    createPulseAnimation()
                }
            }, 1200)
            
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error animating logo", e)
        }
    }
    
    private fun createPulseAnimation() {
        try {
            val scaleX = ObjectAnimator.ofFloat(binding.logoCard, View.SCALE_X, 1f, 1.05f, 1f)
            val scaleY = ObjectAnimator.ofFloat(binding.logoCard, View.SCALE_Y, 1f, 1.05f, 1f)
            
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleX, scaleY)
            animatorSet.duration = 1000
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.start()
            
            animatorSet.doOnEnd {
                if (!isFinishing) {
                    handler.postDelayed({ createPulseAnimation() }, 500)
                }
            }
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error in pulse animation", e)
        }
    }
    
    private fun animateAppName() {
        try {
            binding.tvAppName.alpha = 0f
            binding.tvAppName.translationY = 50f
            
            binding.tvAppName.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error animating app name", e)
        }
    }
    
    private fun animateTagline() {
        try {
            binding.tvTagline.alpha = 0f
            binding.tvTagline.translationY = 30f
            
            binding.tvTagline.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(800)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error animating tagline", e)
        }
    }
    
    private fun animateProgressBar() {
        try {
            binding.loadingContainer.alpha = 0f
            
            // Fade in the container
            binding.loadingContainer.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(1000)
                .start()
            
            // Animate progress from 0 to 100
            handler.postDelayed({
                if (!isFinishing) {
                    val progressAnimator = ValueAnimator.ofInt(0, 100)
                    progressAnimator.duration = splashDuration - 1200
                    progressAnimator.addUpdateListener { animator ->
                        try {
                            binding.progressBar.progress = animator.animatedValue as Int
                        } catch (e: Exception) {
                            Log.e("SplashActivity", "Error updating progress", e)
                        }
                    }
                    progressAnimator.start()
                }
            }, 1200)
            
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error animating progress bar", e)
        }
    }
    
    private fun animateLoadingText() {
        try {
            val loadingTexts = listOf(
                "Initializing...",
                "Loading vehicles...",
                "Preparing app...",
                "Almost ready..."
            )
            
            var currentIndex = 0
            val textChangeInterval = 700L
            
            val textRunnable = object : Runnable {
                override fun run() {
                    if (!isFinishing && currentIndex < loadingTexts.size) {
                        try {
                            binding.tvLoading.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .withEndAction {
                                    try {
                                        binding.tvLoading.text = loadingTexts[currentIndex]
                                        binding.tvLoading.animate()
                                            .alpha(1f)
                                            .setDuration(200)
                                            .start()
                                    } catch (e: Exception) {
                                        Log.e("SplashActivity", "Error changing text", e)
                                    }
                                }
                                .start()
                            
                            currentIndex++
                            handler.postDelayed(this, textChangeInterval)
                        } catch (e: Exception) {
                            Log.e("SplashActivity", "Error in text animation", e)
                        }
                    }
                }
            }
            
            handler.postDelayed(textRunnable, 1200)
            
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error animating loading text", e)
        }
    }
    
    private fun animateVersionText() {
        try {
            binding.tvVersion.alpha = 0f
            
            binding.tvVersion.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(1400)
                .start()
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error animating version", e)
        }
    }
    
    private fun navigateToNextScreen() {
        try {
            // Fade out animation
            binding.root.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    try {
                        val nextActivity = when {
                            preferenceManager?.isFirstLaunch == true -> OnboardingActivity::class.java
                            AuthRepository.isUserLoggedIn() -> MainActivity::class.java
                            else -> LoginActivity::class.java
                        }
                        
                        Log.d("SplashActivity", "Navigating to: ${nextActivity.simpleName}")
                        
                        startActivity(Intent(this, nextActivity))
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    } catch (e: Exception) {
                        Log.e("SplashActivity", "Error navigating", e)
                        navigateToLogin()
                    }
                }
                .start()
                
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error in navigation", e)
            navigateToLogin()
        }
    }
    
    private fun navigateToLogin() {
        try {
            Log.d("SplashActivity", "Fallback: Navigating to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } catch (e: Exception) {
            Log.e("SplashActivity", "Cannot navigate to login", e)
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            handler.removeCallbacksAndMessages(null)
            _binding = null
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error in onDestroy", e)
        }
    }
}
