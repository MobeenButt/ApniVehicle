package com.example.apnivehicle.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.FragmentAddVehicleBinding
import com.example.apnivehicle.models.Vehicle
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.NotificationHelper

class AddVehicleFragment : Fragment() {

    private var _binding: FragmentAddVehicleBinding? = null
    private val binding get() = _binding!!
    
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.ivVehicleImage.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddVehicleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.buttonAddVehicle.setOnClickListener {
            val title = binding.inputTitle.text.toString().trim()
            val price = binding.inputPrice.text.toString().toLongOrNull()
            val city = binding.inputCity.text.toString().trim()
            val year = binding.inputYear.text.toString().toIntOrNull()
            val description = binding.inputDescription.text.toString().trim()

            if (title.isBlank() || price == null || city.isBlank() || year == null || description.isBlank()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val vehicle = Vehicle(
                title = title,
                price = price,
                city = city,
                year = year,
                image = 0,
                imageUri = selectedImageUri?.toString(),
                description = description,
                isMyAd = true
            )

            VehicleRepository.addVehicle(vehicle)
            NotificationHelper(requireContext()).showVehicleAdded(title)
            Toast.makeText(requireContext(), "Vehicle added", Toast.LENGTH_SHORT).show()

            // Clear form
            binding.inputTitle.text?.clear()
            binding.inputPrice.text?.clear()
            binding.inputCity.text?.clear()
            binding.inputYear.text?.clear()
            binding.inputDescription.text?.clear()
            binding.ivVehicleImage.setImageResource(R.drawable.ic_car_rental)
            selectedImageUri = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

