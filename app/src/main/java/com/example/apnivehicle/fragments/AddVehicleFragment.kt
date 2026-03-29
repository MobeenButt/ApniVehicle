package com.example.apnivehicle.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.FragmentAddVehicleBinding
import com.example.apnivehicle.models.Vehicle
import com.example.apnivehicle.models.VehicleType
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
        
        setupDropdowns()

        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.buttonAddVehicle.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun setupDropdowns() {
        // Vehicle Types
        val types = VehicleType.values().map { it.name }
        val typeAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), R.layout.list_item, types)
        binding.spinnerType.setAdapter(typeAdapter)

        // Fuel Types
        val fuelTypes = listOf("Petrol", "Diesel", "CNG", "Hybrid", "Electric")
        val fuelAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), R.layout.list_item, fuelTypes)
        binding.spinnerFuel.setAdapter(fuelAdapter)
    }

    private fun validateAndSubmit() {
        val title = binding.inputTitle.text.toString().trim()
        val price = binding.inputPrice.text.toString().toLongOrNull()
        val city = binding.inputCity.text.toString().trim()
        val year = binding.inputYear.text.toString().toIntOrNull()
        val mileage = binding.inputMileage.text.toString().toIntOrNull() ?: 0
        val typeStr = binding.spinnerType.text.toString()
        val fuelType = binding.spinnerFuel.text.toString()
        val description = binding.inputDescription.text.toString().trim()

        if (title.isBlank() || price == null || city.isBlank() || year == null || typeStr.isBlank() || description.isBlank()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val type = try { VehicleType.valueOf(typeStr) } catch (_: Exception) { VehicleType.CAR }

        val vehicle = Vehicle(
            title = title,
            price = price,
            city = city,
            year = year,
            type = type,
            fuelType = fuelType,
            mileage = mileage,
            imageUri = selectedImageUri?.toString(),
            description = description,
            isMyAd = true
        )

        VehicleRepository.addVehicle(vehicle)
        NotificationHelper(requireContext()).showVehicleAdded(title)
        Toast.makeText(requireContext(), "Your ad has been posted successfully!", Toast.LENGTH_LONG).show()

        clearForm()
    }

    private fun clearForm() {
        binding.inputTitle.text?.clear()
        binding.inputPrice.text?.clear()
        binding.inputCity.text?.clear()
        binding.inputYear.text?.clear()
        binding.inputMileage.text?.clear()
        binding.spinnerType.text = null
        binding.spinnerFuel.text = null
        binding.inputDescription.text?.clear()
        binding.ivVehicleImage.setImageResource(R.drawable.ic_car_rental)
        selectedImageUri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
