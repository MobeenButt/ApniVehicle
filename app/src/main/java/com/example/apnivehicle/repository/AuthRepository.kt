package com.example.apnivehicle.repository

import android.content.Context
import com.example.apnivehicle.models.User
import com.example.apnivehicle.utils.JsonFileHandler
import com.example.apnivehicle.utils.PreferenceManager
import com.example.apnivehicle.utils.ValidationUtils

object AuthRepository {
    private val users = mutableListOf<User>()
    private var currentUser: User? = null
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var context: Context
    private var isInitialized = false
    
    private const val USERS_FILE = "users.json"
    
    fun init(context: Context) {
        if (isInitialized) return
        
        this.context = context
        preferenceManager = PreferenceManager(context)
        isInitialized = true
        
        // Load users from JSON file
        val loadedUsers = JsonFileHandler.loadFromFile<List<User>>(context, USERS_FILE)
        if (loadedUsers != null) {
            users.clear()
            users.addAll(loadedUsers)
        } else {
            // Add sample users for testing
            users.add(User(
                email = "test@example.com",
                username = "testuser",
                password = "Test@123",
                phoneNumber = "0300-1234567",
                location = "Lahore"
            ))
            users.add(User(
                email = "demo@example.com",
                username = "demouser",
                password = "Demo@123",
                phoneNumber = "0321-9876543",
                location = "Karachi"
            ))
            saveUsers()
        }
        
        // Load current user if remember me is enabled
        val userId = preferenceManager.currentUserId
        if (userId != null && preferenceManager.rememberMe) {
            currentUser = users.find { it.id == userId }
        }
    }
    
    private fun saveUsers() {
        JsonFileHandler.saveToFile(context, USERS_FILE, users)
    }
    
    fun isUserLoggedIn(): Boolean = currentUser != null
    
    fun getCurrentUser(): User? = currentUser
    
    fun signup(email: String, username: String, password: String, phoneNumber: String = ""): Result<User> {
        return try {
            // Check if user already exists
            if (users.any { it.email == email || it.username == username }) {
                return Result.failure(Exception("Email or username already exists"))
            }
            
            // Validate inputs
            if (email.isBlank() || username.isBlank() || password.isBlank()) {
                return Result.failure(Exception("All fields are required"))
            }
            
            if (!ValidationUtils.isValidEmail(email)) {
                return Result.failure(Exception("Invalid email format"))
            }
            
            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }
            
            if (phoneNumber.isNotBlank() && !ValidationUtils.isValidPakistanPhone(phoneNumber)) {
                return Result.failure(Exception("Invalid phone number format. Use 03XX-XXXXXXX"))
            }
            
            val newUser = User(
                email = email,
                username = username,
                password = password,
                phoneNumber = phoneNumber
            )
            users.add(newUser)
            currentUser = newUser
            saveCurrentUser(newUser)
            saveUsers()
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun login(email: String, password: String, rememberMe: Boolean = false): Result<User> {
        return try {
            // Validate inputs
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Email and password are required"))
            }
            
            val user = users.find { it.email == email && it.password == password }
            if (user != null) {
                currentUser = user
                preferenceManager.rememberMe = rememberMe
                if (rememberMe) {
                    preferenceManager.savedEmail = email
                }
                saveCurrentUser(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() {
        currentUser = null
        preferenceManager.currentUserId = null
        if (!preferenceManager.rememberMe) {
            preferenceManager.savedEmail = null
        }
    }
    
    fun updateUser(user: User): Result<User> {
        return try {
            val index = users.indexOfFirst { it.id == user.id }
            if (index != -1) {
                users[index] = user
                if (currentUser?.id == user.id) {
                    currentUser = user
                }
                saveUsers()
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun saveCurrentUser(user: User) {
        preferenceManager.currentUserId = user.id
    }
}

