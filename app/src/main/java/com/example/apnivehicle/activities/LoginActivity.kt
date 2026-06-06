package com.example.apnivehicle.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ActivityLoginBinding
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.utils.PreferenceManager
import com.example.apnivehicle.utils.ValidationUtils
import com.example.apnivehicle.utils.setDebouncedClickListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AuthRepository.init(this)
        preferenceManager = PreferenceManager(this)

        // Pre-fill email if remember me was checked
        preferenceManager.savedEmail?.let {
            binding.inputEmail.setText(it)
            binding.checkboxRememberMe.isChecked = true
        }

        setupValidation()
        setupClickListeners()
        playEntryAnimation()
    }

    private fun playEntryAnimation() {
        // Access views through binding to avoid any R resolution issues
        val headerSection = binding.root.findViewById<View>(com.example.apnivehicle.R.id.header_section)
        val formCard = binding.root.findViewById<View>(com.example.apnivehicle.R.id.form_card)

        headerSection?.let {
            it.translationY = -30f
            val fadeIn = ObjectAnimator.ofFloat(it, "alpha", 0f, 1f).setDuration(500)
            val slideIn = ObjectAnimator.ofFloat(it, "translationY", -30f, 0f).setDuration(500)
            slideIn.interpolator = DecelerateInterpolator()
            AnimatorSet().apply { playTogether(fadeIn, slideIn); start() }
        }

        formCard?.let {
            it.translationY = 60f
            val fadeIn = ObjectAnimator.ofFloat(it, "alpha", 0f, 1f).setDuration(500)
            val slideIn = ObjectAnimator.ofFloat(it, "translationY", 60f, 0f).setDuration(550)
            slideIn.interpolator = DecelerateInterpolator(1.5f)
            AnimatorSet().apply {
                playTogether(fadeIn, slideIn)
                startDelay = 150
                start()
            }
        }
    }

    private fun setupValidation() {
        binding.inputEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                if (email.isNotBlank() && !ValidationUtils.isValidEmail(email)) {
                    binding.textInputLayoutEmail.error = "Invalid email format"
                } else {
                    binding.textInputLayoutEmail.error = null
                }
            }
        })
    }

     private fun setupClickListeners() {
         binding.buttonLogin.setDebouncedClickListener(1500L) { loginUser() }
         binding.textSignUp.setDebouncedClickListener(1000L) {
             startActivity(Intent(this, SignUpActivity::class.java))
         }
         // Forgot password button hidden as per requirement
         binding.textForgotPassword.visibility = View.GONE
     }

    private fun loginUser() {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString()
        val rememberMe = binding.checkboxRememberMe.isChecked

        val emailValidation = ValidationUtils.validateEmail(email)
        if (!emailValidation.isValid) {
            binding.textInputLayoutEmail.error = emailValidation.errorMessage
            return
        } else {
            binding.textInputLayoutEmail.error = null
        }

        if (password.isBlank()) {
            binding.textInputLayoutPassword.error = "Password is required"
            return
        } else {
            binding.textInputLayoutPassword.error = null
        }

        setLoading(true)

        lifecycleScope.launch {
            val result = AuthRepository.loginAsync(email, password, rememberMe)
            setLoading(false)
            result.onSuccess {
                Snackbar.make(binding.root, "Welcome back!", Snackbar.LENGTH_SHORT).show()
                // Clear the task so pressing back from MainActivity doesn't return to LoginActivity
                val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            result.onFailure { error ->
                val errorMessage = error.message ?: "Login failed"
                val userFriendlyMessage = when {
                    errorMessage.contains("Incorrect email or password", ignoreCase = true) ||
                    errorMessage.contains("Incorrect password", ignoreCase = true) ||
                    errorMessage.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                    errorMessage.contains("wrong-password", ignoreCase = true) ->
                        "Incorrect email or password. Please try again."
                    errorMessage.contains("not found", ignoreCase = true) ||
                    errorMessage.contains("user-not-found", ignoreCase = true) ->
                        "No account found with this email. Please sign up first."
                    errorMessage.contains("disabled", ignoreCase = true) ->
                        "This account has been disabled. Please contact support."
                    errorMessage.contains("too many", ignoreCase = true) ||
                    errorMessage.contains("TOO_MANY", ignoreCase = true) ->
                        "Too many failed attempts. Please wait a few minutes and try again."
                    errorMessage.contains("network", ignoreCase = true) ||
                    errorMessage.contains("unable to resolve", ignoreCase = true) ||
                    errorMessage.contains("timeout", ignoreCase = true) ->
                        "Network error. Please check your internet connection."
                    errorMessage.contains("Email and password are required", ignoreCase = true) ->
                        "Please enter both email and password."
                    else -> errorMessage
                }
                Snackbar.make(binding.root, userFriendlyMessage, Snackbar.LENGTH_LONG).show()
            }
        }
    }

     private fun setLoading(loading: Boolean) {
         binding.buttonLogin.isEnabled = !loading
         binding.progressLogin?.visibility = if (loading) View.VISIBLE else View.GONE
     }

     // Note: showForgotPasswordDialog() method removed as forgot password feature is hidden
     // If this feature is needed in future, the method can be restored from version control
}
