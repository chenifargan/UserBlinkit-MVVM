package com.example.userbinkitclone.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.userbinkitclone.models.Product
import com.example.userbinkitclone.roomdb.CartProductDao
import com.example.userbinkitclone.roomdb.CartProductTable
import com.example.userbinkitclone.roomdb.CartProductsDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.example.userbinkitclone.Utils
import com.example.userbinkitclone.api.ApiUtilities
import com.example.userbinkitclone.models.Bestseller
import com.example.userbinkitclone.models.FCMMessage
import com.example.userbinkitclone.models.Notification
import com.example.userbinkitclone.models.Orders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.getValue
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UserViewModel(application: Application): AndroidViewModel(application) {
        //init
    val sharedPreferences : SharedPreferences = application.getSharedPreferences("My_Pref", Context.MODE_PRIVATE)
    val cartProductDao : CartProductDao = CartProductsDatabase.getDatabaseInstance(application).cartProductsDao()



    //room db
    suspend fun  insertCartProduct(products:CartProductTable){
        cartProductDao.insertCartProduct(products)
    }
    suspend fun  updateCartProduct(products:CartProductTable){
        cartProductDao.updateCartProduct(products)
    }

    suspend fun deleteCartProduct(product: String){
        cartProductDao.deleteCartProduct(product)
    }
    fun getAll(): LiveData<List<CartProductTable>>{
        return cartProductDao.getAllCartProducts()
    }
    suspend fun deleteCartProducts(){
        cartProductDao.deleteCartProducts()
    }

    //firebase call
    fun fetchAllTheProducts(): Flow<List<Product>> = callbackFlow{
        val db =     FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)



                }
                trySend(products)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose {
            db.removeEventListener(eventListener)
        }
    }
    fun getAllOrders():Flow<List<Orders>> = callbackFlow {
       val db= FirebaseDatabase.getInstance().getReference("Admins").child("Orders").orderByChild("orderStatus")
        val eventListener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = ArrayList<Orders>()
                for (orders in snapshot.children){
                    val order = orders.getValue(Orders::class.java)
                    if(order?.orderingUserId==Utils.getCurrentUserId()){
                        orderList.add(order!!)
                    }

                }
                trySend(orderList)



            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose{
            db.removeEventListener(eventListener)
        }

    }

    fun getOrderedProducts(orderId :String):Flow<List<CartProductTable>> = callbackFlow{
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId)
        val eventListener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Orders::class.java)
                trySend(order?.orderList!!)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose{
            db.removeEventListener(eventListener)
        }
    }
    fun updateItemCount(product: Product,itemCount: Int){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").child("itemCount").setValue(itemCount)

    }


    fun getCategoryProduct(category: String): Flow<List<Product>> = callbackFlow{
        val db =  FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${category}")
        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)



                }
                trySend(products)            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose {
            db.removeEventListener(eventListener)
        }


    }
    fun saveUserAddress(address:String){
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserId()).child("userAddress").setValue(address)
    }
    fun saveProductsAfterOrder(stock:Int,product: CartProductTable){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").child("itemCount").setValue(0)

        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").child("productStock").setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").child("productStock").setValue(stock)
          FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").child("productStock").setValue(stock)

    }

    fun getUserAddress(callback:(String?)->Unit){
        val db =FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserId()).child("userAddress")
        db.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val address = snapshot.getValue(String::class.java)
                    callback(address)

                }
                else{
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }

        })

    }
    fun logOutUser(){
        FirebaseAuth.getInstance().signOut()
    }
    fun saveAddress(address:String){
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserId()).child("userAddress").setValue(address)
    }
    fun saveOrderProducts(orders: Orders){
        FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orders.orderId!!).setValue(orders)
    }
    fun fetchProductType(): Flow<List<Bestseller>> = callbackFlow{
        val db =FirebaseDatabase.getInstance().getReference("Admins/ProductType")
        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val productTypeList = ArrayList<Bestseller>()
                for (productType in snapshot.children){
                    val productTypeName = productType.key
                    val productList =ArrayList<Product>()
                    for(products in productType.children){
                        val product =products.getValue(Product::class.java)
                        productList.add(product!!)
                    }
                    val bestseller = Bestseller(productType=productTypeName, products = productList)
                    productTypeList.add(bestseller)

                }
                trySend(productTypeList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose{
            db.removeEventListener(eventListener)
        }
    }

//    fun getUserAddress(callback: (String?)->Unit){
//        val db =FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserId()).child("userAddress")
//        db.addListenerForSingleValueEvent(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if(snapshot.exists()){
//                    val address =snapshot.getValue(String::class.java)
//                    callback(address)
//
//                }
//                else{
//                    callback(null)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//               callback(null)
//            }
//
//        })
//
//    }
        //sharePreference
    fun savingItemCartCount(itemCount:Int){
        sharedPreferences.edit().putInt("itemCount",itemCount).apply()
    }
    fun fetchTotalCartItemCount() :MutableLiveData<Int>{
        val totalItemCount =MutableLiveData<Int>()
        totalItemCount.value = sharedPreferences.getInt("itemCount",0)
        return  totalItemCount
    }

    fun saveAddressStatus(){
        sharedPreferences.edit().putBoolean("addressStatus",true).apply()

    }
    fun getAddressStatus():MutableLiveData<Boolean>{
        val status = MutableLiveData<Boolean>()
        status.value = sharedPreferences.getBoolean("addressStatus",false)
        return status
    }
    suspend fun sendNotification(adminUid: String, title: String, message: String) {
        val getToken = FirebaseDatabase.getInstance().getReference("Admins")
            .child("AdminInfo")
            .child(adminUid)
            .child("adminToken")
            .get()

        getToken.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result.getValue(String::class.java)
                if (token != null) {

                    val notification = Notification(title = title, body = message)
                    val message = FCMMessage(
                        message = FCMMessage.Message(
                            token = token, // or use `topic` or `condition` if applicable
                            notification = notification
                        )
                    )

                    ApiUtilities.notificationApi.sendNotification(message).enqueue(object : Callback<FCMMessage> {
                        override fun onResponse(call: Call<FCMMessage>, response: Response<FCMMessage>) {
                            if (response.isSuccessful) {
                                Log.d("GGG", "send notification")
                            } else {
                                Log.d("GGG", "not send notification, response code: ${response.code()}")
                                Log.d("GGG", "error: ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<FCMMessage>, t: Throwable) {
                            Log.d("ggg", "error: ${t.message}")
                        }
                    })
                } else {
                    Log.d("ggg", "Token is null")
                }
            } else {
                Log.d("ggg", "Failed to get token: ${task.exception?.message}")
            }
        }
    }



}