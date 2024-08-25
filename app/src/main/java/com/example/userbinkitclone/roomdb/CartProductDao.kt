package com.example.userbinkitclone.roomdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

 @Dao
interface CartProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCartProduct(products:CartProductTable)
    @Update
    fun updateCartProduct(products:CartProductTable)

    @Query("DELETE FROM CartProductTable WHERE productRandomId= :productId")
  suspend  fun deleteCartProduct(productId: String)

    @Query("SELECT * FROM CartProductTable")
    fun getAllCartProducts(): LiveData<List<CartProductTable>>

    @Query("DELETE FROM CartProductTable")
   suspend fun deleteCartProducts()
}