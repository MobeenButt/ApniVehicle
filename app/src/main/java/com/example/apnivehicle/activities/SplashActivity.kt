package com.example.apnivehicle.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.apnivehicle.R
import com.example.apnivehicle.repository.AuthRepository

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize AuthRepository with context
        AuthRepository.init(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivity = if (AuthRepository.isUserLoggedIn()) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }
            startActivity(Intent(this, nextActivity))
            finish()
        }, 2000)
    }
}
