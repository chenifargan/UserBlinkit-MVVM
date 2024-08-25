package com.example.userbinkitclone.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.userbinkitclone.CartListener
import com.example.userbinkitclone.adapters.AdapterCartProducts
import com.example.userbinkitclone.databinding.ActivityUsersMainBinding
import com.example.userbinkitclone.databinding.BsCartProductsBinding
import com.example.userbinkitclone.roomdb.CartProductTable
import com.example.userbinkitclone.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class UsersMainActivity : AppCompatActivity() ,CartListener{
        private lateinit var binding : ActivityUsersMainBinding
        private val viewModel : UserViewModel by viewModels()
    private lateinit var cartProductList: List<CartProductTable>
    private lateinit var adapterCartProducts: AdapterCartProducts
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityUsersMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getAllCartProducts()
        getTotalItemCountInCart()
        onCartClicked()
        onNextButtonClicked()

    }

    private fun onNextButtonClicked() {
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this,OrderPlaceActivity::class.java))
        }
    }


    private fun onCartClicked() {
        binding.llItemCart.setOnClickListener {
            val bsCartProductsBinding = BsCartProductsBinding.inflate(LayoutInflater.from(this))
            val bs = BottomSheetDialog(this)
            bs.setContentView(bsCartProductsBinding.root)
            bsCartProductsBinding.tvNumberOfProductCount.text = binding.tvNumberOfProductCount.text.toString()
            bsCartProductsBinding.btnNext.setOnClickListener {
                startActivity(Intent(this,OrderPlaceActivity::class.java))
            }
            adapterCartProducts = AdapterCartProducts()
            bsCartProductsBinding.rvProductsItems.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(cartProductList)
            bs.show()
        }
    }
    private fun getAllCartProducts(){
        viewModel.getAll().observe(this){

                cartProductList = it

        }
    }

    private fun getTotalItemCountInCart() {
        viewModel.fetchTotalCartItemCount().observe(this){
            if(it>0){
                binding.llCard.visibility= View.VISIBLE
                binding.tvNumberOfProductCount.text = it.toString()

            }
            else{
                binding.llCard.visibility= View.GONE


            }
        }
    }

    override fun showCartLayout(itemCount:Int) {
    val previousCount = binding.tvNumberOfProductCount.text.toString().toInt()
        val updatedCount =previousCount+itemCount
        if (updatedCount>0){
            binding.llCard.visibility= View.VISIBLE

            binding.tvNumberOfProductCount.text = updatedCount.toString()


    }
        else{
            binding.llCard.visibility= View.GONE
            binding.tvNumberOfProductCount.text = "0"


        }
}

    override fun savingCartItemCount(itemCount: Int) {
        viewModel.fetchTotalCartItemCount().observe(this){
            viewModel.savingItemCartCount(it+itemCount)
        }



    }

    override fun hideCartLayout() {
        binding.llCard.visibility= View.GONE
        binding.tvNumberOfProductCount.text = "0"
    }
}