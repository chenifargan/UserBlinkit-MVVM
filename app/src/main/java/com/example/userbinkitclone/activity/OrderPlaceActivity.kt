package com.example.userbinkitclone.activity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Base64.NO_WRAP
import android.util.Base64.encodeToString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.userbinkitclone.CartListener
import com.example.userbinkitclone.Constants
import com.example.userbinkitclone.R
import com.example.userbinkitclone.Utils
import com.example.userbinkitclone.adapters.AdapterCartProducts
import com.example.userbinkitclone.api.GoogleTokenService
import com.example.userbinkitclone.databinding.ActivityOrderPlaceBinding
import com.example.userbinkitclone.databinding.AddressLayoutBinding
import com.example.userbinkitclone.models.Orders
import com.example.userbinkitclone.viewmodels.UserViewModel


import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.view.CardInputWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject


class OrderPlaceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderPlaceBinding
    private val viewModel : UserViewModel by viewModels()
    private lateinit var adapterCartProducts: AdapterCartProducts

    private lateinit var paymentSheet : PaymentSheet
    private var customerID: String? = null
    private var ephemeralKey: String? = null
    private var clientSecret: String? = null
    private var cartListener : CartListener? = null

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "UserBlinkit"
    private val description = "Blinkit messages"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor()
        GoogleTokenService.initialize(this)

        initializeStripe()

        //getPaymentView()


        backToUserActivity()
        getAllCartProducts()
        onPlaceOrderClicked()
    }
    private fun initializeStripe() {
        PaymentConfiguration.init(this, Constants.PUBLISH_KEY)
        paymentSheet = PaymentSheet(this) { paymentResult ->
            onPaymentResult(paymentResult)
        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    }



    private fun onPaymentResult(paymentResult: PaymentSheetResult) {
        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                Utils.showToast(this@OrderPlaceActivity, "Payment successful")
                saveOrder()
                deleteCartProducts()
                sendNotification()
                startActivity(Intent(this@OrderPlaceActivity, UsersMainActivity::class.java))
                finish()
            }
            is PaymentSheetResult.Canceled -> {
                Utils.showToast(this@OrderPlaceActivity, "Payment canceled")
            }
            is PaymentSheetResult.Failed -> {
                Utils.showToast(this@OrderPlaceActivity, "Payment failed: ${paymentResult.error}")
                Log.e("PaymentError", paymentResult.error.message ?: "Unknown error")
            }
        }
    }

    private fun sendNotification() {
        val intent = Intent(this, UsersMainActivity::class.java)
        val body="Some products has been ordered"

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        // checking if android version is greater than oreo(API 26) or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                .setContentTitle("Blinkit")
                .setContentText(body)
                .setSmallIcon(R.drawable.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.app_icon))
                .setContentIntent(pendingIntent)
        } else {

            builder = Notification.Builder(this)
                .setContentTitle("Blinkit")
                .setContentText(body)
                .setSmallIcon(R.drawable.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.app_icon))
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1234, builder.build())
    }

    private fun deleteCartProducts() {
        lifecycleScope.launch {
            viewModel.deleteCartProducts()

        }
        viewModel.savingItemCartCount(0)
        cartListener?.hideCartLayout()

    }

    private fun saveOrder() {
        viewModel.getAll().observe(this){cartProductsList->
            if(cartProductsList.isNotEmpty()){
                viewModel.getUserAddress { address->
                    val order = Orders(
                        orderId = Utils.getRandomId(),
                        orderList = cartProductsList,
                        userAddress = address,
                        orderStatus = 0,
                        orderDate = Utils.getCurrentDate(),
                        orderingUserId = Utils.getCurrentUserId()

                    )
                    viewModel.saveOrderProducts(order)
                    lifecycleScope.launch {
                        viewModel.sendNotification(cartProductsList[0].adminUid!!,"Ordered","Some products has been ordered")
                    }

                }
                for (product in cartProductsList){
                    val count = product.productCount
                    val stock = product.productStock?.minus(count!!)
                    if (stock != null) {
                        viewModel.saveProductsAfterOrder(stock, product)

                    }
                }
            }

        }
    }


    private fun onPlaceOrderClicked() {
    binding.btnNext.setOnClickListener {

        lifecycleScope.launch {
            getPaymentViewWithDelay()
        }



    }
    }
    private suspend fun getPaymentViewWithDelay() {
        getPaymentView()
        delay(5000) // 5 seconds delay
        viewModel.getAddressStatus().observe(this@OrderPlaceActivity) { status ->
            if (status) {
                if (clientSecret != null) {
                    // payment work
                    paymentFlow()
                } else {
                    Utils.showToast(this@OrderPlaceActivity, "Client secret is not yet initialized. Please try again.")
                }
            } else {
                val addressLayoutBinding = AddressLayoutBinding.inflate(LayoutInflater.from(this@OrderPlaceActivity))
                val alertDialog = AlertDialog.Builder(this@OrderPlaceActivity)
                    .setView(addressLayoutBinding.root)
                    .create()
                alertDialog.show()
                addressLayoutBinding.btnAdd.setOnClickListener {
                    saveAddress(alertDialog, addressLayoutBinding)
                }
            }
        }
    }


    private fun getPaymentView() {
    val stringRequest = object : StringRequest(
        Method.POST, "https://api.stripe.com/v1/customers",
        Response.Listener<String> { response ->
            val jsonObject = JSONObject(response)
            customerID = jsonObject.getString("id")
            Log.d("tag", "Customer ID: $customerID")
            getEphemeralKey(customerID!!)
        },
        Response.ErrorListener { error ->
            Log.e("StripeError", "Error creating customer: ${error.message}")
        }) {
        override fun getHeaders(): MutableMap<String, String> {
            return hashMapOf("Authorization" to "Bearer ${Constants.SECRET_KEY}")
        }
    }
    Volley.newRequestQueue(this).add(stringRequest)
}

    private fun getEphemeralKey(customerID: String) {
        val stringRequest = object : StringRequest(
            Method.POST, "https://api.stripe.com/v1/ephemeral_keys",
            Response.Listener<String> { response ->
                val jsonObject = JSONObject(response)
                ephemeralKey = jsonObject.getString("id")
                Log.d("tag", "Ephemeral Key: $ephemeralKey")
                getClientSecret(customerID, ephemeralKey!!)
            },
            Response.ErrorListener { error ->
                Log.e("StripeError", "Error creating ephemeral key: ${error.message}")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf(
                    "Authorization" to "Bearer ${Constants.SECRET_KEY}",
                    "Stripe-Version" to "2024-06-20"
                )
            }

            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("customer" to customerID)
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun getClientSecret(customerID: String, ephemeralKey: String) {
        val stringRequest = object : StringRequest(
            Method.POST, "https://api.stripe.com/v1/payment_intents",
            Response.Listener<String> { response ->
                val jsonObject = JSONObject(response)
                clientSecret = jsonObject.getString("client_secret")
                Log.d("tag", "Client Secret: $clientSecret")
                //paymentFlow()
            },
            Response.ErrorListener { error ->
                Log.e("StripeError", "Error creating payment intent: ${error.message}")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf("Authorization" to "Bearer ${Constants.SECRET_KEY}")
            }

            override fun getParams(): MutableMap<String, String> {
                val params = hashMapOf<String, String>()
                var total = binding.tvTotal.text.toString()
                total = total.substring(0, total.length - 1)
                params["customer"] = customerID
                params["amount"] = total + "00"
                params["currency"] = "usd"
                params["automatic_payment_methods[enabled]"] = "true"
                return params
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }
    private fun paymentFlow() {

        if (clientSecret != null) {
            Log.d("tag", "Starting payment flow with clientSecret: $clientSecret")
            paymentSheet.presentWithPaymentIntent(
                clientSecret!!,
                PaymentSheet.Configuration(
                    "ABC company",
                   // PaymentSheet.CustomerConfiguration(customerID!!, ephemeralKey!!)
                )
            )
        } else {
            Utils.showToast(this, "Client secret is not initialized.")
        }
    }


    private fun saveAddress(alertDialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
        Utils.showDialog(this,"Processing....")
        val userPinCode = addressLayoutBinding.etPinCode.text.toString()
        val userPhoneNumber = addressLayoutBinding.etPhone.text.toString()
        val userState =  addressLayoutBinding.etState.text.toString()
        val userDistrict =  addressLayoutBinding.etDistrict.text.toString()
        val  userAddress = addressLayoutBinding.etAddress.text.toString()

        val address = "$userPinCode, $userDistrict($userState),$userAddress,$userPhoneNumber"
        lifecycleScope.launch {
            viewModel.saveUserAddress(address)
            viewModel.saveAddressStatus()
        }
        Utils.showToast(this,"Saved...")
        alertDialog.dismiss()
        Utils.hideDialog()
    }

    private fun backToUserActivity() {
        binding.tbOrderFragment.setNavigationOnClickListener {
            startActivity(Intent(this,UsersMainActivity::class.java))
            finish()

        }
    }

    private fun getAllCartProducts(){
        viewModel.getAll().observe(this){cartProductList ->

        adapterCartProducts = AdapterCartProducts()
            binding.rvProductsItems.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(cartProductList)
            var totalPrice =0
            for (product in cartProductList){
                val price = product.productPrice?.let {
                    it.removeSuffix("$").toIntOrNull()
                } ?: 0
                val item = product.productCount?:0
                totalPrice +=(price*item)
            }
            binding.tvSubTotal.text = totalPrice.toString()+"$"
            if(totalPrice<200){
                binding.tvDelivery.text = "20$"
                totalPrice+=20
            }
            binding.tvTotal.text = totalPrice.toString()+ "$"
        }
    }
    private fun setStatusBarColor(){
        window?.apply {
            val statusBarColors = ContextCompat.getColor(this@OrderPlaceActivity,R.color.orange)
            statusBarColor=statusBarColors
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }

        }
    }
}