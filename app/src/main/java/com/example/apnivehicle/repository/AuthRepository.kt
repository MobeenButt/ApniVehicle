package com.example.apnivehicle.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.apnivehicle.models.User

object AuthRepository {
    private val users = mutableListOf<User>()
    private var currentUser: User? = null
    private lateinit var sharedPreferences: SharedPreferences
    
    private const val PREF_NAME = "auth_prefs"
    private const val CURRENT_USER_ID = "current_user_id"
    
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        // Add sample users for testing
        if (users.isEmpty()) {
            users.add(User(email = "test@example.com", username = "testuser", password = "123456"))
            users.add(User(email = "demo@example.com", username = "demouser", password = "123456"))
        }
        
        // Load current user from SharedPreferences if exists
        val userId = sharedPreferences.getString(CURRENT_USER_ID, null)
        if (userId != null) {
            currentUser = users.find { it.id == userId }
        }
    }
    
    fun isUserLoggedIn(): Boolean = currentUser != null
    
    fun getCurrentUser(): User? = currentUser
    
    fun signup(email: String, username: String, password: String): Result<User> {
        return try {
            // Check if user already exists
            if (users.any { it.email == email || it.username == username }) {
                return Result.failure<User>(Exception("Email or username already exists"))
            }
            
            // Validate inputs
            if (email.isBlank() || username.isBlank() || password.isBlank()) {
                return Result.failure<User>(Exception("All fields are required"))
            }
            
            if (!isValidEmail(email)) {
                return Result.failure<User>(Exception("Invalid email format"))
            }
            
            if (password.length < 6) {
                return Result.failure<User>(Exception("Password must be at least 6 characters"))
            }
            
            val newUser = User(
                email = email,
                username = username,
                password = password
            )
            users.add(newUser)
            currentUser = newUser
            saveCurrentUser(newUser)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun login(email: String, password: String): Result<User> {
        return try {
            // Validate inputs
            if (email.isBlank() || password.isBlank()) {
                return Result.failure<User>(Exception("Email and password are required"))
            }
            
            val user = users.find { it.email == email && it.password == password }
            if (user != null) {
                currentUser = user
                saveCurrentUser(user)
                Result.success(user)
            } else {
                Result.failure<User>(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() {
        currentUser = null
        sharedPreferences.edit().remove(CURRENT_USER_ID).apply()
    }
    
    private fun saveCurrentUser(user: User) {
        sharedPreferences.edit().putString(CURRENT_USER_ID, user.id).apply()
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}

