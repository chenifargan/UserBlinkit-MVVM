package com.example.userbinkitclone

interface CartListener {

    fun showCartLayout(itemCount:Int)
    fun savingCartItemCount(itemCount:Int)
    fun hideCartLayout()
}