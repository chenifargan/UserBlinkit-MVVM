package com.example.userbinkitclone.api

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials

import java.io.IOException
import java.io.InputStream

object GoogleTokenService {

    private const val SCOPES = "https://www.googleapis.com/auth/firebase.messaging"
    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context
    }

    fun getAccessToken(): String {
        return try {
            val inputStream: InputStream = context.assets.open("blinkit-clone-b71f7-firebase-adminsdk-5cqyf-36d92ec22e.json")
            val credentials = GoogleCredentials
                .fromStream(inputStream)
                .createScoped(listOf(SCOPES))
            credentials.refreshIfExpired()
            credentials.accessToken.tokenValue

        } catch (e: IOException) {
            throw RuntimeException("Failed to load service account key", e)
        }
    }

}