package com.example.apnivehicle.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ImgBBClient {

    // ┌─────────────────────────────────────────────────────────────────────┐
    // │  PASTE YOUR IMGBB API KEY HERE                                      │
    // │  Get it free at: https://api.imgbb.com  (login → copy API key)     │
    // └─────────────────────────────────────────────────────────────────────┘
    const val API_KEY = "14fbf0ca2a00fa988db1bbfda41a9257"

    private const val BASE_URL = "https://api.imgbb.com/"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)   // large Base64 payloads need more time
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    val api: ImgBBApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ImgBBApi::class.java)
    }
}
