package com.example.userbinkitclone.models

import com.example.userbinkitclone.roomdb.CartProductTable

data class Orders(
    val orderId :String? = null,
    val orderList: List <CartProductTable>? = null,
    val userAddress :String? = null,
    val orderStatus :Int? = 0,
    val orderDate :String? = null,
    val orderingUserId: String? =null



    )
