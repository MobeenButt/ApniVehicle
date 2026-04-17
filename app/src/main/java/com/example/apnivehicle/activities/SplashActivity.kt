package com.example.apnivehicle.activities

import android.animation.*
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.example.apnivehicle.databinding.ActivitySplashBinding
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.PreferenceManager
import com.example.apnivehicle.utils.ThemeManager
import kotlin.random.Random

class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    private lateinit var preferenceManager: PreferenceManager
    private val particles = mutableListOf<View>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            // Apply theme before super.onCreate
            ThemeManager.applyTheme(this)
            
            super.onCreate(savedInstanceState)
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize repositories
            try {
                AuthRepository.init(this)
                VehicleRepository.init(this)
                preferenceManager = PreferenceManager(this)
            } catch (e: Exception) {
                android.util.Log.e("SplashActivity", "Error initializing repositories", e)
                preferenceManager = PreferenceManager(this)
            }
            
            // Collect all particle views
            particles.addAll(listOf(
                binding.particle1, binding.particle2, binding.particle3, binding.particle4,
                binding.particle5, binding.particle6, binding.particle7, binding.particle8
            ))
            
            // Start the magical animation sequence
            startMagicalAnimations()

            Handler(Looper.getMainLooper()).postDelayed({
                navigateToNextScreen()
            }, 3500)
        } catch (e: Exception) {
            android.util.Log.e("SplashActivity", "Fatal error in onCreate", e)
            try {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } catch (ex: Exception) {
                android.util.Log.e("SplashActivity", "Cannot recover from crash", ex)
            }
        }
    }
    
    private fun startMagicalAnimations() {
        // 1. Animate background gradient
        animateBackgroundGradient()
        
        // 2. Animate floating particles
        animateFloatingParticles()
        
        // 3. Animate rotating glow ring
        animateGlowRing()
        
        // 4. Animate pulsing heart with logo
        animatePulsingHeart()
        
        // 5. Animate text elements
        animateTextElements()
        
        // 6. Animate progress bar
        animateProgressBar()
        
        // 7. Animate bouncing car
        animateBouncingCar()
    }
    
    private fun animateBackgroundGradient() {
        val colorAnimation = ValueAnimator.ofArgb(
            Color.parseColor("#1E88E5"),
            Color.parseColor("#1565C0"),
            Color.parseColor("#0D47A1"),
            Color.parseColor("#1565C0"),
            Color.parseColor("#1E88E5")
        )
        colorAnimation.duration = 3500
        colorAnimation.addUpdateListener { animator ->
            binding.root.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }
    
    private fun animateFloatingParticles() {
        particles.forEachIndexed { index, particle ->
            // Random starting position
            val startX = Random.nextFloat() * resources.displayMetrics.widthPixels
            val startY = Random.nextFloat() * resources.displayMetrics.heightPixels
            
            particle.x = startX
            particle.y = startY
            
            // Delayed start for each particle
            Handler(Looper.getMainLooper()).postDelayed({
                // Fade in
                particle.animate()
                    .alpha(0.8f)
                    .setDuration(500)
                    .start()
                
                // Float animation
                val floatAnimator = createFloatingAnimation(particle, startX, startY)
                floatAnimator.start()
                
                // Twinkle effect
                createTwinkleAnimation(particle).start()
            }, (index * 150).toLong())
        }
    }
    
    private fun createFloatingAnimation(view: View, startX: Float, startY: Float): AnimatorSet {
        val displayMetrics = resources.displayMetrics
        val endX = Random.nextFloat() * displayMetrics.widthPixels
        val endY = Random.nextFloat() * displayMetrics.heightPixels
        
        val animX = ObjectAnimator.ofFloat(view, View.X, startX, endX)
        val animY = ObjectAnimator.ofFloat(view, View.Y, startY, endY)
        
        val set = AnimatorSet()
        set.playTogether(animX, animY)
        set.duration = 3000 + Random.nextLong(1000)
        set.interpolator = AccelerateDecelerateInterpolator()
        
        set.doOnEnd {
            // Loop the animation
            if (!isFinishing) {
                createFloatingAnimation(view, endX, endY).start()
            }
        }
        
        return set
    }
    
    private fun createTwinkleAnimation(view: View): ObjectAnimator {
        val twinkle = ObjectAnimator.ofFloat(view, View.ALPHA, 0.3f, 1f, 0.3f)
        twinkle.duration = 1500 + Random.nextLong(1000)
        twinkle.repeatCount = ValueAnimator.INFINITE
        twinkle.repeatMode = ValueAnimator.REVERSE
        return twinkle
    }
    
    private fun animateGlowRing() {
        // Fade in
        binding.glowRing.animate()
            .alpha(0.6f)
            .setDuration(800)
            .setStartDelay(200)
            .start()
        
        // Rotate continuously
        val rotation = ObjectAnimator.ofFloat(binding.glowRing, View.ROTATION, 0f, 360f)
        rotation.duration = 3000
        rotation.repeatCount = ValueAnimator.INFINITE
        rotation.interpolator = LinearInterpolator()
        rotation.start()
        
        // Pulse scale
        val scaleX = ObjectAnimator.ofFloat(binding.glowRing, View.SCALE_X, 1f, 1.1f, 1f)
        scaleX.duration = 2000
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleX.interpolator = AccelerateDecelerateInterpolator()
        
        val scaleY = ObjectAnimator.ofFloat(binding.glowRing, View.SCALE_Y, 1f, 1.1f, 1f)
        scaleY.duration = 2000
        scaleY.repeatCount = ValueAnimator.INFINITE
        scaleY.interpolator = AccelerateDecelerateInterpolator()
        
        val scaleSet = AnimatorSet()
        scaleSet.playTogether(scaleX, scaleY)
        scaleSet.start()
    }
    
    private fun animatePulsingHeart() {
        // Heart glow fade in and pulse
        binding.heartGlow.animate()
            .alpha(0.8f)
            .setDuration(600)
            .setStartDelay(400)
            .start()
        
        val glowPulse = ObjectAnimator.ofFloat(binding.heartGlow, View.ALPHA, 0.4f, 0.9f, 0.4f)
        glowPulse.duration = 1500
        glowPulse.repeatCount = ValueAnimator.INFINITE
        glowPulse.interpolator = AccelerateDecelerateInterpolator()
        glowPulse.startDelay = 400
        glowPulse.start()
        
        // Logo scale in with bounce
        binding.ivLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setStartDelay(500)
            .setInterpolator(OvershootInterpolator(2f))
            .start()
        
        // Heartbeat pulse
        Handler(Looper.getMainLooper()).postDelayed({
            createHeartbeatAnimation().start()
        }, 1300)
    }
    
    private fun createHeartbeatAnimation(): AnimatorSet {
        // First beat
        val beat1ScaleX = ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_X, 1f, 1.15f, 1f)
        val beat1ScaleY = ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_Y, 1f, 1.15f, 1f)
        val beat1 = AnimatorSet()
        beat1.playTogether(beat1ScaleX, beat1ScaleY)
        beat1.duration = 150
        
        // Second beat
        val beat2ScaleX = ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_X, 1f, 1.1f, 1f)
        val beat2ScaleY = ObjectAnimator.ofFloat(binding.ivLogo, View.SCALE_Y, 1f, 1.1f, 1f)
        val beat2 = AnimatorSet()
        beat2.playTogether(beat2ScaleX, beat2ScaleY)
        beat2.duration = 150
        
        // Combine beats
        val heartbeat = AnimatorSet()
        heartbeat.playSequentially(beat1, beat2)
        heartbeat.startDelay = 800
        heartbeat.interpolator = AccelerateDecelerateInterpolator()
        
        heartbeat.doOnEnd {
            if (!isFinishing) {
                Handler(Looper.getMainLooper()).postDelayed({
                    createHeartbeatAnimation().start()
                }, 800)
            }
        }
        
        return heartbeat
    }
    
    private fun animateTextElements() {
        // App name - slide up and fade in
        binding.tvAppName.translationY = 50f
        binding.tvAppName.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(800)
            .setInterpolator(DecelerateInterpolator())
            .start()
        
        // Tagline - slide up and fade in
        binding.tvTagline.translationY = 50f
        binding.tvTagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(1000)
            .setInterpolator(DecelerateInterpolator())
            .start()
        
        // Progress container - fade in
        binding.progressContainer.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(1200)
            .start()
        
        // Version - fade in
        binding.tvVersion.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(1400)
            .start()
    }
    
    private fun animateProgressBar() {
        Handler(Looper.getMainLooper()).postDelayed({
            val displayMetrics = resources.displayMetrics
            val containerWidth = displayMetrics.widthPixels - (48 * 2 * displayMetrics.density).toInt()
            
            val progressAnim = ValueAnimator.ofInt(0, containerWidth)
            progressAnim.duration = 2000
            progressAnim.interpolator = AccelerateDecelerateInterpolator()
            progressAnim.addUpdateListener { animator ->
                val params = binding.progressBar.layoutParams
                params.width = animator.animatedValue as Int
                binding.progressBar.layoutParams = params
            }
            progressAnim.start()
            
            // Animate loading text
            animateLoadingText()
        }, 1200)
    }
    
    private fun animateLoadingText() {
        val texts = listOf(
            "Loading your journey...",
            "Preparing vehicles...",
            "Almost ready...",
            "Welcome!"
        )
        
        var currentIndex = 0
        val handler = Handler(Looper.getMainLooper())
        
        val runnable = object : Runnable {
            override fun run() {
                if (currentIndex < texts.size && !isFinishing) {
                    binding.tvLoading.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction {
                            binding.tvLoading.text = texts[currentIndex]
                            binding.tvLoading.animate()
                                .alpha(1f)
                                .setDuration(200)
                                .start()
                        }
                        .start()
                    
                    currentIndex++
                    handler.postDelayed(this, 700)
                }
            }
        }
        
        handler.postDelayed(runnable, 500)
    }
    
    private fun animateBouncingCar() {
        binding.bouncingCar.animate()
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(1600)
            .withEndAction {
                createBounceAnimation().start()
            }
            .start()
    }
    
    private fun createBounceAnimation(): AnimatorSet {
        val bounce = ObjectAnimator.ofFloat(binding.bouncingCar, View.TRANSLATION_Y, 0f, -30f, 0f)
        bounce.duration = 800
        bounce.interpolator = BounceInterpolator()
        
        val rotate = ObjectAnimator.ofFloat(binding.bouncingCar, View.ROTATION, -5f, 5f, -5f)
        rotate.duration = 800
        rotate.interpolator = AccelerateDecelerateInterpolator()
        
        val set = AnimatorSet()
        set.playTogether(bounce, rotate)
        
        set.doOnEnd {
            if (!isFinishing) {
                Handler(Looper.getMainLooper()).postDelayed({
                    createBounceAnimation().start()
                }, 400)
            }
        }
        
        return set
    }
    
    private fun navigateToNextScreen() {
        // Fade out animation before navigation
        binding.root.animate()
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                val nextActivity = when {
                    preferenceManager.isFirstLaunch -> OnboardingActivity::class.java
                    AuthRepository.isUserLoggedIn() -> MainActivity::class.java
                    else -> LoginActivity::class.java
                }
                startActivity(Intent(this, nextActivity))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            .start()
    }
}
