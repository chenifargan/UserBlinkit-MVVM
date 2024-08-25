package com.example.userbinkitclone.api

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtilities {
    private const val BASE_URL = "https://fcm.googleapis.com/"

    val notificationApi: ApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient())
            .build()
            .create(ApiInterface::class.java)
    }

    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val accessToken = GoogleTokenService.getAccessToken()
                Log.d("gggggg",accessToken)
                val newRequest = original.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .header("Content-Type", "application/json")
                    .build()

                chain.proceed(newRequest)
            }
            .build()
    }
}