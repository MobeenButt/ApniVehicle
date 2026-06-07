package com.example.apnivehicle.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * ImgBB Image Hosting API — free, no billing required.
 *
 * How to get your free API key:
 *  1. Go to https://imgbb.com and create a free account
 *  2. Go to https://api.imgbb.com — you'll see your API key
 *  3. Copy it and paste it in ImgBBClient.API_KEY below
 *
 * Free tier limits:
 *  - Unlimited uploads
 *  - Max 32 MB per image
 *  - Images never expire (unless you delete them)
 *  - Direct links work everywhere (Glide, browsers, etc.)
 */
interface ImgBBApi {

    /**
     * Upload an image encoded as Base64.
     *
     * @param apiKey  Your ImgBB API key
     * @param image   Base64-encoded image string (no data:image/... prefix needed)
     * @param name    Optional filename for display in ImgBB dashboard
     */
    @FormUrlEncoded
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Field("image") image: String,
        @Field("name") name: String = ""
    ): ImgBBResponse
}

// ── Response models ──────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class ImgBBResponse(
    @Json(name = "data")    val data: ImgBBData? = null,
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "status")  val status: Int = 0
)

@JsonClass(generateAdapter = true)
data class ImgBBData(
    @Json(name = "id")          val id: String = "",
    @Json(name = "url")         val url: String = "",          // full-size direct link
    @Json(name = "display_url") val displayUrl: String = "",   // same as url for images
    @Json(name = "thumb")       val thumb: ImgBBThumb? = null, // 180px thumbnail
    @Json(name = "medium")      val medium: ImgBBMedium? = null, // medium size
    @Json(name = "delete_url")  val deleteUrl: String = "",    // to delete image later
    @Json(name = "size")        val size: Long = 0L,
    @Json(name = "width")       val width: String = "",
    @Json(name = "height")      val height: String = ""
)

@JsonClass(generateAdapter = true)
data class ImgBBThumb(
    @Json(name = "url") val url: String = ""
)

@JsonClass(generateAdapter = true)
data class ImgBBMedium(
    @Json(name = "url") val url: String = ""
)
