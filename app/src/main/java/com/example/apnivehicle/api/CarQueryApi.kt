package com.example.apnivehicle.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * CarQuery API — free vehicle data API (carqueryapi.com)
 * Provides makes, models, and trim data.
 */
interface CarQueryApi {

    @GET("?cmd=getMakes&year=2000")
    suspend fun getMakes(): CarQueryMakesResponse

    @GET("?cmd=getModels")
    suspend fun getModels(
        @Query("make") make: String,
        @Query("year") year: Int? = null
    ): CarQueryModelsResponse

    @GET("?cmd=getTrims")
    suspend fun getTrims(
        @Query("make") make: String,
        @Query("model") model: String,
        @Query("year") year: Int? = null
    ): CarQueryTrimsResponse
}

@JsonClass(generateAdapter = true)
data class CarQueryMakesResponse(
    @Json(name = "Makes") val makes: List<CarMake> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CarMake(
    @Json(name = "make_id") val makeId: String = "",
    @Json(name = "make_display") val makeDisplay: String = "",
    @Json(name = "make_country") val makeCountry: String = ""
)

@JsonClass(generateAdapter = true)
data class CarQueryModelsResponse(
    @Json(name = "Models") val models: List<CarModel> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CarModel(
    @Json(name = "model_name") val modelName: String = "",
    @Json(name = "model_make_id") val modelMakeId: String = "",
    @Json(name = "model_year") val modelYear: String = ""
)

@JsonClass(generateAdapter = true)
data class CarQueryTrimsResponse(
    @Json(name = "Trims") val trims: List<CarTrim> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CarTrim(
    @Json(name = "model_id") val modelId: String = "",
    @Json(name = "model_make_id") val modelMakeId: String = "",
    @Json(name = "model_name") val modelName: String = "",
    @Json(name = "model_trim") val modelTrim: String = "",
    @Json(name = "model_year") val modelYear: String = "",
    @Json(name = "model_engine_fuel") val engineFuel: String = "",
    @Json(name = "model_transmission_type") val transmissionType: String = "",
    @Json(name = "model_engine_cc") val engineCc: String = ""
)
