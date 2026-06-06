package com.example.apnivehicle.repository

import android.content.Context
import android.util.Log
import com.example.apnivehicle.models.User
import com.example.apnivehicle.utils.JsonFileHandler
import com.example.apnivehicle.utils.PreferenceManager
import com.example.apnivehicle.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * AuthRepository — wraps Firebase Auth + Firestore for user management.
 * Falls back to local JSON when Firestore is unavailable (offline mode).
 */
object AuthRepository {

    private const val TAG = "AuthRepository"
    private const val USERS_FILE = "users.json"
    private const val COLLECTION_USERS = "users"

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // In-memory cache of users (local fallback)
    private val localUsers = mutableListOf<User>()
    private var currentLocalUser: User? = null

    // Fix: changed from lateinit to nullable so any code that calls these before init()
    // (e.g. FCM token refresh, VehicleRepository.getMyAds) doesn't crash with
    // UninitializedPropertyAccessException.
    private var preferenceManager: PreferenceManager? = null
    private var appContext: Context? = null
    @Volatile private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext
        preferenceManager = PreferenceManager(appContext!!)
        isInitialized = true

        // Load local users as fallback
        val loaded = JsonFileHandler.loadFromFile<List<User>>(appContext!!, USERS_FILE)
        if (!loaded.isNullOrEmpty()) {
            localUsers.clear()
            localUsers.addAll(loaded)
        } else {
            loadSampleUsers()
            saveLocalUsers()
        }

        // Restore current user ONLY if Firebase also confirms an active session.
        // Without this check, a stale local cache entry would make isUserLoggedIn()
        // return true even after the Firebase session has expired or been signed out.
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            // Firebase has a live session — restore matching local profile
            currentLocalUser = localUsers.find { it.id == firebaseUser.uid }
                ?: User(id = firebaseUser.uid, email = firebaseUser.email ?: "", username = firebaseUser.displayName ?: "")
        } else {
            // No Firebase session → do NOT restore local user, even if rememberMe is set.
            // The user will need to log in again so Firebase can issue a fresh token.
            currentLocalUser = null
            preferenceManager?.currentUserId = null
        }
    }

    // ===== Auth State =====

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null && currentLocalUser == null) {
            currentLocalUser = localUsers.find { it.id == firebaseUser.uid }
                ?: User(id = firebaseUser.uid, email = firebaseUser.email ?: "", username = firebaseUser.displayName ?: "")
        }
        return currentLocalUser
    }

    fun getFirebaseUser(): FirebaseUser? = auth.currentUser

    fun getUserById(userId: String): User? = localUsers.find { it.id == userId }

    // ===== Sign Up =====

    /**
     * Async signup using Firebase Auth + Firestore.
     * Falls back to local JSON on failure.
     */
    suspend fun signupAsync(
        email: String,
        username: String,
        password: String,
        phoneNumber: String = ""
    ): Result<User> {
        return try {
            // Validate inputs first
            if (email.isBlank() || username.isBlank() || password.isBlank())
                return Result.failure(Exception("All fields are required"))
            if (!ValidationUtils.isValidEmail(email))
                return Result.failure(Exception("Invalid email format"))
            if (password.length < 6)
                return Result.failure(Exception("Password must be at least 6 characters"))
            if (phoneNumber.isNotBlank() && !ValidationUtils.isValidPakistanPhone(phoneNumber))
                return Result.failure(Exception("Invalid phone number format. Use 03XX-XXXXXXX"))

            // DO NOT check localUsers for duplicate email before Firebase.
            // The local list only holds users from this device — a user registered on another
            // device or directly in Firebase Console would be blocked by a false positive here.
            // Firebase Auth itself will return "email-already-in-use" if the account exists.

            // Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Sign up failed"))

            val newUser = User(
                id = firebaseUser.uid,
                email = email,
                username = username,
                password = password,  // Store password locally for offline fallback login
                phoneNumber = phoneNumber
            )

            // Save to Firestore (without plain password)
            val userForCloud = newUser.copy(password = "")
            db.collection(COLLECTION_USERS).document(firebaseUser.uid).set(userForCloud).await()

            // Update local cache
            localUsers.removeAll { it.email == email }
            localUsers.add(newUser)
            currentLocalUser = newUser
            saveLocalUsers()
            preferenceManager?.currentUserId = newUser.id

            Log.d(TAG, "Signup successful: ${firebaseUser.uid}")
            Result.success(newUser)
        } catch (e: Exception) {
            val msg = e.message ?: ""
            // Surface clean Firebase errors directly — don't silently fall to local signup
            // if the user already exists in Firebase (that would create a ghost local account).
            when {
                msg.contains("email-already-in-use", ignoreCase = true) ||
                msg.contains("EMAIL_EXISTS", ignoreCase = true) ->
                    Result.failure(Exception("This email is already registered. Please log in instead."))
                msg.contains("network", ignoreCase = true) ||
                msg.contains("unable to resolve", ignoreCase = true) -> {
                    // Offline: fall back to local-only signup
                    Log.w(TAG, "No network during signup, saving locally", e)
                    signupLocal(email, username, password, phoneNumber)
                }
                else -> {
                    Log.e(TAG, "signupAsync failed", e)
                    Result.failure(Exception(msg.ifBlank { "Sign up failed. Please try again." }))
                }
            }
        }
    }

    /** Synchronous local-only signup (used as fallback) */
    fun signup(email: String, username: String, password: String, phoneNumber: String = ""): Result<User> {
        return signupLocal(email, username, password, phoneNumber)
    }

    private fun signupLocal(email: String, username: String, password: String, phoneNumber: String): Result<User> {
        return try {
            if (localUsers.any { it.email == email || it.username == username })
                return Result.failure(Exception("Email or username already exists"))
            if (email.isBlank() || username.isBlank() || password.isBlank())
                return Result.failure(Exception("All fields are required"))
            if (!ValidationUtils.isValidEmail(email))
                return Result.failure(Exception("Invalid email format"))
            if (password.length < 6)
                return Result.failure(Exception("Password must be at least 6 characters"))
            if (phoneNumber.isNotBlank() && !ValidationUtils.isValidPakistanPhone(phoneNumber))
                return Result.failure(Exception("Invalid phone number format. Use 03XX-XXXXXXX"))

            val newUser = User(email = email, username = username, password = password, phoneNumber = phoneNumber)
            localUsers.add(newUser)
            currentLocalUser = newUser
            saveLocalUsers()
            preferenceManager?.currentUserId = newUser.id
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== Login =====

    suspend fun loginAsync(email: String, password: String, rememberMe: Boolean = false): Result<User> {
        if (email.isBlank() || password.isBlank())
            return Result.failure(Exception("Email and password are required"))

        // ── Step 1: Try Firebase Auth ──────────────────────────────────────────────
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Login failed. Please try again."))

            // Fetch full profile from Firestore
            val doc = try {
                db.collection(COLLECTION_USERS).document(firebaseUser.uid).get().await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore profile fetch failed, using basic user", e)
                null
            }

            val cloudUser = if (doc != null && doc.exists()) {
                val deserialized = doc.toObject(User::class.java)
                // @DocumentId fills in the id field, but as a safety net ensure it's correct
                deserialized?.let {
                    if (it.id.isBlank()) it.copy(id = firebaseUser.uid) else it
                } ?: User(id = firebaseUser.uid, email = email, username = firebaseUser.displayName ?: "")
            } else {
                // User exists in Firebase Auth but has no Firestore doc yet — create one
                val fallbackUser = User(id = firebaseUser.uid, email = email, username = firebaseUser.displayName ?: email.substringBefore("@"))
                db.collection(COLLECTION_USERS).document(firebaseUser.uid).set(fallbackUser.copy(password = ""))
                    .addOnFailureListener { Log.e(TAG, "Failed to create missing Firestore doc", it) }
                fallbackUser
            }

            // Preserve locally-stored password for offline fallback; NEVER write it back to Firestore
            val existingLocal = localUsers.find { it.id == cloudUser.id }
            val user = cloudUser.copy(
                password = existingLocal?.password ?: password
            )

            // Sync to local cache
            localUsers.removeAll { it.id == user.id || it.email == user.email }
            localUsers.add(user)
            currentLocalUser = user
            saveLocalUsers()

            preferenceManager?.rememberMe = rememberMe
            if (rememberMe) preferenceManager?.savedEmail = email
            preferenceManager?.currentUserId = user.id

            Log.d(TAG, "Firebase login success: ${user.id}")
            return Result.success(user)

        } catch (firebaseEx: Exception) {
            val msg = firebaseEx.message ?: ""
            Log.w(TAG, "Firebase login failed: $msg")

            // ── Classify the Firebase error ──────────────────────────────────────
            return when {
                // Wrong password — definitive, do NOT fall back to local (local has no password match either)
                msg.contains("wrong-password", ignoreCase = true) ||
                msg.contains("INVALID_PASSWORD", ignoreCase = true) ||
                msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                msg.contains("password is invalid", ignoreCase = true) ->
                    Result.failure(Exception("Incorrect email or password. Please try again."))

                // Account doesn't exist in Firebase — try local (offline-created account)
                msg.contains("user-not-found", ignoreCase = true) ||
                msg.contains("USER_NOT_FOUND", ignoreCase = true) ||
                msg.contains("no user record", ignoreCase = true) -> {
                    Log.d(TAG, "User not in Firebase, trying local fallback")
                    loginLocal(email, password, rememberMe)
                }

                // No internet — try local cache
                msg.contains("network", ignoreCase = true) ||
                msg.contains("unable to resolve", ignoreCase = true) ||
                msg.contains("timeout", ignoreCase = true) ||
                msg.contains("connect", ignoreCase = true) -> {
                    Log.d(TAG, "No network, trying local fallback")
                    loginLocal(email, password, rememberMe)
                }

                // Account disabled / blocked
                msg.contains("user-disabled", ignoreCase = true) ||
                msg.contains("USER_DISABLED", ignoreCase = true) ->
                    Result.failure(Exception("This account has been disabled. Contact support."))

                // Too many failed attempts
                msg.contains("too-many-requests", ignoreCase = true) ||
                msg.contains("TOO_MANY_ATTEMPTS", ignoreCase = true) ->
                    Result.failure(Exception("Too many failed attempts. Please wait a few minutes and try again."))

                // Anything else — try local as last resort but show the real error if local also fails
                else -> {
                    Log.w(TAG, "Unknown Firebase error, trying local fallback")
                    val localResult = loginLocal(email, password, rememberMe)
                    if (localResult.isSuccess) localResult
                    else Result.failure(Exception("Login failed: ${msg.ifBlank { "Unknown error" }}"))
                }
            }
        }
    }

    /** Synchronous local-only login (used as fallback / offline) */
    fun login(email: String, password: String, rememberMe: Boolean = false): Result<User> {
        return loginLocal(email, password, rememberMe)
    }

    private fun loginLocal(email: String, password: String, rememberMe: Boolean): Result<User> {
        return try {
            if (email.isBlank() || password.isBlank())
                return Result.failure(Exception("Email and password are required"))
            val user = localUsers.find { it.email == email && it.password == password }
                ?: return Result.failure(Exception("Invalid email or password"))
            currentLocalUser = user
            preferenceManager?.rememberMe = rememberMe
            if (rememberMe) preferenceManager?.savedEmail = email
            preferenceManager?.currentUserId = user.id
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== Logout =====

    fun logout() {
        try { auth.signOut() } catch (_: Exception) {}
        currentLocalUser = null
        preferenceManager?.currentUserId = null
        if (preferenceManager?.rememberMe != true) preferenceManager?.savedEmail = null
    }

    // ===== Update User =====

    fun updateUser(user: User): Result<User> {
        return try {
            val index = localUsers.indexOfFirst { it.id == user.id }
            if (index != -1) localUsers[index] = user else localUsers.add(user)
            if (currentLocalUser?.id == user.id) currentLocalUser = user
            saveLocalUsers()

            // Async Firestore update — strip password before writing (fire-and-forget)
            val cloudUser = user.copy(password = "")
            db.collection(COLLECTION_USERS).document(user.id).set(cloudUser)
                .addOnFailureListener { Log.e(TAG, "Firestore updateUser failed", it) }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserAsync(user: User): Result<User> {
        return try {
            // Strip password before writing to Firestore
            val cloudUser = user.copy(password = "")
            db.collection(COLLECTION_USERS).document(user.id).set(cloudUser).await()
            val index = localUsers.indexOfFirst { it.id == user.id }
            if (index != -1) localUsers[index] = user else localUsers.add(user)
            if (currentLocalUser?.id == user.id) currentLocalUser = user
            saveLocalUsers()
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "updateUserAsync failed", e)
            updateUser(user)
        }
    }

    // ===== FCM Token =====

    fun saveFcmToken(token: String) {
        // Fix: guard against being called before init() (e.g. from FCM token refresh on cold start)
        val user = currentLocalUser ?: return
        user.fcmToken = token
        updateUser(user)
    }

    // ===== Forgot Password =====

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            if (email.isBlank()) return Result.failure(Exception("Email is required"))
            if (!ValidationUtils.isValidEmail(email))
                return Result.failure(Exception("Invalid email format"))

            auth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent to $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "sendPasswordResetEmail failed for $email", e)
            // Re-throw with the original Firebase message so the UI can parse the error code
            Result.failure(e)
        }
    }

    // ===== Private Helpers =====

    private fun saveLocalUsers() {
        val ctx = appContext ?: return
        JsonFileHandler.saveToFile(ctx, USERS_FILE, localUsers)
    }

    private fun loadSampleUsers() {
        localUsers.addAll(listOf(
            User(id = "seller-001", email = "seller001@example.com", username = "Ahmad Khan",
                password = "Test@123", phoneNumber = "0300-1234567", location = "Lahore",
                isVerified = true, isPhoneVerified = true, rating = 4.8f, reviewCount = 12),
            User(id = "seller-002", email = "seller002@example.com", username = "Fatima Ali",
                password = "Test@123", phoneNumber = "0321-9876543", location = "Karachi",
                isVerified = true, isPhoneVerified = true, rating = 5f, reviewCount = 28),
            User(id = "seller-003", email = "seller003@example.com", username = "Hassan Raza",
                password = "Test@123", phoneNumber = "0333-5555666", location = "Islamabad",
                isVerified = true, isPhoneVerified = true, rating = 4.5f, reviewCount = 8),
            User(id = "seller-004", email = "seller004@example.com", username = "Ayesha Malik",
                password = "Test@123", phoneNumber = "0345-7778899", location = "Lahore",
                isVerified = true, isPhoneVerified = true, rating = 4.7f, reviewCount = 15),
            User(id = "seller-005", email = "seller005@example.com", username = "Usman Sheikh",
                password = "Test@123", phoneNumber = "0301-2223344", location = "Faisalabad",
                isVerified = true, isPhoneVerified = true, rating = 4.6f, reviewCount = 10)
        ))
    }
}
