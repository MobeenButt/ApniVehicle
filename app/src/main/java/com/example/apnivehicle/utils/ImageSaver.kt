package com.example.apnivehicle.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Handles image compression and local storage for vehicle photos.
 *
 * Strategy (no Firebase Storage required):
 * 1. Decode URI → Bitmap (with inSampleSize to avoid OOM on high-res phones)
 * 2. Fix EXIF rotation so images always appear right-side up
 * 3. Scale down to targetWidthPx maintaining aspect ratio
 * 4. Compress to JPEG at qualityPercent
 * 5. Save to app-private files directory (vehicle_images/)
 * 6. Return absolute path — used as imageUri and in imageList on the Vehicle model
 */
object ImageSaver {

    private const val TAG = "ImageSaver"
    private const val IMAGES_DIR = "vehicle_images"

    /**
     * Compress and save a single image from a URI.
     *
     * @param context       Application context
     * @param uri           Content URI from the image picker
     * @param targetWidthPx Max width after scaling (height scales proportionally)
     * @param qualityPercent JPEG quality 0-100 (75 gives ~80-150KB per image)
     * @return Absolute file path of the saved image, or null if failed
     */
    fun compressAndSave(
        context: Context,
        uri: Uri,
        targetWidthPx: Int = 1024,
        qualityPercent: Int = 75
    ): String? {
        return try {
            val dir = File(context.filesDir, IMAGES_DIR).also { it.mkdirs() }
            val outFile = File(dir, "${UUID.randomUUID()}.jpg")

            // Step 1: Get image dimensions without loading full bitmap
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }

            // Step 2: Calculate sample size so we never load more than ~2x target
            val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, targetWidthPx)

            // Step 3: Decode with sample size
            val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            var bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, opts)
            } ?: return null

            // Step 4: Fix rotation from EXIF data (many phones save rotated JPEGs)
            bitmap = fixRotation(context, uri, bitmap)

            // Step 5: Scale to target width
            if (bitmap.width > targetWidthPx) {
                val ratio = targetWidthPx.toFloat() / bitmap.width
                val newH = (bitmap.height * ratio).toInt()
                val scaled = Bitmap.createScaledBitmap(bitmap, targetWidthPx, newH, true)
                if (scaled !== bitmap) bitmap.recycle()
                bitmap = scaled
            }

            // Step 6: Write compressed JPEG
            FileOutputStream(outFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, qualityPercent, out)
                out.flush()
            }
            bitmap.recycle()

            val path = outFile.absolutePath
            Log.d(TAG, "Saved ${outFile.length() / 1024}KB → $path")
            path

        } catch (e: Exception) {
            Log.e(TAG, "compressAndSave failed for $uri", e)
            null
        }
    }

    /**
     * Delete all images in a vehicle's image list from local storage.
     */
    fun deleteImages(paths: List<String>) {
        paths.forEach { path ->
            if (path.startsWith("/")) {          // local path
                try { File(path).delete() } catch (_: Exception) {}
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun calculateSampleSize(srcWidth: Int, srcHeight: Int, targetWidth: Int): Int {
        var size = 1
        while (srcWidth / (size * 2) >= targetWidth && srcHeight / (size * 2) > 0) {
            size *= 2
        }
        return size
    }

    private fun fixRotation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            inputStream.close()

            val rotation = when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90  -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (rotation == 0f) return bitmap

            val matrix = Matrix().apply { postRotate(rotation) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated !== bitmap) bitmap.recycle()
            rotated
        } catch (e: Exception) {
            Log.w(TAG, "EXIF rotation fix failed", e)
            bitmap
        }
    }
}
