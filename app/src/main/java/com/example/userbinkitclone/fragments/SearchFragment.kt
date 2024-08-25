package com.example.userbinkitclone.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userbinkitclone.CartListener
import com.example.userbinkitclone.R
import com.example.userbinkitclone.Utils
import com.example.userbinkitclone.adapters.AdapterProduct
import com.example.userbinkitclone.databinding.FragmentSearchBinding
import com.example.userbinkitclone.databinding.ItemViewProductBinding
import com.example.userbinkitclone.models.Product
import com.example.userbinkitclone.roomdb.CartProductTable
import com.example.userbinkitclone.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import java.lang.ClassCastException


class SearchFragment : Fragment() {

    private lateinit var binding : FragmentSearchBinding
private lateinit var adapterProduct: AdapterProduct
val viewModel :UserViewModel by viewModels()
    private var cartListener : CartListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        getAllTheProducts()
        backToHomeFragment()
        searchProducts()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun backToHomeFragment() {
       binding.searchEt.setOnClickListener {
           findNavController().navigate(R.id.action_searchFragment_to_homeFragment)
       }

    }
    private fun searchProducts() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val quary = s.toString().trim()
                adapterProduct.getFilter().filter(quary)
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun getAllTheProducts() {
        binding.shimmerViewContainer.visibility =View.VISIBLE
        lifecycleScope.launch {
            viewModel.fetchAllTheProducts().collect{
                if (it.isEmpty()){
                    binding.rvProducts.visibility = View.GONE
                    binding.tvText.visibility = View.VISIBLE
                }
                else{
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.tvText.visibility = View.GONE
                }
                adapterProduct = AdapterProduct(::onAddButtonClicked,
                    ::onIncrementButtonClicked,
                    ::onDecrementButtonClicked
                )
                binding.rvProducts.adapter= adapterProduct
                adapterProduct.differ.submitList(it)
               adapterProduct.originalList =it as ArrayList<Product>
                binding.shimmerViewContainer.visibility =View.GONE

            }
        }



    }
    private fun onAddButtonClicked(product: Product,productBinding: ItemViewProductBinding){
        productBinding.tvAdd.visibility=View.GONE
        productBinding.allProductCount.visibility =View.VISIBLE
        //step 1
        var itemCount = productBinding.tvProductCount.text.toString().toInt()
        itemCount++
        productBinding.tvProductCount.text = itemCount.toString()
        cartListener?.showCartLayout(1)
        //step 2
        product.itemCount = itemCount

        lifecycleScope.launch {
            cartListener?.savingCartItemCount(1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product,itemCount)
        }



    }
    private fun onIncrementButtonClicked(product: Product,productBinding: ItemViewProductBinding){
        var itemCountIncrement = productBinding.tvProductCount.text.toString().toInt()
        itemCountIncrement++
        if(product.productStock!!+1>itemCountIncrement){
            productBinding.tvProductCount.text = itemCountIncrement.toString()
            cartListener?.showCartLayout(1)
            //step 2
            product.itemCount = itemCountIncrement

            lifecycleScope.launch {
                cartListener?.savingCartItemCount(1)
                saveProductInRoomDb(product)
                viewModel.updateItemCount(product, itemCountIncrement)
            }
        }
        else{
            Utils.showToast(requireContext(),"Can't add more item of this")
        }

    }
    private fun onDecrementButtonClicked(product: Product,productBinding: ItemViewProductBinding){
        var itemCountDecrement = productBinding.tvProductCount.text.toString().toInt()
        itemCountDecrement--
        //step 2
        product.itemCount = itemCountDecrement

        lifecycleScope.launch {
            cartListener?.savingCartItemCount(-1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product,itemCountDecrement)

        }
        if(itemCountDecrement>0) {
            productBinding.tvProductCount.text = itemCountDecrement.toString()
        }
        else{
            lifecycleScope.launch {
                viewModel.deleteCartProduct(product.productRandomId!!)
            }
            productBinding.tvAdd.visibility=View.VISIBLE
            productBinding.allProductCount.visibility =View.GONE
            productBinding.tvProductCount.text= "0"

        }
        cartListener?.showCartLayout(-1)



    }
    private fun saveProductInRoomDb(product: Product) {

        val cartProduct = CartProductTable(
            productRandomId = product.productRandomId!!,
            productTitle = product.productTitle,
            productQuantity =product.productQuantity.toString()+product.productUnit.toString(),
            productPrice = product.productPrice.toString()+"$",
            productCount = product.itemCount,
            productStock = product.productStock,
            productImage = product.productImageUris?.get(0)!!,
            productCategory = product.productCategory,
            adminUid = product.adminUid,
            productType = product.productType







        )
        lifecycleScope.launch {
            viewModel.insertCartProduct(cartProduct)

        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if( context is CartListener){
            cartListener=context
        }
        else{
            throw ClassCastException("please implement cart listener")
        }




    }
}