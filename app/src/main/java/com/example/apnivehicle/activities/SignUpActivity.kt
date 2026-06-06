package com.example.apnivehicle.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ActivitySignupBinding
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.utils.ValidationUtils
import com.example.apnivehicle.utils.setDebouncedClickListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AuthRepository.init(this)
        setupValidation()
        setupClickListeners()
        playEntryAnimation()
    }

    private fun playEntryAnimation() {
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

        binding.inputPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString()
                if (phone.isNotBlank() && !ValidationUtils.isValidPakistanPhone(phone)) {
                    binding.textInputLayoutPhone.error = "Format: 03XX-XXXXXXX"
                } else {
                    binding.textInputLayoutPhone.error = null
                }
            }
        })

        binding.inputPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.isNotBlank()) {
                    val strength = ValidationUtils.getPasswordStrength(password)
                    binding.tvPasswordStrength.text = "Strength: ${ValidationUtils.getPasswordStrengthText(strength)}"
                    binding.tvPasswordStrength.setTextColor(
                        ContextCompat.getColor(this@SignUpActivity, ValidationUtils.getPasswordStrengthColor(strength))
                    )
                } else {
                    binding.tvPasswordStrength.text = ""
                }
            }
        })

        binding.inputConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = binding.inputPassword.text.toString()
                val confirmPassword = s.toString()
                if (confirmPassword.isNotBlank() && password != confirmPassword) {
                    binding.textInputLayoutConfirmPassword.error = "Passwords do not match"
                } else {
                    binding.textInputLayoutConfirmPassword.error = null
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.buttonSignUp.setDebouncedClickListener(1500L) { signupUser() }
        binding.textLogin.setDebouncedClickListener(1000L) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun signupUser() {
        val email = binding.inputEmail.text.toString().trim()
        val username = binding.inputUsername.text.toString().trim()
        val phone = binding.inputPhone.text.toString().trim()
        val password = binding.inputPassword.text.toString()
        val confirmPassword = binding.inputConfirmPassword.text.toString()

        var hasError = false

        val nameValidation = ValidationUtils.validateName(username)
        if (!nameValidation.isValid) {
            binding.textInputLayoutUsername.error = nameValidation.errorMessage
            hasError = true
        } else binding.textInputLayoutUsername.error = null

        val emailValidation = ValidationUtils.validateEmail(email)
        if (!emailValidation.isValid) {
            binding.textInputLayoutEmail.error = emailValidation.errorMessage
            hasError = true
        } else binding.textInputLayoutEmail.error = null

        if (phone.isNotBlank()) {
            val phoneValidation = ValidationUtils.validatePhone(phone)
            if (!phoneValidation.isValid) {
                binding.textInputLayoutPhone.error = phoneValidation.errorMessage
                hasError = true
            } else binding.textInputLayoutPhone.error = null
        } else binding.textInputLayoutPhone.error = null

        val passwordValidation = ValidationUtils.validatePassword(password)
        if (!passwordValidation.isValid) {
            binding.textInputLayoutPassword.error = passwordValidation.errorMessage
            hasError = true
        } else binding.textInputLayoutPassword.error = null

        val matchValidation = ValidationUtils.validatePasswordMatch(password, confirmPassword)
        if (!matchValidation.isValid) {
            binding.textInputLayoutConfirmPassword.error = matchValidation.errorMessage
            hasError = true
        } else binding.textInputLayoutConfirmPassword.error = null

        if (hasError) return

        setLoading(true)

        lifecycleScope.launch {
            val result = AuthRepository.signupAsync(email, username, password, phone)
            setLoading(false)
            result.onSuccess {
                Snackbar.make(binding.root, "Account created successfully!", Snackbar.LENGTH_SHORT).show()
                // Clear the task so pressing back from MainActivity doesn't return to SignUpActivity
                val intent = Intent(this@SignUpActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            result.onFailure { error ->
                val msg = error.message ?: "Sign up failed"
                val friendly = when {
                    msg.contains("already registered", ignoreCase = true) ||
                    msg.contains("email-already-in-use", ignoreCase = true) ||
                    msg.contains("EMAIL_EXISTS", ignoreCase = true) ->
                        "This email is already registered. Please log in instead."
                    msg.contains("weak-password", ignoreCase = true) ->
                        "Password is too weak. Use at least 6 characters."
                    msg.contains("network", ignoreCase = true) ||
                    msg.contains("unable to resolve", ignoreCase = true) ->
                        "Network error. Please check your internet connection."
                    else -> msg
                }
                Snackbar.make(binding.root, friendly, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.buttonSignUp.isEnabled = !loading
        binding.progressSignup?.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
