package com.example.apnivehicle.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.apnivehicle.databinding.ActivityLoginBinding
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.utils.PreferenceManager
import com.example.apnivehicle.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize AuthRepository if not already initialized
        AuthRepository.init(this)
        
        preferenceManager = PreferenceManager(this)
        
        // Pre-fill email if remember me was checked
        preferenceManager.savedEmail?.let {
            binding.inputEmail.setText(it)
            binding.checkboxRememberMe.isChecked = true
        }
        
        setupValidation()
        setupClickListeners()
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
        binding.buttonLogin.setOnClickListener {
            loginUser()
        }

        binding.textSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        
        binding.textForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun loginUser() {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString()
        val rememberMe = binding.checkboxRememberMe.isChecked

        // Validate email
        val emailValidation = ValidationUtils.validateEmail(email)
        if (!emailValidation.isValid) {
            binding.textInputLayoutEmail.error = emailValidation.errorMessage
            return
        } else {
            binding.textInputLayoutEmail.error = null
        }
        
        // Validate password
        if (password.isBlank()) {
            binding.textInputLayoutPassword.error = "Password is required"
            return
        } else {
            binding.textInputLayoutPassword.error = null
        }

        // Disable button during login
        binding.buttonLogin.isEnabled = false

        val result = AuthRepository.login(email, password, rememberMe)
        result.onSuccess {
            Snackbar.make(binding.root, "Welcome back!", Snackbar.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        result.onFailure { error ->
            binding.buttonLogin.isEnabled = true
            Snackbar.make(
                binding.root,
                error.message ?: "Login failed",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
    
    private fun showForgotPasswordDialog() {
        // TODO: Implement forgot password flow
        Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
    }
}

