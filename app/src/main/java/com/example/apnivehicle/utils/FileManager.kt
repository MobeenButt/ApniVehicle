package com.example.apnivehicle.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.apnivehicle.models.User
import com.example.apnivehicle.models.Vehicle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.util.UUID

/**
 * Centralized file management for ApniVehicle
 * Handles JSON data storage and image file operations
 */
object FileManager {
    
    private const val TAG = "FileManager"
    private const val USERS_FILE = "users.json"
    private const val VEHICLES_FILE = "vehicles.json"
    private const val FAVORITES_FILE = "favorites.json"
    private const val IMAGES_DIR = "vehicle_images"
    
    private val gson = Gson()
    private lateinit var context: Context
    
    fun init(context: Context) {
        this.context = context.applicationContext
        // Create images directory if it doesn't exist
        val imagesDir = File(context.filesDir, IMAGES_DIR)
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
    }
    
    // ===== JSON Operations =====
    
    /**
     * Save users list to JSON file
     */
    fun saveUsers(users: List<User>): Boolean {
        return try {
            val file = File(context.filesDir, USERS_FILE)
            val writer = FileWriter(file)
            gson.toJson(users, writer)
            writer.close()
            Log.d(TAG, "Users saved successfully: ${users.size} users")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving users", e)
            false
        }
    }
    
    /**
     * Load users list from JSON file
     */
    fun loadUsers(): List<User> {
        return try {
            val file = File(context.filesDir, USERS_FILE)
            if (!file.exists()) {
                Log.d(TAG, "Users file doesn't exist, returning empty list")
                return emptyList()
            }
            
            val reader = FileReader(file)
            val type = object : TypeToken<List<User>>() {}.type
            val users: List<User> = gson.fromJson(reader, type)
            reader.close()
            Log.d(TAG, "Users loaded successfully: ${users.size} users")
            users
        } catch (e: Exception) {
            Log.e(TAG, "Error loading users", e)
            emptyList()
        }
    }
    
    /**
     * Save vehicles list to JSON file
     */
    fun saveVehicles(vehicles: List<Vehicle>): Boolean {
        return try {
            val file = File(context.filesDir, VEHICLES_FILE)
            val writer = FileWriter(file)
            gson.toJson(vehicles, writer)
            writer.close()
            Log.d(TAG, "Vehicles saved successfully: ${vehicles.size} vehicles")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving vehicles", e)
            false
        }
    }
    
    /**
     * Load vehicles list from JSON file
     */
    fun loadVehicles(): List<Vehicle> {
        return try {
            val file = File(context.filesDir, VEHICLES_FILE)
            if (!file.exists()) {
                Log.d(TAG, "Vehicles file doesn't exist, returning empty list")
                return emptyList()
            }
            
            val reader = FileReader(file)
            val type = object : TypeToken<List<Vehicle>>() {}.type
            val vehicles: List<Vehicle> = gson.fromJson(reader, type)
            reader.close()
            Log.d(TAG, "Vehicles loaded successfully: ${vehicles.size} vehicles")
            vehicles
        } catch (e: Exception) {
            Log.e(TAG, "Error loading vehicles", e)
            emptyList()
        }
    }
    
    /**
     * Save favorites (vehicle IDs) to JSON file
     */
    fun saveFavorites(favoriteIds: List<String>): Boolean {
        return try {
            val file = File(context.filesDir, FAVORITES_FILE)
            val writer = FileWriter(file)
            gson.toJson(favoriteIds, writer)
            writer.close()
            Log.d(TAG, "Favorites saved successfully: ${favoriteIds.size} favorites")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving favorites", e)
            false
        }
    }
    
    /**
     * Load favorites (vehicle IDs) from JSON file
     */
    fun loadFavorites(): List<String> {
        return try {
            val file = File(context.filesDir, FAVORITES_FILE)
            if (!file.exists()) {
                Log.d(TAG, "Favorites file doesn't exist, returning empty list")
                return emptyList()
            }
            
            val reader = FileReader(file)
            val type = object : TypeToken<List<String>>() {}.type
            val favorites: List<String> = gson.fromJson(reader, type)
            reader.close()
            Log.d(TAG, "Favorites loaded successfully: ${favorites.size} favorites")
            favorites
        } catch (e: Exception) {
            Log.e(TAG, "Error loading favorites", e)
            emptyList()
        }
    }
    
    // ===== Image Operations =====
    
    /**
     * Save image from URI to app storage
     * @param uri Source image URI
     * @return Saved file path or null if failed
     */
    fun saveImageFromUri(uri: Uri): String? {
        // Guard: ensure FileManager.init(context) has been called
        if (!this::context.isInitialized) {
            Log.e(TAG, "FileManager.saveImageFromUri called before init")
            return null
        }

        return try {
            // First pass: decode bounds only to calculate sample size
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { firstStream ->
                BitmapFactory.decodeStream(firstStream, null, boundsOptions)
            }

            // Calculate inSampleSize to downsample large images (target max 1920px)
            val sampleSize = calculateInSampleSize(boundsOptions, 1920, 1920)

            // Second pass: decode with sample size so full-resolution bitmap is never in memory
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }

            if (bitmap != null) {
                saveImage(bitmap)
            } else {
                Log.e(TAG, "Failed to decode bitmap from URI")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image from URI", e)
            null
        }
    }

    /**
     * Calculate inSampleSize to downsample images larger than requested dimensions.
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
    
    /**
     * Save bitmap image to app storage with compression
     * @param bitmap Image to save
     * @param quality Compression quality (0-100), default 80
     * @return Saved file path or null if failed
     */
    fun saveImage(bitmap: Bitmap, quality: Int = 80): String? {
        return try {
            // Generate unique filename
            val filename = "${UUID.randomUUID()}.jpg"
            val imagesDir = File(context.filesDir, IMAGES_DIR)
            val imageFile = File(imagesDir, filename)
            
            // Compress and save
            val outputStream = FileOutputStream(imageFile)
            
            // Resize if too large (max 1920x1920)
            val resizedBitmap = if (bitmap.width > 1920 || bitmap.height > 1920) {
                val ratio = minOf(1920f / bitmap.width, 1920f / bitmap.height)
                val newWidth = (bitmap.width * ratio).toInt()
                val newHeight = (bitmap.height * ratio).toInt()
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }
            
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()

            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
                bitmap.recycle()
            }
            
            val savedPath = imageFile.absolutePath
            Log.d(TAG, "Image saved successfully: $savedPath")
            savedPath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image", e)
            null
        }
    }
    
    /**
     * Load image from file path
     * @param path Image file path
     * @return Bitmap or null if failed
     */
    fun loadImage(path: String): Bitmap? {
        return try {
            val file = File(path)
            if (!file.exists()) {
                Log.w(TAG, "Image file doesn't exist: $path")
                return null
            }
            
            val bitmap = BitmapFactory.decodeFile(path)
            Log.d(TAG, "Image loaded successfully: $path")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image", e)
            null
        }
    }
    
    /**
     * Delete image file
     * @param path Image file path
     * @return true if deleted successfully
     */
    fun deleteImage(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "Image deleted successfully: $path")
                } else {
                    Log.w(TAG, "Failed to delete image: $path")
                }
                deleted
            } else {
                Log.w(TAG, "Image file doesn't exist: $path")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image", e)
            false
        }
    }
    
    /**
     * Delete multiple images
     * @param paths List of image file paths
     */
    fun deleteImages(paths: List<String>) {
        paths.forEach { path ->
            deleteImage(path)
        }
    }
    
    /**
     * Get total size of images directory in bytes
     */
    fun getImagesDirSize(): Long {
        return try {
            val imagesDir = File(context.filesDir, IMAGES_DIR)
            if (!imagesDir.exists()) return 0L
            
            var size = 0L
            imagesDir.listFiles()?.forEach { file ->
                size += file.length()
            }
            size
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating images directory size", e)
            0L
        }
    }
    
    /**
     * Clear all images (use with caution!)
     */
    fun clearAllImages(): Boolean {
        return try {
            val imagesDir = File(context.filesDir, IMAGES_DIR)
            if (!imagesDir.exists()) return true
            
            var success = true
            imagesDir.listFiles()?.forEach { file ->
                if (!file.delete()) {
                    success = false
                    Log.w(TAG, "Failed to delete: ${file.name}")
                }
            }
            Log.d(TAG, "Clear all images: ${if (success) "success" else "partial failure"}")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing images", e)
            false
        }
    }
    
    /**
     * Check if file exists
     */
    fun fileExists(filename: String): Boolean {
        val file = File(context.filesDir, filename)
        return file.exists()
    }
    
    /**
     * Delete JSON file
     */
    fun deleteFile(filename: String): Boolean {
        return try {
            val file = File(context.filesDir, filename)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: $filename", e)
            false
        }
    }
}
