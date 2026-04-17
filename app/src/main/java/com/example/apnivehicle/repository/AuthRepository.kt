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
            // Add sample users for testing with phone numbers
            users.addAll(listOf(
                User(
                    id = "seller-001",
                    email = "seller001@example.com",
                    username = "Ahmad Khan",
                    password = "Test@123",
                    phoneNumber = "0300-1234567",
                    location = "Lahore",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.8f,
                    reviewCount = 12
                ),
                User(
                    id = "seller-002",
                    email = "seller002@example.com",
                    username = "Fatima Ali",
                    password = "Test@123",
                    phoneNumber = "0321-9876543",
                    location = "Karachi",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 5f,
                    reviewCount = 28
                ),
                User(
                    id = "seller-003",
                    email = "seller003@example.com",
                    username = "Hassan Raza",
                    password = "Test@123",
                    phoneNumber = "0333-5555666",
                    location = "Islamabad",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.5f,
                    reviewCount = 8
                ),
                User(
                    id = "seller-004",
                    email = "seller004@example.com",
                    username = "Ayesha Malik",
                    password = "Test@123",
                    phoneNumber = "0345-7778899",
                    location = "Lahore",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.7f,
                    reviewCount = 15
                ),
                User(
                    id = "seller-005",
                    email = "seller005@example.com",
                    username = "Usman Sheikh",
                    password = "Test@123",
                    phoneNumber = "0301-2223344",
                    location = "Faisalabad",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.6f,
                    reviewCount = 10
                ),
                User(
                    id = "seller-006",
                    email = "seller006@example.com",
                    username = "Zainab Ahmed",
                    password = "Test@123",
                    phoneNumber = "0322-4445566",
                    location = "Multan",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.9f,
                    reviewCount = 22
                ),
                User(
                    id = "seller-007",
                    email = "seller007@example.com",
                    username = "Ali Hassan",
                    password = "Test@123",
                    phoneNumber = "0334-6667788",
                    location = "Rawalpindi",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.9f,
                    reviewCount = 18
                ),
                User(
                    id = "seller-008",
                    email = "seller008@example.com",
                    username = "Sara Khan",
                    password = "Test@123",
                    phoneNumber = "0346-8889900",
                    location = "Peshawar",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.4f,
                    reviewCount = 9
                ),
                User(
                    id = "seller-009",
                    email = "seller009@example.com",
                    username = "Bilal Tariq",
                    password = "Test@123",
                    phoneNumber = "0311-1112233",
                    location = "Karachi",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 5f,
                    reviewCount = 35
                ),
                User(
                    id = "seller-010",
                    email = "seller010@example.com",
                    username = "Mariam Siddiqui",
                    password = "Test@123",
                    phoneNumber = "0323-3334455",
                    location = "Lahore",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.8f,
                    reviewCount = 20
                ),
                User(
                    id = "seller-011",
                    email = "seller011@example.com",
                    username = "Kamran Iqbal",
                    password = "Test@123",
                    phoneNumber = "0335-5556677",
                    location = "Sialkot",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.6f,
                    reviewCount = 14
                ),
                User(
                    id = "seller-012",
                    email = "seller012@example.com",
                    username = "Hina Butt",
                    password = "Test@123",
                    phoneNumber = "0347-7778899",
                    location = "Gujranwala",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.7f,
                    reviewCount = 16
                ),
                User(
                    id = "seller-013",
                    email = "seller013@example.com",
                    username = "Imran Yousaf",
                    password = "Test@123",
                    phoneNumber = "0312-9990011",
                    location = "Quetta",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.5f,
                    reviewCount = 11
                ),
                User(
                    id = "seller-014",
                    email = "seller014@example.com",
                    username = "Nadia Aziz",
                    password = "Test@123",
                    phoneNumber = "0324-1122334",
                    location = "Islamabad",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.9f,
                    reviewCount = 25
                ),
                User(
                    id = "seller-015",
                    email = "seller015@example.com",
                    username = "Faisal Mahmood",
                    password = "Test@123",
                    phoneNumber = "0336-2233445",
                    location = "Hyderabad",
                    isVerified = true,
                    isPhoneVerified = true,
                    rating = 4.3f,
                    reviewCount = 7
                )
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
    
    fun getUserById(userId: String): User? {
        return users.find { it.id == userId }
    }
    
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

