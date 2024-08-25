package com.example.userbinkitclone.models

data class Notification(
    val title: String? = null,
    val body: String? = null
)

data class FCMMessage(
    val message: Message
) {
    data class Message(
        val token: String? = null, // Use `token` or `topic` or `condition` for the target
        val notification: Notification
    )
}