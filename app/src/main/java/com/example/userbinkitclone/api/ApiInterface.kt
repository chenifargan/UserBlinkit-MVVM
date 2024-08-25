package com.example.userbinkitclone.api

import com.example.userbinkitclone.models.FCMMessage
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {
//    @Headers(
//        "Authorization: Bearer 45bf175a8718eb03036ec054dd5ab91907c4f694",
//        "Content-Type: application/json"
//    )
    @Headers("Content-Type: application/json")
    @POST("v1/projects/blinkit-clone-b71f7/messages:send")
    fun sendNotification(@Body message: FCMMessage): Call<FCMMessage>
}