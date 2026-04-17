package com.example.apnivehicle.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.apnivehicle.activities.LoginActivity
import com.example.apnivehicle.databinding.FragmentSettingsBinding
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.utils.Constants
import com.example.apnivehicle.utils.PreferenceManager
import com.example.apnivehicle.utils.ThemeManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferenceManager = PreferenceManager(requireContext())
        
        setupUserInfo()
        setupThemeToggle()
        setupLanguageSelection()
        setupNotificationToggle()
        setupAboutSection()
        setupLogout()
    }
    
    private fun setupUserInfo() {
        val currentUser = AuthRepository.getCurrentUser()
        if (currentUser != null) {
            binding.textUserEmail.text = "Email: ${currentUser.email}"
            binding.textUsername.text = "Username: ${currentUser.username}"
            
            // Show verification status
            val verificationStatus = when {
                currentUser.isVerified -> "✓ Verified Seller"
                currentUser.isEmailVerified || currentUser.isPhoneVerified || currentUser.isCnicVerified -> "Partially Verified"
                else -> "Not Verified"
            }
            binding.textUserEmail.text = "${binding.textUserEmail.text}\nStatus: $verificationStatus"
        }
    }
    
    private fun setupThemeToggle() {
        // Set initial state
        binding.switchTheme.isChecked = preferenceManager.isDarkTheme
        
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            ThemeManager.setTheme(requireContext(), isChecked)
            Toast.makeText(
                requireContext(),
                if (isChecked) "Dark theme enabled" else "Light theme enabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun setupLanguageSelection() {
        val currentLanguage = preferenceManager.language
        binding.textLanguageValue.text = if (currentLanguage == Constants.LANG_URDU) "اردو" else "English"
        
        binding.layoutLanguage.setOnClickListener {
            showLanguageDialog()
        }
    }
    
    private fun showLanguageDialog() {
        val languages = arrayOf("English", "اردو (Urdu)")
        val currentLanguage = preferenceManager.language
        val selectedIndex = if (currentLanguage == Constants.LANG_URDU) 1 else 0
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Language")
            .setSingleChoiceItems(languages, selectedIndex) { dialog, which ->
                val newLanguage = if (which == 1) Constants.LANG_URDU else Constants.LANG_ENGLISH
                preferenceManager.language = newLanguage
                binding.textLanguageValue.text = languages[which]
                Toast.makeText(
                    requireContext(),
                    "Language changed to ${languages[which]}. Restart app to apply.",
                    Toast.LENGTH_LONG
                ).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setupNotificationToggle() {
        binding.switchNotifications.isChecked = preferenceManager.notificationsEnabled
        
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.notificationsEnabled = isChecked
            Toast.makeText(
                requireContext(),
                if (isChecked) "Notifications enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun setupAboutSection() {
        binding.layoutAbout.setOnClickListener {
            showAboutDialog()
        }
        
        binding.layoutPrivacy.setOnClickListener {
            showPrivacyDialog()
        }
        
        binding.layoutTerms.setOnClickListener {
            showTermsDialog()
        }
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("About ${Constants.APP_NAME}")
            .setMessage("""
                ${Constants.APP_NAME} v${Constants.APP_VERSION}
                
                Pakistan's #1 Vehicle Marketplace
                
                Buy and sell cars, bikes, and other vehicles with ease.
                
                Powered by ApniVehicle v2.0
                Enhanced by AI
                
                © 2024 ApniVehicle. All rights reserved.
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showPrivacyDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Privacy Policy")
            .setMessage("""
                Privacy Policy
                
                We value your privacy and are committed to protecting your personal information.
                
                Data Collection:
                • We collect only necessary information for app functionality
                • Your data is stored locally on your device
                • We do not share your data with third parties
                
                Security:
                • All sensitive data is encrypted
                • We use industry-standard security practices
                
                Your Rights:
                • You can delete your account anytime
                • You can export your data
                • You can opt-out of notifications
                
                For more information, contact us at privacy@apnivehicle.com
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showTermsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Terms & Conditions")
            .setMessage("""
                Terms & Conditions
                
                By using ApniVehicle, you agree to:
                
                1. Provide accurate information in your listings
                2. Not post fraudulent or misleading ads
                3. Respect other users and communicate professionally
                4. Comply with local laws and regulations
                5. Not use the app for illegal activities
                
                We reserve the right to:
                • Remove inappropriate content
                • Suspend or ban accounts violating terms
                • Update these terms at any time
                
                For questions, contact us at support@apnivehicle.com
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun setupLogout() {
        binding.buttonLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                AuthRepository.logout()
                Toast.makeText(requireContext(), Constants.SUCCESS_LOGOUT, Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

