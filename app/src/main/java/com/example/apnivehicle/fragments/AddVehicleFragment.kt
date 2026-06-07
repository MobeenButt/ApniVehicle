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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.apnivehicle.R
import com.example.apnivehicle.adapters.ImageThumbnailAdapter
import com.example.apnivehicle.databinding.FragmentAddVehicleBinding
import com.example.apnivehicle.models.Vehicle
import com.example.apnivehicle.models.VehicleType
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.repository.VehicleDataRepository
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.Constants
import com.example.apnivehicle.utils.ImgBBUploader
import com.example.apnivehicle.utils.NetworkMonitor
import com.example.apnivehicle.utils.NotificationHelper
import com.example.apnivehicle.utils.ValidationUtils
import com.example.apnivehicle.utils.setDebouncedClickListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddVehicleFragment : Fragment() {

    private var _binding: FragmentAddVehicleBinding? = null
    private val binding get() = _binding!!

    private val selectedImageUris = mutableListOf<Uri>()
    private lateinit var vehicleDataRepository: VehicleDataRepository
    private lateinit var thumbnailAdapter: ImageThumbnailAdapter

    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val remainingSlots = Constants.MAX_IMAGES - selectedImageUris.size
            val urisToAdd = uris.take(remainingSlots)
            selectedImageUris.addAll(urisToAdd)
            updateImagePreview()
            thumbnailAdapter.notifyDataSetChanged()
            if (uris.size > remainingSlots) {
                Toast.makeText(
                    requireContext(),
                    "Max ${Constants.MAX_IMAGES} images. ${uris.size - remainingSlots} not added.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddVehicleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vehicleDataRepository = VehicleDataRepository(requireContext())
        setupThumbnailRecycler()
        setupDropdowns()
        setupImagePicker()
        setupSubmitButton()
        playEntryAnimation()

        NetworkMonitor.isOnline.observe(viewLifecycleOwner) { online ->
            val b = _binding ?: return@observe
            // Keep button always enabled — offline posts save locally as fallback
            b.buttonAddVehicle.isEnabled = true
            if (!online) {
                Snackbar.make(b.root,
                    "You are offline. Images will be saved locally and won't be visible on other devices.",
                    Snackbar.LENGTH_LONG).show()
            }
        }

        lifecycleScope.launch {
            try {
                val makes = vehicleDataRepository.getMakes()
                val b = _binding ?: return@launch
                b.spinnerBrand.setAdapter(
                    ArrayAdapter(requireContext(), R.layout.list_item, makes)
                )
            } catch (_: Exception) { /* Constants fallback already set */ }
        }
    }

    // ── Thumbnail strip ───────────────────────────────────────────────────────

    private fun setupThumbnailRecycler() {
        thumbnailAdapter = ImageThumbnailAdapter(
            uris = selectedImageUris,
            onRemove = { position ->
                selectedImageUris.removeAt(position)
                thumbnailAdapter.notifyItemRemoved(position)
                thumbnailAdapter.notifyItemRangeChanged(position, selectedImageUris.size)
                updateImagePreview()
            },
            onClick = { position ->
                // Show tapped image as preview
                if (position < selectedImageUris.size) {
                    binding.ivVehicleImage.clearColorFilter()
                    Glide.with(this)
                        .load(selectedImageUris[position])
                        .centerCrop()
                        .into(binding.ivVehicleImage)
                }
            }
        )
        binding.rvImageThumbnails.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvImageThumbnails.adapter = thumbnailAdapter
    }

    // ── Entry animation ───────────────────────────────────────────────────────

    private fun playEntryAnimation() {
        val root = _binding?.root ?: return
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
            AnimatorSet().apply { playTogether(fadeIn, slide); startDelay = delay; start() }
        }
    }

    // ── Dropdowns ─────────────────────────────────────────────────────────────

    private fun setupDropdowns() {
        val types = VehicleType.values().map { it.toDisplayName() }
        binding.spinnerType.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, types))
        binding.spinnerCity.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, Constants.PAKISTANI_CITIES))
        binding.spinnerFuel.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, Constants.FUEL_TYPES))
        binding.spinnerTransmission.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, Constants.TRANSMISSION_TYPES))
        binding.spinnerCondition.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, Constants.VEHICLE_CONDITIONS))
        binding.spinnerBrand.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, Constants.VEHICLE_MAKES))
        binding.spinnerBrand.setOnItemClickListener { _, _, _, _ ->
            val brand = binding.spinnerBrand.text.toString().trim()
            if (brand.isNotBlank()) {
                lifecycleScope.launch {
                    try { vehicleDataRepository.getMakes() } catch (_: Exception) {}
                }
            }
        }
    }

    // ── Image picker ──────────────────────────────────────────────────────────

    private fun setupImagePicker() {
        binding.btnSelectImage.setOnClickListener {
            if (selectedImageUris.size >= Constants.MAX_IMAGES) {
                Toast.makeText(requireContext(), "Maximum ${Constants.MAX_IMAGES} photos allowed", Toast.LENGTH_SHORT).show()
            } else {
                pickImagesLauncher.launch("image/*")
            }
        }
        // Tap placeholder to open picker too
        binding.layoutImagePlaceholder.setOnClickListener {
            if (selectedImageUris.size < Constants.MAX_IMAGES) pickImagesLauncher.launch("image/*")
        }
        binding.btnClearImages.setOnClickListener {
            selectedImageUris.clear()
            thumbnailAdapter.notifyDataSetChanged()
            updateImagePreview()
        }
    }

    private fun updateImagePreview() {
        val count = selectedImageUris.size
        binding.textImageCount.text = "$count / ${Constants.MAX_IMAGES}"

        if (count > 0) {
            binding.ivVehicleImage.clearColorFilter()
            Glide.with(this)
                .load(selectedImageUris[0])
                .centerCrop()
                .placeholder(R.drawable.ic_car_rental)
                .into(binding.ivVehicleImage)
            binding.layoutImagePlaceholder.visibility = View.GONE
            binding.rvImageThumbnails.visibility = View.VISIBLE
            binding.btnClearImages.visibility = View.VISIBLE
        } else {
            binding.ivVehicleImage.clearColorFilter()
            binding.ivVehicleImage.setImageResource(R.drawable.ic_car_rental)
            binding.layoutImagePlaceholder.visibility = View.VISIBLE
            binding.rvImageThumbnails.visibility = View.GONE
            binding.btnClearImages.visibility = View.GONE
        }
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    private fun setupSubmitButton() {
        binding.buttonAddVehicle.setDebouncedClickListener(1500L) { validateAndSubmit() }
    }

    private fun validateAndSubmit() {
        val title        = binding.inputTitle.text.toString().trim()
        val priceStr     = binding.inputPrice.text.toString().trim()
        val city         = binding.spinnerCity.text.toString().trim()
        val yearStr      = binding.inputYear.text.toString().trim()
        val mileageStr   = binding.inputMileage.text.toString().trim()
        val typeStr      = binding.spinnerType.text.toString()
        val brand        = binding.spinnerBrand.text.toString().trim()
        val fuelType     = binding.spinnerFuel.text.toString()
        val transmission = binding.spinnerTransmission.text.toString()
        val condition    = binding.spinnerCondition.text.toString()
        val description  = binding.inputDescription.text.toString().trim()

        // Field validation
        if (!validate(title, priceStr, city, yearStr, mileageStr,
                typeStr, brand, fuelType, transmission, condition, description)) return

        val price   = priceStr.toLong()
        val year    = yearStr.toInt()
        val mileage = mileageStr.toInt()

        setLoadingState(true, 0, selectedImageUris.size)

        lifecycleScope.launch {
            try {
                val uploadedUrls = uploadImages()

                if (_binding == null) return@launch
                if (uploadedUrls.isEmpty()) {
                    Snackbar.make(binding.root, "Failed to save images. Please try again.", Snackbar.LENGTH_LONG).show()
                    return@launch
                }

                val currentUser  = AuthRepository.getCurrentUser()
                val vehicleType  = typeStr.toVehicleType()
                val vehicle = Vehicle(
                    title        = title,
                    price        = price,
                    city         = city,
                    year         = year,
                    type         = vehicleType,
                    brand        = brand,
                    fuelType     = fuelType,
                    transmission = transmission,
                    condition    = condition,
                    mileage      = mileage,
                    imageUri     = uploadedUrls[0],         // cover image
                    imageList    = uploadedUrls.toMutableList(), // ALL images
                    description  = description,
                    isMyAd       = true,
                    sellerId     = currentUser?.id ?: "",
                    sellerPhone  = currentUser?.phoneNumber ?: ""
                )

                VehicleRepository.addVehicleAsync(vehicle)

                if (_binding != null) {
                    NotificationHelper(requireContext()).showVehicleAdded(title)
                    Snackbar.make(binding.root, "✅ Ad posted! ${uploadedUrls.size} image(s) saved.", Snackbar.LENGTH_LONG).show()
                    clearForm()
                }
            } catch (e: Exception) {
                android.util.Log.e("AddVehicle", "Submit failed", e)
                if (_binding != null) {
                    Snackbar.make(binding.root, "Error: ${e.message ?: "Unknown error"}", Snackbar.LENGTH_LONG).show()
                }
            } finally {
                _binding?.let { setLoadingState(false, 0, 0) }
            }
        }
    }

    // ── Image upload ──────────────────────────────────────────────────────────

    /**
     * Upload all selected images to ImgBB.
     * Returns list of permanent https:// URLs (or local paths as fallback).
     *
     * Flow:
     *  compress → Base64 → POST to ImgBB → get URL → store in Firestore
     *  Any device can then load the image via Glide using the URL.
     */
    private suspend fun uploadImages(): List<String> {
        val total = selectedImageUris.size
        val vehicleTitle = withContext(Dispatchers.Main) {
            _binding?.inputTitle?.text?.toString()?.trim()?.take(30) ?: "vehicle"
        }

        // Show overlay on Main thread
        withContext(Dispatchers.Main) {
            _binding?.layoutUploadProgress?.visibility = View.VISIBLE
            _binding?.tvUploadProgress?.text = "Preparing images…"
            _binding?.tvUploadPercent?.text = "0%"
            _binding?.uploadProgressIndicator?.progress = 0
        }

        // Upload all images — callback switches to Main for UI updates
        val results = ImgBBUploader.uploadAll(
            context      = requireContext(),
            uris         = selectedImageUris,
            vehicleTitle = vehicleTitle,
            onImageProgress = { imageIndex: Int, percent: Int ->
                val humanIndex = imageIndex + 1
                val overall    = ((imageIndex * 100 + percent) / total).coerceIn(0, 100)
                val progressText = if (percent < 100)
                    "Uploading image $humanIndex of $total…"
                else
                    "Image $humanIndex of $total done ✓"
                // Switch to Main thread to update UI
                lifecycleScope.launch(Dispatchers.Main) {
                    val b = _binding ?: return@launch
                    b.tvUploadProgress.text = progressText
                    b.tvUploadPercent.text = "$overall%"
                    b.uploadProgressIndicator.progress = overall
                }
            }
        )

        // Hide overlay
        withContext(Dispatchers.Main) {
            _binding?.layoutUploadProgress?.visibility = View.GONE
        }

        val urls        = results.map { result -> result.url }
        val localCount  = results.count { result -> result.isLocal }
        val remoteCount = results.count { result -> !result.isLocal }

        android.util.Log.d("AddVehicle",
            "Upload complete: $remoteCount to ImgBB, $localCount local. URLs: $urls")

        if (localCount > 0) {
            withContext(Dispatchers.Main) {
                val b = _binding ?: return@withContext
                Snackbar.make(
                    b.root,
                    "$localCount image(s) saved locally — connect to internet for full upload",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        return urls
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun validate(
        title: String, priceStr: String, city: String, yearStr: String,
        mileageStr: String, typeStr: String, brand: String,
        fuelType: String, transmission: String, condition: String, description: String
    ): Boolean {
        var ok = true

        val titleV = ValidationUtils.validateVehicleTitle(title)
        binding.textInputLayoutTitle.error = if (!titleV.isValid) { ok = false; titleV.errorMessage } else null

        val price = priceStr.toLongOrNull()
        val priceV = ValidationUtils.validatePrice(price)
        binding.textInputLayoutPrice.error = if (!priceV.isValid) { ok = false; priceV.errorMessage } else null

        val cityV = ValidationUtils.validateCity(city)
        if (!cityV.isValid) { ok = false; Snackbar.make(binding.root, cityV.errorMessage ?: "Select a city", Snackbar.LENGTH_SHORT).show() }

        val year = yearStr.toIntOrNull()
        val yearV = ValidationUtils.validateYear(year)
        binding.textInputLayoutYear.error = if (!yearV.isValid) { ok = false; yearV.errorMessage } else null

        val mileage = mileageStr.toIntOrNull()
        val mileV = ValidationUtils.validateMileage(mileage)
        binding.textInputLayoutMileage.error = if (!mileV.isValid) { ok = false; mileV.errorMessage } else null

        val descV = ValidationUtils.validateDescription(description)
        binding.textInputLayoutDescription.error = if (!descV.isValid) { ok = false; descV.errorMessage } else null

        val imageV = ValidationUtils.validateImages(selectedImageUris.size)
        if (!imageV.isValid) { ok = false; Snackbar.make(binding.root, imageV.errorMessage ?: "Add at least 1 photo", Snackbar.LENGTH_SHORT).show() }

        if (typeStr.isBlank() || brand.isBlank() || fuelType.isBlank() || transmission.isBlank() || condition.isBlank()) {
            ok = false
            Snackbar.make(binding.root, "Please fill all required fields", Snackbar.LENGTH_SHORT).show()
        }
        return ok
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private fun setLoadingState(loading: Boolean, done: Int, total: Int) {
        binding.buttonAddVehicle.isEnabled = !loading
        binding.buttonAddVehicle.text = if (loading) "Uploading…" else "Post Ad Now"
        binding.progressSubmit?.visibility = if (loading && total == 0) View.VISIBLE else View.GONE
        if (!loading) binding.layoutUploadProgress.visibility = View.GONE
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
        thumbnailAdapter.notifyDataSetChanged()
        updateImagePreview()
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

    // ── Extension helpers ─────────────────────────────────────────────────────

    private fun VehicleType.toDisplayName(): String = when (this) {
        VehicleType.CAR           -> "Car"
        VehicleType.MOTORCYCLE    -> "Motorcycle / Bike"
        VehicleType.TRUCK         -> "Truck"
        VehicleType.BUS           -> "Bus / Coaster"
        VehicleType.VAN           -> "Van / Minivan"
        VehicleType.JEEP          -> "Jeep / SUV"
        VehicleType.AUTO_RICKSHAW -> "Auto Rickshaw"
        VehicleType.TRACTOR       -> "Tractor / Agricultural"
    }

    private fun String.toVehicleType(): VehicleType = when (this) {
        "Car"                    -> VehicleType.CAR
        "Motorcycle / Bike"      -> VehicleType.MOTORCYCLE
        "Truck"                  -> VehicleType.TRUCK
        "Bus / Coaster"          -> VehicleType.BUS
        "Van / Minivan"          -> VehicleType.VAN
        "Jeep / SUV"             -> VehicleType.JEEP
        "Auto Rickshaw"          -> VehicleType.AUTO_RICKSHAW
        "Tractor / Agricultural" -> VehicleType.TRACTOR
        else -> try { VehicleType.valueOf(this) } catch (_: Exception) { VehicleType.CAR }
    }
}
