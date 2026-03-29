package com.example.apnivehicle.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.apnivehicle.databinding.FragmentUserProfileBinding
import com.example.apnivehicle.repository.AuthRepository

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val currentUser = AuthRepository.getCurrentUser()
            if (currentUser != null) {
                currentUser.avatarUri = uri.toString()
                binding.ivAvatar.setImageURI(uri)
                Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = AuthRepository.getCurrentUser()
        if (currentUser != null) {
            displayUserInfo(currentUser)
            setupListeners(currentUser)
        }
    }

    private fun displayUserInfo(user: com.example.apnivehicle.models.User) {
        binding.apply {
            tvUsername.text = user.username
            tvEmail.text = user.email
            inputPhone.setText(user.phoneNumber)
            inputLocation.setText(user.location)
            inputBio.setText(user.bio)
            tvVerificationBadge.text = if (user.isVerified) "✓ Verified" else "Not Verified"
            tvRating.text = "${user.rating}/5.0"
            tvReviewCount.text = "${user.reviewCount} reviews"
            tvTotalListings.text = "Listings: ${user.totalListings}"
            tvTotalSold.text = "Sold: ${user.totalSold}"
            tvResponseTime.text = "Response: ${user.responseTime} min"

            if (user.avatarUri != null && user.avatarUri!!.isNotEmpty()) {
                try {
                    ivAvatar.setImageURI(android.net.Uri.parse(user.avatarUri))
                } catch (e: Exception) {
                    // Use default image
                }
            }
        }
    }

    private fun setupListeners(user: com.example.apnivehicle.models.User) {
        binding.apply {
            btnChangeAvatar.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }

            btnEditProfile.setOnClickListener {
                enableEditing(true)
            }

            btnSaveProfile.setOnClickListener {
                saveProfile(user)
            }

            btnCancel.setOnClickListener {
                enableEditing(false)
                displayUserInfo(user)
            }

            btnVerifyPhone.setOnClickListener {
                showVerificationDialog("Phone Number", inputPhone.text.toString())
            }

            btnVerifyEmail.setOnClickListener {
                showVerificationDialog("Email", user.email)
            }
        }
    }

    private fun enableEditing(enabled: Boolean) {
        binding.apply {
            inputPhone.isEnabled = enabled
            inputLocation.isEnabled = enabled
            inputBio.isEnabled = enabled
            btnEditProfile.visibility = if (enabled) View.GONE else View.VISIBLE
            btnSaveProfile.visibility = if (enabled) View.VISIBLE else View.GONE
            btnCancel.visibility = if (enabled) View.VISIBLE else View.GONE
        }
    }

    private fun saveProfile(user: com.example.apnivehicle.models.User) {
        val phone = binding.inputPhone.text.toString().trim()
        val location = binding.inputLocation.text.toString().trim()
        val bio = binding.inputBio.text.toString().trim()

        if (phone.isEmpty() || location.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        user.phoneNumber = phone
        user.location = location
        user.bio = bio

        enableEditing(false)
        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showVerificationDialog(type: String, value: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Verify $type")
            .setMessage("We'll send an OTP to verify your $type:\n$value")
            .setPositiveButton("Send OTP") { dialog, _ ->
                Toast.makeText(requireContext(), "OTP sent to $value", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

