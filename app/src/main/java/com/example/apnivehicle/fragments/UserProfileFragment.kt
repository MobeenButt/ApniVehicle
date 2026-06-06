package com.example.apnivehicle.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.FragmentUserProfileBinding
import com.example.apnivehicle.models.User
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.utils.NetworkMonitor
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    // Holds the verification ID returned by Firebase Phone Auth
    private var phoneVerificationId: String? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        val b = _binding ?: return@registerForActivityResult
        val currentUser = AuthRepository.getCurrentUser() ?: return@registerForActivityResult

        // Show immediately in UI using Glide (handles both local URIs and http URLs)
        Glide.with(this).load(uri).circleCrop().into(b.ivAvatar)

        if (!NetworkMonitor.isCurrentlyOnline()) {
            // Offline: store local URI string — will be re-uploaded when online
            currentUser.avatarUri = uri.toString()
            AuthRepository.updateUser(currentUser)
            Toast.makeText(requireContext(), "Photo saved locally (will sync when online)", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        lifecycleScope.launch {
            try {
                b.btnChangeAvatar.isEnabled = false
                b.btnChangeAvatar.text = "Uploading..."

                val ref = FirebaseStorage.getInstance().reference
                    .child("avatars/${currentUser.id}.jpg")
                ref.putFile(uri).await()
                val downloadUrl = ref.downloadUrl.await().toString()

                // Save URL to user object and persist to Firestore
                currentUser.avatarUri = downloadUrl
                AuthRepository.updateUserAsync(currentUser)

                val bSafe = _binding ?: return@launch
                // Reload from URL so we confirm the upload worked
                Glide.with(this@UserProfileFragment)
                    .load(downloadUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(bSafe.ivAvatar)

                Toast.makeText(requireContext(), "Profile photo updated", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e("UserProfile", "Avatar upload failed", e)
                // Fall back to local URI
                currentUser.avatarUri = uri.toString()
                AuthRepository.updateUser(currentUser)
                Toast.makeText(requireContext(), "Upload failed — saved locally", Toast.LENGTH_SHORT).show()
            } finally {
                _binding?.btnChangeAvatar?.isEnabled = true
                _binding?.btnChangeAvatar?.text = "Change Photo"
            }
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = AuthRepository.getCurrentUser() ?: return
        displayUserInfo(currentUser)
        setupListeners(currentUser)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Display ───────────────────────────────────────────────────────────────

    private fun displayUserInfo(user: User) {
        val b = _binding ?: return
        b.tvUsername.text = user.username
        b.tvEmail.text = user.email
        b.inputPhone.setText(user.phoneNumber)
        b.inputLocation.setText(user.location)
        b.inputBio.setText(user.bio)

        // Verification badge
        b.tvVerificationBadge.text = when {
            user.isVerified       -> "✓ Verified"
            user.isEmailVerified  -> "✓ Email Verified"
            else                  -> "Not Verified"
        }

        b.tvRating.text        = "${user.rating}/5.0"
        b.tvReviewCount.text   = "${user.reviewCount} reviews"
        b.tvTotalListings.text = "Listings: ${user.totalListings}"
        b.tvTotalSold.text     = "Sold: ${user.totalSold}"
        b.tvResponseTime.text  = "Response: ${user.responseTime} min"

        // Profile photo — use Glide for both http URLs and local file URIs
        val avatar = user.avatarUri
        if (!avatar.isNullOrEmpty()) {
            Glide.with(this)
                .load(avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(b.ivAvatar)
        }

        // Update verify-email button label based on current status
        b.btnVerifyEmail.text = if (user.isEmailVerified) "✓ Email Verified" else "Verify Email"
        b.btnVerifyEmail.isEnabled = !user.isEmailVerified

        // Update verify-phone button label
        b.btnVerifyPhone.text = if (user.isPhoneVerified) "✓ Phone Verified" else "Verify"
        b.btnVerifyPhone.isEnabled = !user.isPhoneVerified
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private fun setupListeners(user: User) {
        val b = _binding ?: return

        b.btnChangeAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        b.btnEditProfile.setOnClickListener { enableEditing(true) }

        b.btnSaveProfile.setOnClickListener { saveProfile(user) }

        b.btnCancel.setOnClickListener {
            enableEditing(false)
            displayUserInfo(user)
        }

        b.btnVerifyPhone.setOnClickListener {
            val phone = b.inputPhone.text.toString().trim()
            if (phone.isEmpty()) {
                Toast.makeText(requireContext(), "Enter your phone number first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startPhoneVerification(phone, user)
        }

        b.btnVerifyEmail.setOnClickListener {
            sendEmailVerification(user)
        }

        b.btnViewReviews?.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ReviewsFragment.newInstance(user.id, "seller"))
                .addToBackStack(null)
                .commit()
        }
    }

    private fun enableEditing(enabled: Boolean) {
        val b = _binding ?: return
        b.inputPhone.isEnabled    = enabled
        b.inputLocation.isEnabled = enabled
        b.inputBio.isEnabled      = enabled
        b.btnEditProfile.visibility  = if (enabled) View.GONE else View.VISIBLE
        b.btnSaveProfile.visibility  = if (enabled) View.VISIBLE else View.GONE
        b.btnCancel.visibility       = if (enabled) View.VISIBLE else View.GONE
    }

    private fun saveProfile(user: User) {
        val b = _binding ?: return
        val phone    = b.inputPhone.text.toString().trim()
        val location = b.inputLocation.text.toString().trim()
        val bio      = b.inputBio.text.toString().trim()

        if (location.isEmpty()) {
            Toast.makeText(requireContext(), "Location cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        user.phoneNumber = phone
        user.location    = location
        user.bio         = bio

        lifecycleScope.launch {
            AuthRepository.updateUserAsync(user)
            _binding ?: return@launch
            enableEditing(false)
            displayUserInfo(user)
            Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Email Verification (Firebase Auth built-in) ───────────────────────────

    private fun sendEmailVerification(user: User) {
        if (!NetworkMonitor.isCurrentlyOnline()) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "Please log in again to verify email", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                _binding?.btnVerifyEmail?.isEnabled = false
                _binding?.btnVerifyEmail?.text = "Sending..."

                firebaseUser.sendEmailVerification().await()

                _binding ?: return@launch

                // Show dialog explaining what to do
                AlertDialog.Builder(requireContext())
                    .setTitle("Verification Email Sent")
                    .setMessage(
                        "A verification link has been sent to:\n${user.email}\n\n" +
                        "Please open your email and click the link to verify your account.\n\n" +
                        "After clicking the link, come back here and tap \"I've Verified\" to confirm."
                    )
                    .setPositiveButton("I've Verified") { _, _ ->
                        checkEmailVerified(user)
                    }
                    .setNegativeButton("Later", null)
                    .show()

            } catch (e: Exception) {
                _binding ?: return@launch
                val msg = when {
                    e.message?.contains("too-many-requests", ignoreCase = true) == true ->
                        "Too many requests. Please wait a few minutes and try again."
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Check your internet connection."
                    else -> "Failed to send verification email: ${e.message}"
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            } finally {
                val b = _binding ?: return@launch
                b.btnVerifyEmail.isEnabled = !user.isEmailVerified
                b.btnVerifyEmail.text = if (user.isEmailVerified) "✓ Email Verified" else "Verify Email"
            }
        }
    }

    /** Called after user taps "I've Verified" — reload Firebase user to check email_verified flag */
    private fun checkEmailVerified(user: User) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        lifecycleScope.launch {
            try {
                // Force refresh the Firebase token so emailVerified is up-to-date
                firebaseUser.reload().await()
                val isVerified = firebaseUser.isEmailVerified

                if (isVerified) {
                    user.isEmailVerified = true
                    user.isVerified      = true
                    AuthRepository.updateUserAsync(user)

                    _binding ?: return@launch
                    displayUserInfo(user)
                    Toast.makeText(requireContext(), "✓ Email verified successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Not Verified Yet")
                        .setMessage(
                            "Your email hasn't been verified yet.\n\n" +
                            "Please check your inbox (and spam folder) for the verification link from Firebase."
                        )
                        .setPositiveButton("Resend Email") { _, _ -> sendEmailVerification(user) }
                        .setNegativeButton("Later", null)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error checking verification: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Phone Verification (Firebase Phone Auth — sends real SMS) ─────────────

    private fun startPhoneVerification(rawPhone: String, user: User) {
        if (!NetworkMonitor.isCurrentlyOnline()) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }

        // Normalize phone: convert 03XX-XXXXXXX → +923XXXXXXXXX
        val normalizedPhone = normalizePhoneNumber(rawPhone)
        if (normalizedPhone == null) {
            Toast.makeText(
                requireContext(),
                "Invalid phone format. Use: 03XX-XXXXXXX or 03XXXXXXXXX",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val b = _binding ?: return
        b.btnVerifyPhone.isEnabled = false
        b.btnVerifyPhone.text = "Sending SMS..."

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            /** SMS auto-retrieved (on supported devices) — verify immediately */
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                android.util.Log.d("UserProfile", "Phone auto-verified")
                signInWithPhoneCredential(credential, user, rawPhone)
            }

            /** Verification failed — show reason */
            override fun onVerificationFailed(e: FirebaseException) {
                android.util.Log.e("UserProfile", "Phone verification failed", e)
                val b2 = _binding ?: return
                b2.btnVerifyPhone.isEnabled = true
                b2.btnVerifyPhone.text = "Verify"

                val msg = when {
                    e.message?.contains("invalid", ignoreCase = true) == true ->
                        "Invalid phone number. Check the format and try again."
                    e.message?.contains("quota", ignoreCase = true) == true ->
                        "SMS quota exceeded. Please try again later."
                    e.message?.contains("blocked", ignoreCase = true) == true ->
                        "Phone verification blocked. Check Firebase console settings."
                    else -> "Verification failed: ${e.message}"
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }

            /** Code sent — show OTP input dialog */
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                phoneVerificationId = verificationId
                android.util.Log.d("UserProfile", "OTP code sent to $normalizedPhone")

                val b2 = _binding ?: return
                b2.btnVerifyPhone.isEnabled = true
                b2.btnVerifyPhone.text = "Verify"

                showOtpInputDialog(verificationId, user, rawPhone)
            }
        }

        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(normalizedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /** Show a dialog with a 6-digit OTP input field */
    private fun showOtpInputDialog(verificationId: String, user: User, rawPhone: String) {
        val b = _binding ?: return

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_otp_input, null)
        val otpInput = dialogView.findViewById<TextInputEditText>(R.id.input_otp)

        AlertDialog.Builder(requireContext())
            .setTitle("Enter OTP")
            .setMessage("A 6-digit code has been sent to $rawPhone\nPlease enter it below:")
            .setView(dialogView)
            .setPositiveButton("Verify") { _, _ ->
                val code = otpInput?.text.toString().trim()
                if (code.length != 6 || !code.all { it.isDigit() }) {
                    Toast.makeText(requireContext(), "Enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                signInWithPhoneCredential(credential, user, rawPhone)
            }
            .setNegativeButton("Resend") { _, _ ->
                startPhoneVerification(rawPhone, user)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    /** Link the phone credential to the current Firebase user */
    private fun signInWithPhoneCredential(
        credential: PhoneAuthCredential,
        user: User,
        rawPhone: String
    ) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Link phone credential to existing account (doesn't sign out)
                firebaseUser.linkWithCredential(credential).await()

                user.phoneNumber    = rawPhone
                user.isPhoneVerified = true
                user.isVerified     = user.isEmailVerified || true  // phone verified = seller verified
                AuthRepository.updateUserAsync(user)

                _binding ?: return@launch
                displayUserInfo(user)
                Toast.makeText(requireContext(), "✓ Phone number verified!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("invalid-verification-code", ignoreCase = true) == true ||
                    e.message?.contains("invalid verification code", ignoreCase = true) == true ->
                        "Incorrect OTP code. Please try again."
                    e.message?.contains("expired", ignoreCase = true) == true ->
                        "OTP expired. Please request a new one."
                    e.message?.contains("credential-already-in-use", ignoreCase = true) == true ->
                        "This phone number is already linked to another account."
                    else -> "Verification failed: ${e.message}"
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Convert Pakistani phone formats to E.164 format required by Firebase:
     *   03XX-XXXXXXX  → +923XXXXXXXXX
     *   03XXXXXXXXX   → +923XXXXXXXXX
     *   923XXXXXXXXX  → +923XXXXXXXXX
     *   +923XXXXXXXXX → +923XXXXXXXXX (already correct)
     */
    private fun normalizePhoneNumber(phone: String): String? {
        val digits = phone.replace(Regex("[^0-9+]"), "")
        return when {
            digits.startsWith("+92") && digits.length == 13 -> digits
            digits.startsWith("92")  && digits.length == 12 -> "+$digits"
            digits.startsWith("0")   && digits.length == 11 -> "+92${digits.substring(1)}"
            else -> null
        }
    }
}
