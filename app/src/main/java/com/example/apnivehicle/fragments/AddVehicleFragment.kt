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
import com.example.apnivehicle.utils.Constants
import com.example.apnivehicle.utils.FileManager
import com.example.apnivehicle.utils.NotificationHelper
import com.example.apnivehicle.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar

class AddVehicleFragment : Fragment() {

    private var _binding: FragmentAddVehicleBinding? = null
    private val binding get() = _binding!!
    
    private val selectedImageUris = mutableListOf<Uri>()

    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val remainingSlots = Constants.MAX_IMAGES - selectedImageUris.size
            val urisToAdd = uris.take(remainingSlots)
            selectedImageUris.addAll(urisToAdd)
            updateImagePreview()
            
            if (uris.size > remainingSlots) {
                Toast.makeText(
                    requireContext(),
                    "Maximum ${Constants.MAX_IMAGES} images allowed. ${uris.size - remainingSlots} images not added.",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
        setupImagePicker()
        setupSubmitButton()
    }

    private fun setupDropdowns() {
        // Vehicle Types
        val types = VehicleType.values().map { it.name }
        val typeAdapter = ArrayAdapter(requireContext(), R.layout.list_item, types)
        binding.spinnerType.setAdapter(typeAdapter)

        // Cities
        val cityAdapter = ArrayAdapter(requireContext(), R.layout.list_item, Constants.PAKISTANI_CITIES)
        binding.spinnerCity.setAdapter(cityAdapter)

        // Fuel Types
        val fuelAdapter = ArrayAdapter(requireContext(), R.layout.list_item, Constants.FUEL_TYPES)
        binding.spinnerFuel.setAdapter(fuelAdapter)
        
        // Transmission
        val transmissionAdapter = ArrayAdapter(requireContext(), R.layout.list_item, Constants.TRANSMISSION_TYPES)
        binding.spinnerTransmission.setAdapter(transmissionAdapter)
        
        // Condition
        val conditionAdapter = ArrayAdapter(requireContext(), R.layout.list_item, Constants.VEHICLE_CONDITIONS)
        binding.spinnerCondition.setAdapter(conditionAdapter)
        
        // Brand
        val brandAdapter = ArrayAdapter(requireContext(), R.layout.list_item, Constants.VEHICLE_MAKES)
        binding.spinnerBrand.setAdapter(brandAdapter)
    }

    private fun setupImagePicker() {
        binding.btnSelectImage.setOnClickListener {
            if (selectedImageUris.size >= Constants.MAX_IMAGES) {
                Toast.makeText(
                    requireContext(),
                    "Maximum ${Constants.MAX_IMAGES} images allowed",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                pickImagesLauncher.launch("image/*")
            }
        }
        
        binding.btnClearImages.setOnClickListener {
            selectedImageUris.clear()
            updateImagePreview()
        }
    }

    private fun updateImagePreview() {
        if (selectedImageUris.isNotEmpty()) {
            binding.ivVehicleImage.setImageURI(selectedImageUris[0])
            binding.textImageCount.text = "${selectedImageUris.size} image(s) selected"
            binding.textImageCount.visibility = View.VISIBLE
            binding.btnClearImages.visibility = View.VISIBLE
        } else {
            binding.ivVehicleImage.setImageResource(R.drawable.ic_car_rental)
            binding.textImageCount.visibility = View.GONE
            binding.btnClearImages.visibility = View.GONE
        }
    }

    private fun setupSubmitButton() {
        binding.buttonAddVehicle.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {
        // Get input values
        val title = binding.inputTitle.text.toString().trim()
        val priceStr = binding.inputPrice.text.toString().trim()
        val city = binding.spinnerCity.text.toString().trim()
        val yearStr = binding.inputYear.text.toString().trim()
        val mileageStr = binding.inputMileage.text.toString().trim()
        val typeStr = binding.spinnerType.text.toString()
        val brand = binding.spinnerBrand.text.toString().trim()
        val fuelType = binding.spinnerFuel.text.toString()
        val transmission = binding.spinnerTransmission.text.toString()
        val condition = binding.spinnerCondition.text.toString()
        val description = binding.inputDescription.text.toString().trim()

        // Validate title
        val titleValidation = ValidationUtils.validateVehicleTitle(title)
        if (!titleValidation.isValid) {
            binding.textInputLayoutTitle.error = titleValidation.errorMessage
            return
        } else {
            binding.textInputLayoutTitle.error = null
        }

        // Validate price
        val price = priceStr.toLongOrNull()
        val priceValidation = ValidationUtils.validatePrice(price)
        if (!priceValidation.isValid) {
            binding.textInputLayoutPrice.error = priceValidation.errorMessage
            return
        } else {
            binding.textInputLayoutPrice.error = null
        }

        // Validate city
        val cityValidation = ValidationUtils.validateCity(city)
        if (!cityValidation.isValid) {
            Snackbar.make(binding.root, cityValidation.errorMessage ?: "Invalid city", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Validate year
        val year = yearStr.toIntOrNull()
        val yearValidation = ValidationUtils.validateYear(year)
        if (!yearValidation.isValid) {
            binding.textInputLayoutYear.error = yearValidation.errorMessage
            return
        } else {
            binding.textInputLayoutYear.error = null
        }

        // Validate mileage
        val mileage = mileageStr.toIntOrNull()
        val mileageValidation = ValidationUtils.validateMileage(mileage)
        if (!mileageValidation.isValid) {
            binding.textInputLayoutMileage.error = mileageValidation.errorMessage
            return
        } else {
            binding.textInputLayoutMileage.error = null
        }

        // Validate description
        val descriptionValidation = ValidationUtils.validateDescription(description)
        if (!descriptionValidation.isValid) {
            binding.textInputLayoutDescription.error = descriptionValidation.errorMessage
            return
        } else {
            binding.textInputLayoutDescription.error = null
        }

        // Validate images
        val imageValidation = ValidationUtils.validateImages(selectedImageUris.size)
        if (!imageValidation.isValid) {
            Snackbar.make(binding.root, imageValidation.errorMessage ?: "Please add at least one image", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Validate required dropdowns
        if (typeStr.isBlank() || brand.isBlank() || fuelType.isBlank() || transmission.isBlank() || condition.isBlank()) {
            Snackbar.make(binding.root, "Please fill all required fields", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Show progress
        binding.buttonAddVehicle.isEnabled = false
        binding.buttonAddVehicle.text = "Saving..."

        // Save images
        val savedImagePaths = mutableListOf<String>()
        for (uri in selectedImageUris) {
            val savedPath = FileManager.saveImageFromUri(uri)
            if (savedPath != null) {
                savedImagePaths.add(savedPath)
            }
        }

        if (savedImagePaths.isEmpty()) {
            binding.buttonAddVehicle.isEnabled = true
            binding.buttonAddVehicle.text = "Add Vehicle"
            Snackbar.make(binding.root, "Failed to save images. Please try again.", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Parse vehicle type
        val type = try { VehicleType.valueOf(typeStr) } catch (_: Exception) { VehicleType.CAR }

        // Get current user info
        val currentUser = com.example.apnivehicle.repository.AuthRepository.getCurrentUser()
        val sellerId = currentUser?.id ?: ""
        val sellerPhone = currentUser?.phoneNumber ?: ""

        // Create vehicle
        val vehicle = Vehicle(
            title = title,
            price = price!!,
            city = city,
            year = year!!,
            type = type,
            brand = brand,
            fuelType = fuelType,
            transmission = transmission,
            condition = condition,
            mileage = mileage!!,
            imageUri = savedImagePaths[0],
            imageList = savedImagePaths,
            description = description,
            isMyAd = true,
            sellerId = sellerId,
            sellerPhone = sellerPhone
        )

        // Add to repository
        VehicleRepository.addVehicle(vehicle)
        NotificationHelper(requireContext()).showVehicleAdded(title)
        
        Snackbar.make(binding.root, Constants.SUCCESS_VEHICLE_ADDED, Snackbar.LENGTH_LONG).show()

        // Reset form
        clearForm()
        binding.buttonAddVehicle.isEnabled = true
        binding.buttonAddVehicle.text = "Add Vehicle"
    }

    private fun clearForm() {
        binding.inputTitle.text?.clear()
        binding.inputPrice.text?.clear()
        binding.spinnerCity.text = null
        binding.inputYear.text?.clear()
        binding.inputMileage.text?.clear()
        binding.spinnerType.text = null
        binding.spinnerBrand.text = null
        binding.spinnerFuel.text = null
        binding.spinnerTransmission.text = null
        binding.spinnerCondition.text = null
        binding.inputDescription.text?.clear()
        selectedImageUris.clear()
        updateImagePreview()
        
        // Clear errors
        binding.textInputLayoutTitle.error = null
        binding.textInputLayoutPrice.error = null
        binding.textInputLayoutYear.error = null
        binding.textInputLayoutMileage.error = null
        binding.textInputLayoutDescription.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
