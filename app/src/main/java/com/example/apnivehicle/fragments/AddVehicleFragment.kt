package com.example.apnivehicle.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.apnivehicle.R
import com.example.apnivehicle.databinding.FragmentAddVehicleBinding
import com.example.apnivehicle.models.Vehicle
import com.example.apnivehicle.models.VehicleType
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.repository.VehicleDataRepository
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.Constants
import com.example.apnivehicle.utils.FileManager
import com.example.apnivehicle.utils.NetworkMonitor
import com.example.apnivehicle.utils.NotificationHelper
import com.example.apnivehicle.utils.ValidationUtils
import com.example.apnivehicle.utils.setDebouncedClickListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddVehicleFragment : Fragment() {

    private var _binding: FragmentAddVehicleBinding? = null
    private val binding get() = _binding!!

    private val selectedImageUris = mutableListOf<Uri>()
    private lateinit var vehicleDataRepository: VehicleDataRepository

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

        vehicleDataRepository = VehicleDataRepository(requireContext())

        setupDropdowns()
        setupImagePicker()
        setupSubmitButton()
        playEntryAnimation()

        // Show offline warning if needed
        NetworkMonitor.isOnline.observe(viewLifecycleOwner) { online ->
            val b = _binding ?: return@observe
            if (b.buttonAddVehicle.text != "Uploading...") {
                b.buttonAddVehicle.isEnabled = online
            }
            if (!online) {
                Snackbar.make(b.root, "You are offline. Cannot post ad.", Snackbar.LENGTH_LONG).show()
            }
        }

        // Load makes from API
        lifecycleScope.launch {
            try {
                val makes = vehicleDataRepository.getMakes()
                val b = _binding ?: return@launch
                val makeAdapter = ArrayAdapter(requireContext(), R.layout.list_item, makes)
                b.spinnerBrand.setAdapter(makeAdapter)
            } catch (_: Exception) {
                if (_binding != null) setupDropdowns()
            }
        }
    }

    private fun playEntryAnimation() {
        val root = _binding?.root ?: return
        // Stagger each direct child card in
        val container = (root as? androidx.core.widget.NestedScrollView)
            ?.getChildAt(0) as? android.widget.LinearLayout ?: return

        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            child.alpha = 0f
            child.translationY = 40f
            val delay = (i * 60).toLong()
            val fadeIn = ObjectAnimator.ofFloat(child, "alpha", 0f, 1f).setDuration(350)
            val slide = ObjectAnimator.ofFloat(child, "translationY", 40f, 0f).setDuration(380)
            slide.interpolator = DecelerateInterpolator()
            AnimatorSet().apply {
                playTogether(fadeIn, slide)
                startDelay = delay
                start()
            }
        }
    }

    private fun setupDropdowns() {
        // Vehicle Types
        val types = VehicleType.values().map { type ->
            when (type) {
                VehicleType.CAR -> "Car"
                VehicleType.MOTORCYCLE -> "Motorcycle / Bike"
                VehicleType.TRUCK -> "Truck"
                VehicleType.BUS -> "Bus / Coaster"
                VehicleType.VAN -> "Van / Minivan"
                VehicleType.JEEP -> "Jeep / SUV"
                VehicleType.AUTO_RICKSHAW -> "Auto Rickshaw"
                VehicleType.TRACTOR -> "Tractor / Agricultural"
            }
        }
        binding.spinnerType.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, types))

        // Cities
        binding.spinnerCity.setAdapter(
            ArrayAdapter(requireContext(), R.layout.list_item, Constants.PAKISTANI_CITIES)
        )

        // Fuel Types
        binding.spinnerFuel.setAdapter(
            ArrayAdapter(requireContext(), R.layout.list_item, Constants.FUEL_TYPES)
        )

        // Transmission
        binding.spinnerTransmission.setAdapter(
            ArrayAdapter(requireContext(), R.layout.list_item, Constants.TRANSMISSION_TYPES)
        )

        // Condition
        binding.spinnerCondition.setAdapter(
            ArrayAdapter(requireContext(), R.layout.list_item, Constants.VEHICLE_CONDITIONS)
        )

        // Brand — start with curated Pakistan list
        binding.spinnerBrand.setAdapter(
            ArrayAdapter(requireContext(), R.layout.list_item, Constants.VEHICLE_MAKES)
        )

        // When brand changes, immediately populate models from Constants (no network needed),
        // then refresh in background from API if available.
        binding.spinnerBrand.setOnItemClickListener { _, _, _, _ ->
            val selectedBrand = binding.spinnerBrand.text.toString().trim()
            if (selectedBrand.isNotBlank()) {
                loadModelsForBrand(selectedBrand)
            }
        }
    }

    private fun loadModelsForBrand(brand: String) {
        // Step 1: Instantly populate from local Constants (no delay)
        val localModels = vehicleDataRepository.getLocalModels(brand)
        if (localModels.isNotEmpty()) {
            val modelAdapter = ArrayAdapter(requireContext(), R.layout.list_item, localModels)
            _binding?.spinnerBrand?.let { /* brand already set */ }
            // We don't have a spinner_model in the current layout,
            // but title hint can guide the user. Models are reflected in the title field.
        }

        // Step 2: Fetch from API/cache in background to keep brand list fresh
        lifecycleScope.launch {
            try {
                vehicleDataRepository.getMakes() // refreshes cache
            } catch (_: Exception) { /* silent — Constants already loaded */ }
        }
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
            // Clear any tint before Glide loads so the real image is never tinted
            binding.ivVehicleImage.clearColorFilter()
            Glide.with(this)
                .load(selectedImageUris[0])
                .override(800, 600)
                .centerCrop()
                .placeholder(R.drawable.ic_car_rental)
                .into(binding.ivVehicleImage)
            binding.textImageCount.text = "${selectedImageUris.size} image(s) selected"
            binding.textImageCount.visibility = View.VISIBLE
            binding.btnClearImages.visibility = View.VISIBLE
            binding.layoutImagePlaceholder.visibility = View.GONE
        } else {
            binding.ivVehicleImage.clearColorFilter()
            binding.ivVehicleImage.setImageResource(R.drawable.ic_car_rental)
            binding.textImageCount.visibility = View.GONE
            binding.btnClearImages.visibility = View.GONE
            binding.layoutImagePlaceholder.visibility = View.VISIBLE
        }
    }

    private fun setupSubmitButton() {
        binding.buttonAddVehicle.setDebouncedClickListener(1500L) {
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
        binding.buttonAddVehicle.text = "Uploading..."
        binding.progressSubmit?.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val uploadedUrls = mutableListOf<String>()

                if (NetworkMonitor.isCurrentlyOnline()) {
                    // Upload to Firebase Storage
                    val storage = FirebaseStorage.getInstance()
                    for (uri in selectedImageUris) {
                        try {
                            val ref = storage.reference.child("vehicles/${System.currentTimeMillis()}_${uri.lastPathSegment}")
                            ref.putFile(uri).await()
                            val downloadUrl = ref.downloadUrl.await().toString()
                            uploadedUrls.add(downloadUrl)
                        } catch (e: Exception) {
                            android.util.Log.e("AddVehicle", "Storage upload failed, using local", e)
                            val localPath = FileManager.saveImageFromUri(uri)
                            if (localPath != null) uploadedUrls.add(localPath)
                        }
                    }
                } else {
                    // Offline: save locally
                    for (uri in selectedImageUris) {
                        val localPath = FileManager.saveImageFromUri(uri)
                        if (localPath != null) uploadedUrls.add(localPath)
                    }
                }

                // Fragment may have been destroyed while we were uploading — check before touching views
                if (_binding == null) return@launch

                if (uploadedUrls.isEmpty()) {
                    Snackbar.make(binding.root, "Failed to save images. Please try again.", Snackbar.LENGTH_SHORT).show()
                    return@launch
                }

                val vehicleType = when (typeStr) {
                    "Car"                    -> VehicleType.CAR
                    "Motorcycle / Bike"      -> VehicleType.MOTORCYCLE
                    "Truck"                  -> VehicleType.TRUCK
                    "Bus / Coaster"          -> VehicleType.BUS
                    "Van / Minivan"          -> VehicleType.VAN
                    "Jeep / SUV"             -> VehicleType.JEEP
                    "Auto Rickshaw"          -> VehicleType.AUTO_RICKSHAW
                    "Tractor / Agricultural" -> VehicleType.TRACTOR
                    else -> try { VehicleType.valueOf(typeStr) } catch (_: Exception) { VehicleType.CAR }
                }
                val currentUser = AuthRepository.getCurrentUser()
                val sellerId = currentUser?.id ?: ""
                val sellerPhone = currentUser?.phoneNumber ?: ""

                val vehicle = Vehicle(
                    title = title, price = price!!, city = city, year = year!!,
                    type = vehicleType, brand = brand, fuelType = fuelType,
                    transmission = transmission, condition = condition, mileage = mileage!!,
                    imageUri = uploadedUrls[0], imageList = uploadedUrls.toMutableList(),
                    description = description, isMyAd = true,
                    sellerId = sellerId, sellerPhone = sellerPhone
                )

                VehicleRepository.addVehicleAsync(vehicle)

                // Only show UI feedback if fragment is still attached
                if (_binding != null) {
                    NotificationHelper(requireContext()).showVehicleAdded(title)
                    Snackbar.make(binding.root, Constants.SUCCESS_VEHICLE_ADDED, Snackbar.LENGTH_LONG).show()
                    clearForm()
                }
            } finally {
                // Re-enable button only if view still exists
                _binding?.let {
                    it.buttonAddVehicle.isEnabled = true
                    it.buttonAddVehicle.text = "Post Ad Now"
                    it.progressSubmit?.visibility = View.GONE
                }
            }
        }
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
