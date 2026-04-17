package com.example.apnivehicle.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.apnivehicle.databinding.FragmentVerificationBinding
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.utils.EmailService
import com.example.apnivehicle.utils.ValidationUtils

class VerificationFragment : Fragment() {

    private var _binding: FragmentVerificationBinding? = null
    private val binding get() = _binding!!
    
    private var emailVerificationCode = ""
    private var phoneVerificationCode = ""
    
    private val pickCnicFrontLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val currentUser = AuthRepository.getCurrentUser()
            currentUser?.cnicFrontImage = uri.toString()
            binding.ivCnicFront.setImageURI(uri)
            binding.tvCnicFrontStatus.text = "✓ Front uploaded"
            checkCnicComplete()
        }
    }
    
    private val pickCnicBackLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val currentUser = AuthRepository.getCurrentUser()
            currentUser?.cnicBackImage = uri.toString()
            binding.ivCnicBack.setImageURI(uri)
            binding.tvCnicBackStatus.text = "✓ Back uploaded"
            checkCnicComplete()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val currentUser = AuthRepository.getCurrentUser()
        if (currentUser != null) {
            updateVerificationStatus(currentUser)
            setupListeners(currentUser)
        }
    }
    
    private fun updateVerificationStatus(user: com.example.apnivehicle.models.User) {
        binding.apply {
            // Email verification status
            tvEmailStatus.text = if (user.isEmailVerified) "✓ Verified" else "Not Verified"
            btnVerifyEmail.isEnabled = !user.isEmailVerified
            
            // Phone verification status
            tvPhoneStatus.text = if (user.isPhoneVerified) "✓ Verified" else "Not Verified"
            btnVerifyPhone.isEnabled = !user.isPhoneVerified
            
            // CNIC verification status
            tvCnicStatus.text = when {
                user.isCnicVerified -> "✓ Verified"
                user.cnicNumber.isNotEmpty() -> "Under Review"
                else -> "Not Verified"
            }
            btnSubmitCnic.isEnabled = !user.isCnicVerified
            
            // Overall verification badge
            val allVerified = user.isEmailVerified && user.isPhoneVerified && user.isCnicVerified
            user.isVerified = allVerified
            tvOverallStatus.text = if (allVerified) {
                "🎉 You are a Verified Seller!"
            } else {
                "Complete all verifications to become a Verified Seller"
            }
            
            // Show CNIC images if uploaded
            if (!user.cnicFrontImage.isNullOrEmpty()) {
                ivCnicFront.setImageURI(android.net.Uri.parse(user.cnicFrontImage))
                tvCnicFrontStatus.text = "✓ Front uploaded"
            }
            if (!user.cnicBackImage.isNullOrEmpty()) {
                ivCnicBack.setImageURI(android.net.Uri.parse(user.cnicBackImage))
                tvCnicBackStatus.text = "✓ Back uploaded"
            }
            if (!user.cnicNumber.isEmpty()) {
                inputCnic.setText(user.cnicNumber)
            }
        }
    }
    
    private fun setupListeners(user: com.example.apnivehicle.models.User) {
        binding.apply {
            // Email verification
            btnVerifyEmail.setOnClickListener {
                sendEmailVerification(user)
            }
            
            // Phone verification
            btnVerifyPhone.setOnClickListener {
                sendPhoneVerification(user)
            }
            
            // CNIC upload
            btnUploadCnicFront.setOnClickListener {
                pickCnicFrontLauncher.launch("image/*")
            }
            
            btnUploadCnicBack.setOnClickListener {
                pickCnicBackLauncher.launch("image/*")
            }
            
            // CNIC submission
            btnSubmitCnic.setOnClickListener {
                submitCnicVerification(user)
            }
        }
    }
    
    private fun sendEmailVerification(user: com.example.apnivehicle.models.User) {
        emailVerificationCode = EmailService.generateVerificationCode()
        user.verificationToken = emailVerificationCode
        
        EmailService.sendVerificationEmailViaClient(
            requireContext(),
            user.email,
            user.username,
            emailVerificationCode
        )
        
        Toast.makeText(
            requireContext(),
            "Verification email prepared. Please send it.",
            Toast.LENGTH_LONG
        ).show()
        
        // Show dialog to enter code
        showVerificationCodeDialog("Email", emailVerificationCode) { enteredCode ->
            if (enteredCode == emailVerificationCode) {
                user.isEmailVerified = true
                AuthRepository.updateUser(user)
                updateVerificationStatus(user)
                Toast.makeText(requireContext(), "Email verified successfully!", Toast.LENGTH_SHORT).show()
                checkAllVerificationsComplete(user)
            } else {
                Toast.makeText(requireContext(), "Invalid verification code", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun sendPhoneVerification(user: com.example.apnivehicle.models.User) {
        if (user.phoneNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Please add phone number in profile first", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!ValidationUtils.isValidPakistanPhone(user.phoneNumber)) {
            Toast.makeText(requireContext(), "Invalid phone number format", Toast.LENGTH_SHORT).show()
            return
        }
        
        phoneVerificationCode = EmailService.generateVerificationCode()
        
        EmailService.sendPhoneVerificationEmail(
            requireContext(),
            user.email,
            user.username,
            user.phoneNumber,
            phoneVerificationCode
        )
        
        Toast.makeText(
            requireContext(),
            "Verification code sent to your email",
            Toast.LENGTH_LONG
        ).show()
        
        // Show dialog to enter code
        showVerificationCodeDialog("Phone", phoneVerificationCode) { enteredCode ->
            if (enteredCode == phoneVerificationCode) {
                user.isPhoneVerified = true
                AuthRepository.updateUser(user)
                updateVerificationStatus(user)
                Toast.makeText(requireContext(), "Phone verified successfully!", Toast.LENGTH_SHORT).show()
                checkAllVerificationsComplete(user)
            } else {
                Toast.makeText(requireContext(), "Invalid verification code", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun submitCnicVerification(user: com.example.apnivehicle.models.User) {
        val cnicNumber = binding.inputCnic.text.toString().trim()
        
        if (cnicNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter CNIC number", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!cnicNumber.matches(Regex("^\\d{5}-\\d{7}-\\d{1}$"))) {
            Toast.makeText(requireContext(), "Invalid CNIC format. Use: 12345-1234567-1", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (user.cnicFrontImage.isNullOrEmpty() || user.cnicBackImage.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please upload both sides of CNIC", Toast.LENGTH_SHORT).show()
            return
        }
        
        user.cnicNumber = cnicNumber
        AuthRepository.updateUser(user)
        
        EmailService.sendCnicVerificationEmail(
            requireContext(),
            user.email,
            user.username,
            cnicNumber
        )
        
        // Simulate verification (in real app, this would be reviewed by admin)
        AlertDialog.Builder(requireContext())
            .setTitle("CNIC Submitted")
            .setMessage("Your CNIC has been submitted for verification. Our team will review it within 24-48 hours.\n\nFor demo purposes, click 'Approve Now' to instantly verify.")
            .setPositiveButton("Approve Now") { _, _ ->
                user.isCnicVerified = true
                AuthRepository.updateUser(user)
                updateVerificationStatus(user)
                Toast.makeText(requireContext(), "CNIC verified!", Toast.LENGTH_SHORT).show()
                checkAllVerificationsComplete(user)
            }
            .setNegativeButton("Wait for Review", null)
            .show()
    }
    
    private fun checkCnicComplete() {
        val currentUser = AuthRepository.getCurrentUser()
        if (currentUser != null) {
            val bothUploaded = !currentUser.cnicFrontImage.isNullOrEmpty() && 
                              !currentUser.cnicBackImage.isNullOrEmpty()
            binding.btnSubmitCnic.isEnabled = bothUploaded && !currentUser.isCnicVerified
        }
    }
    
    private fun showVerificationCodeDialog(type: String, correctCode: String, onVerify: (String) -> Unit) {
        val input = EditText(requireContext()).apply {
            hint = "Enter 6-digit code"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Verify $type")
            .setMessage("Enter the 6-digit verification code sent to your email")
            .setView(input)
            .setPositiveButton("Verify") { _, _ ->
                val enteredCode = input.text.toString().trim()
                onVerify(enteredCode)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun checkAllVerificationsComplete(user: com.example.apnivehicle.models.User) {
        if (user.isEmailVerified && user.isPhoneVerified && user.isCnicVerified) {
            user.isVerified = true
            AuthRepository.updateUser(user)
            
            EmailService.sendVerificationCompleteEmail(
                requireContext(),
                user.email,
                user.username
            )
            
            AlertDialog.Builder(requireContext())
                .setTitle("🎉 Congratulations!")
                .setMessage("You are now a Verified Seller on ApniVehicle!\n\nYour profile will display a verified badge, and your listings will get higher visibility.")
                .setPositiveButton("Awesome!") { _, _ ->
                    updateVerificationStatus(user)
                }
                .setCancelable(false)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
