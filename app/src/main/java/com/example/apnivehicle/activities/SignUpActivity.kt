package com.example.apnivehicle.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.ActivitySignupBinding
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize AuthRepository if not already initialized
        AuthRepository.init(this)

        setupValidation()
        setupClickListeners()
    }
    
    private fun setupValidation() {
        // Email validation
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
        
        // Phone validation
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
        
        // Password strength indicator
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
        
        // Confirm password matching
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
        binding.buttonSignUp.setOnClickListener {
            signupUser()
        }

        binding.textLogin.setOnClickListener {
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

        // Validate all fields
        var hasError = false
        
        // Validate username
        val nameValidation = ValidationUtils.validateName(username)
        if (!nameValidation.isValid) {
            binding.textInputLayoutUsername.error = nameValidation.errorMessage
            hasError = true
        } else {
            binding.textInputLayoutUsername.error = null
        }
        
        // Validate email
        val emailValidation = ValidationUtils.validateEmail(email)
        if (!emailValidation.isValid) {
            binding.textInputLayoutEmail.error = emailValidation.errorMessage
            hasError = true
        } else {
            binding.textInputLayoutEmail.error = null
        }
        
        // Validate phone (optional but if provided must be valid)
        if (phone.isNotBlank()) {
            val phoneValidation = ValidationUtils.validatePhone(phone)
            if (!phoneValidation.isValid) {
                binding.textInputLayoutPhone.error = phoneValidation.errorMessage
                hasError = true
            } else {
                binding.textInputLayoutPhone.error = null
            }
        } else {
            binding.textInputLayoutPhone.error = null
        }
        
        // Validate password
        val passwordValidation = ValidationUtils.validatePassword(password)
        if (!passwordValidation.isValid) {
            binding.textInputLayoutPassword.error = passwordValidation.errorMessage
            hasError = true
        } else {
            binding.textInputLayoutPassword.error = null
        }
        
        // Validate password match
        val matchValidation = ValidationUtils.validatePasswordMatch(password, confirmPassword)
        if (!matchValidation.isValid) {
            binding.textInputLayoutConfirmPassword.error = matchValidation.errorMessage
            hasError = true
        } else {
            binding.textInputLayoutConfirmPassword.error = null
        }
        
        if (hasError) return

        // Disable button during signup
        binding.buttonSignUp.isEnabled = false

        val result = AuthRepository.signup(email, username, password, phone)
        result.onSuccess {
            Snackbar.make(binding.root, "Account created successfully!", Snackbar.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        result.onFailure { error ->
            binding.buttonSignUp.isEnabled = true
            Snackbar.make(
                binding.root,
                error.message ?: "Sign up failed",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}

