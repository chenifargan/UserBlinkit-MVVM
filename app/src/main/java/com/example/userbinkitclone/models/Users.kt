package com.example.userbinkitclone.models

data class Users (
    var uid:String ?=null,
    val userPhoneNumber: String? =null,
    val userAddress:String?=null,
    var userToken:String?=null
)
