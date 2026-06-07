package com.example.apnivehicle.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.example.apnivehicle.api.ImgBBClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Uploads vehicle images to ImgBB (free image hosting).
 *
 * Flow per image:
 *  1. Decode URI → Bitmap (with inSampleSize to avoid OOM)
 *  2. Fix EXIF rotation
 *  3. Scale to max 1024px wide
 *  4. Compress to JPEG → ByteArray
 *  5. Base64 encode
 *  6. POST to ImgBB API → get permanent https:// URL
 *  7. Return URL → stored in Vehicle.imageUri / Vehicle.imageList
 *  8. Firestore stores the URL → Glide loads it on any device
 *
 *  On failure: falls back to local file path (works on same device).
 */
object ImgBBUploader {

    private const val TAG = "ImgBBUploader"
    private const val TARGET_WIDTH_PX = 1024
    private const val JPEG_QUALITY = 78      // ~100-200KB per image after compression

    data class UploadResult(
        val url: String,          // permanent display URL from ImgBB
        val thumbUrl: String,     // 180px thumbnail URL
        val deleteUrl: String,    // URL to delete image from ImgBB later
        val isLocal: Boolean      // true = local fallback path, false = ImgBB URL
    )

    /**
     * Upload a single image URI to ImgBB.
     *
     * @param context    App context for content resolver
     * @param uri        Content URI from image picker
     * @param imageName  Optional name for ImgBB dashboard
     * @param onProgress Callback (0..100) called before and after upload
     * @return [UploadResult] with the permanent URL, or local fallback on failure
     */
    suspend fun upload(
        context: Context,
        uri: Uri,
        imageName: String = "vehicle_${System.currentTimeMillis()}",
        onProgress: ((Int) -> Unit)? = null
    ): UploadResult = withContext(Dispatchers.IO) {

        onProgress?.invoke(5)

        val base64 = try {
            compressToBase64(context, uri)
        } catch (e: Exception) {
            Log.e(TAG, "Compression failed for $uri", e)
            null
        }

        onProgress?.invoke(40)

        if (base64 == null) {
            val localPath = ImageSaver.compressAndSave(context, uri) ?: uri.toString()
            return@withContext UploadResult(localPath, localPath, "", isLocal = true)
        }

        return@withContext try {
            val response = ImgBBClient.api.uploadImage(
                apiKey = ImgBBClient.API_KEY,
                image  = base64,
                name   = imageName
            )

            onProgress?.invoke(95)

            if (response.success && response.data != null) {
                val data = response.data
                val url  = data.displayUrl.ifEmpty { data.url }
                Log.d(TAG, "ImgBB upload success: $url (${data.size / 1024}KB)")
                UploadResult(
                    url       = url,
                    thumbUrl  = data.thumb?.url ?: url,
                    deleteUrl = data.deleteUrl,
                    isLocal   = false
                )
            } else {
                Log.w(TAG, "ImgBB API returned success=false, status=${response.status}")
                fallbackLocal(context, uri)
            }
        } catch (e: Exception) {
            Log.e(TAG, "ImgBB upload failed, falling back to local", e)
            fallbackLocal(context, uri)
        }.also { onProgress?.invoke(100) }
    }

    /**
     * Upload multiple images with per-image progress reporting.
     *
     * @param onImageProgress  Called with (imageIndex, percentOfThisImage) — runs on IO thread,
     *                         caller must switch to Main to update UI.
     */
    suspend fun uploadAll(
        context: Context,
        uris: List<Uri>,
        vehicleTitle: String = "vehicle",
        onImageProgress: ((imageIndex: Int, percent: Int) -> Unit)? = null
    ): List<UploadResult> {
        val results = mutableListOf<UploadResult>()
        uris.forEachIndexed { index, uri ->
            val result = upload(
                context    = context,
                uri        = uri,
                imageName  = "${vehicleTitle}_img${index + 1}",
                onProgress = { percent: Int -> onImageProgress?.invoke(index, percent) }
            )
            results.add(result)
        }
        return results
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun compressToBase64(context: Context, uri: Uri): String? {
        // Decode bounds first (no memory allocation)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { s ->
            BitmapFactory.decodeStream(s, null, bounds)
        }
        if (bounds.outWidth <= 0) return null

        // Calculate sample size
        val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, TARGET_WIDTH_PX)
        val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }

        // Decode with sample size
        var bitmap = context.contentResolver.openInputStream(uri)?.use { s ->
            BitmapFactory.decodeStream(s, null, opts)
        } ?: return null

        // Fix rotation
        bitmap = fixRotation(context, uri, bitmap)

        // Scale to target width
        if (bitmap.width > TARGET_WIDTH_PX) {
            val ratio  = TARGET_WIDTH_PX.toFloat() / bitmap.width
            val newH   = (bitmap.height * ratio).toInt()
            val scaled = Bitmap.createScaledBitmap(bitmap, TARGET_WIDTH_PX, newH, true)
            if (scaled !== bitmap) bitmap.recycle()
            bitmap = scaled
        }

        // Compress to JPEG ByteArray
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)
        bitmap.recycle()

        val bytes = baos.toByteArray()
        Log.d(TAG, "Compressed to ${bytes.size / 1024}KB for ImgBB upload")

        // Base64 encode (NO_WRAP = no line breaks, required by ImgBB)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun fallbackLocal(context: Context, uri: Uri): UploadResult {
        val localPath = ImageSaver.compressAndSave(context, uri) ?: uri.toString()
        Log.w(TAG, "Using local fallback: $localPath")
        return UploadResult(localPath, localPath, "", isLocal = true)
    }

    private fun calculateSampleSize(srcW: Int, srcH: Int, targetW: Int): Int {
        var size = 1
        while (srcW / (size * 2) >= targetW && srcH / (size * 2) > 0) size *= 2
        return size
    }

    private fun fixRotation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val stream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exif   = ExifInterface(stream)
            stream.close()
            val angle = when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90  -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (angle == 0f) return bitmap
            val m = Matrix().apply { postRotate(angle) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
            if (rotated !== bitmap) bitmap.recycle()
            rotated
        } catch (_: Exception) { bitmap }
    }
}
