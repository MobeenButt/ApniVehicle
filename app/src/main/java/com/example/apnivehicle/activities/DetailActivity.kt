package com.example.apnivehicle.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.apnivehicle.R
import com.example.apnivehicle.adapters.ImagePagerAdapter
import com.example.apnivehicle.databinding.ActivityDetailBinding
import com.example.apnivehicle.models.Review
import com.example.apnivehicle.repository.AuthRepository
import com.example.apnivehicle.repository.ChatRepository
import com.example.apnivehicle.repository.ReviewRepository
import com.example.apnivehicle.repository.VehicleRepository
import com.example.apnivehicle.utils.AnalyticsManager
import com.example.apnivehicle.utils.NetworkMonitor
import com.example.apnivehicle.utils.NotificationHelper
import com.example.apnivehicle.utils.setDebouncedClickListener
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_VEHICLE_ID = "extra_vehicle_id"
        private val priceFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PK"))
    }

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityDetailBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val vehicleId = intent.getStringExtra(EXTRA_VEHICLE_ID)
            if (vehicleId.isNullOrEmpty()) { finish(); return }

            val vehicle = VehicleRepository.getVehicleById(vehicleId)
            if (vehicle == null) { Toast.makeText(this, "Vehicle not found", Toast.LENGTH_SHORT).show(); finish(); return }

            try {
                VehicleRepository.incrementViewCount(vehicle.id)
                AnalyticsManager.trackVehicleView(this, vehicle.id)
            } catch (e: Exception) { android.util.Log.e("DetailActivity", "Analytics error", e) }

            setupVehicleDetails(vehicle)
            setupButtons(vehicle)
            observeNetwork()
        } catch (e: Exception) {
            android.util.Log.e("DetailActivity", "Fatal error", e)
            Toast.makeText(this, "Error loading vehicle details", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun observeNetwork() {
        NetworkMonitor.isOnline.observe(this) { online ->
            binding.offlineBanner?.visibility = if (online) View.GONE else View.VISIBLE
        }
    }

    private fun setupVehicleDetails(vehicle: com.example.apnivehicle.models.Vehicle) {
        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = vehicle.title
        } catch (e: Exception) { android.util.Log.e("DetailActivity", "Toolbar error", e) }

        try {
            if (vehicle.imageList.isNotEmpty()) {
                binding.imageViewPager.visibility = View.VISIBLE
                binding.imageVehicle.visibility = View.GONE
                val adapter = ImagePagerAdapter(vehicle.imageList)
                binding.imageViewPager.adapter = adapter
                if (vehicle.imageList.size > 1) {
                    binding.imageIndicator.visibility = View.VISIBLE
                    TabLayoutMediator(binding.imageIndicator, binding.imageViewPager) { _, _ -> }.attach()
                } else binding.imageIndicator.visibility = View.GONE
            } else {
                binding.imageViewPager.visibility = View.GONE
                binding.imageVehicle.visibility = View.VISIBLE
                binding.imageIndicator.visibility = View.GONE
                val uri = vehicle.imageUri
                if (!uri.isNullOrEmpty()) {
                    try {
                        com.bumptech.glide.Glide.with(this).load(uri)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.ic_car_rental)
                            .into(binding.imageVehicle)
                    } catch (_: Exception) { binding.imageVehicle.setImageResource(R.drawable.ic_car_rental) }
                } else binding.imageVehicle.setImageResource(R.drawable.ic_car_rental)
            }
        } catch (e: Exception) {
            binding.imageVehicle.setImageResource(R.drawable.ic_car_rental)
        }

        binding.textTitle.text = vehicle.title
        binding.textPrice.text = priceFormatter.format(vehicle.price)
        binding.textDescription.text = vehicle.description
        binding.textCity.text = vehicle.city

        try {
            val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.textDate.text = dateFormat.format(java.util.Date(vehicle.createdAt))
        } catch (_: Exception) { binding.textDate.text = "N/A" }

        binding.quickYear.text = vehicle.year.toString()
        binding.quickFuel.text = vehicle.fuelType
        binding.quickTrans.text = vehicle.transmission
        binding.detailEngine.text = "Engine: ${vehicle.fuelType}"
        binding.detailColor.text = "Color: ${vehicle.color.ifEmpty { "N/A" }}"
        binding.detailAssembly.text = "Condition: ${vehicle.condition}"
        binding.detailMileage.text = "KM Driven: ${NumberFormat.getNumberInstance(Locale.getDefault()).format(vehicle.mileage)} km"

        // Offline stale data indicator
        if (!NetworkMonitor.isCurrentlyOnline() && vehicle.lastSyncedAt > 0) {
            binding.textStaleData?.visibility = View.VISIBLE
            binding.textStaleData?.text = "Last updated: ${com.example.apnivehicle.utils.FormatUtils.getRelativeTime(vehicle.lastSyncedAt)}"
        }

        // Seller rating
        binding.textSellerRating?.text = "Seller Rating: ${vehicle.sellerRating}/5.0 (${vehicle.sellerReviewCount} reviews)"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupButtons(vehicle: com.example.apnivehicle.models.Vehicle) {
        // In-app Chat button - with debounce to prevent rapid multiple clicks
        binding.btnChat.setDebouncedClickListener(1000L) {
            if (!NetworkMonitor.isCurrentlyOnline()) {
                Toast.makeText(this, "You are offline. Cannot start chat.", Toast.LENGTH_SHORT).show()
                return@setDebouncedClickListener
            }
            val currentUser = AuthRepository.getCurrentUser()
            if (currentUser == null) {
                Toast.makeText(this, "Please login to chat", Toast.LENGTH_SHORT).show()
                return@setDebouncedClickListener
            }
            if (currentUser.id == vehicle.sellerId) {
                Toast.makeText(this, "This is your own listing", Toast.LENGTH_SHORT).show()
                return@setDebouncedClickListener
            }
            
            // Disable button to prevent multiple clicks during coroutine execution
            binding.btnChat.isEnabled = false
            
            lifecycleScope.launch {
                try {
                    val result = ChatRepository.getOrCreateChat(
                        currentUserId = currentUser.id,
                        sellerId = vehicle.sellerId,
                        vehicleId = vehicle.id,
                        vehicleTitle = vehicle.title
                    )
                    result.onSuccess { chat ->
                        val safeChatId = chat.chatId.ifBlank {
                            ChatRepository.buildChatId(currentUser.id, vehicle.sellerId, vehicle.id)
                        }
                        AnalyticsManager.trackContactClick(this@DetailActivity, vehicle.id)
                        val intent = Intent(this@DetailActivity, ChatActivity::class.java).apply {
                            putExtra(ChatActivity.EXTRA_CHAT_ID, safeChatId)
                            putExtra(ChatActivity.EXTRA_VEHICLE_TITLE, vehicle.title)
                            putExtra(ChatActivity.EXTRA_VEHICLE_ID, vehicle.id)
                            putExtra(ChatActivity.EXTRA_SELLER_ID, vehicle.sellerId)
                        }
                        startActivity(intent)
                        // Prompt review after contact
                        maybePromptReview(vehicle)
                    }
                    result.onFailure {
                        Toast.makeText(this@DetailActivity, "Could not open chat: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    // Re-enable button after operation completes
                    binding.btnChat.isEnabled = true
                }
            }
        }

        // Call button - with debounce to prevent rapid multiple clicks
        binding.btnCall.setDebouncedClickListener(1000L) {
            val phoneNumber = getSellerPhoneNumber(vehicle)
            if (!phoneNumber.isNullOrBlank()) {
                AnalyticsManager.trackContactClick(this, vehicle.id)
                val dialIntent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phoneNumber") }
                if (dialIntent.resolveActivity(packageManager) != null) startActivity(dialIntent)
                else Toast.makeText(this, "No phone app found", Toast.LENGTH_SHORT).show()
                maybePromptReview(vehicle)
            } else {
                Toast.makeText(this, "Seller's phone number not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun maybePromptReview(vehicle: com.example.apnivehicle.models.Vehicle) {
        val currentUser = AuthRepository.getCurrentUser() ?: return
        if (currentUser.id == vehicle.sellerId) return
        lifecycleScope.launch {
            val alreadyReviewed = ReviewRepository.hasUserReviewedSeller(currentUser.id, vehicle.sellerId)
            if (!alreadyReviewed) {
                runOnUiThread { showReviewDialog(vehicle) }
            }
        }
    }

    private fun showReviewDialog(vehicle: com.example.apnivehicle.models.Vehicle) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_review, null)
        val ratingBar = dialogView.findViewById<android.widget.RatingBar>(R.id.rating_bar_input)
        val inputText = dialogView.findViewById<android.widget.EditText>(R.id.input_review_text)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Rate this Seller")
            .setMessage("How was your experience with ${vehicle.title}?")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val rating = ratingBar.rating
                val text = inputText.text.toString().trim()
                if (rating == 0f) { Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                val currentUser = AuthRepository.getCurrentUser() ?: return@setPositiveButton
                val review = Review(
                    reviewerId = currentUser.id,
                    reviewerName = currentUser.username,
                    targetId = vehicle.sellerId,
                    targetType = "seller",
                    rating = rating,
                    text = text
                )
                lifecycleScope.launch {
                    ReviewRepository.addReview(review)
                    Toast.makeText(this@DetailActivity, "Review submitted!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun getSellerPhoneNumber(vehicle: com.example.apnivehicle.models.Vehicle): String? {
        if (!vehicle.sellerPhone.isNullOrBlank()) return vehicle.sellerPhone
        if (vehicle.sellerId.isNotEmpty()) {
            val seller = AuthRepository.getUserById(vehicle.sellerId)
            if (!seller?.phoneNumber.isNullOrBlank()) return seller?.phoneNumber
        }
        return null
    }
}
