package com.example.apnivehicle.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.apnivehicle.activities.LoginActivity
import com.example.apnivehicle.databinding.FragmentSettingsBinding
import com.example.apnivehicle.repository.AuthRepository

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

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
        
        val currentUser = AuthRepository.getCurrentUser()
        if (currentUser != null) {
            binding.textUserEmail.text = "Email: ${currentUser.email}"
            binding.textUsername.text = "Username: ${currentUser.username}"
        }
        
        binding.buttonLogout.setOnClickListener {
            AuthRepository.logout()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

