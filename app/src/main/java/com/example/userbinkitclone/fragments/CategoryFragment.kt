package com.example.userbinkitclone.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userbinkitclone.CartListener
import com.example.userbinkitclone.R
import com.example.userbinkitclone.Utils
import com.example.userbinkitclone.adapters.AdapterProduct
import com.example.userbinkitclone.databinding.FragmentCategoryBinding
import com.example.userbinkitclone.databinding.ItemViewProductBinding
import com.example.userbinkitclone.models.Product
import com.example.userbinkitclone.roomdb.CartProductTable
import com.example.userbinkitclone.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import java.lang.ClassCastException


class CategoryFragment : Fragment() {
private lateinit var binding :FragmentCategoryBinding
    private lateinit var adapterProduct: AdapterProduct

private  var category :String? =null
    private val viewModel : UserViewModel by viewModels()
private var cartListener : CartListener? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(layoutInflater)
        setStatusBarColor()
        getProductCategory()
        onNavigationIconClicked()
        setToolBarTitle()
        onSearchMenuClicked()
        fetchCategoryProduct()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun onNavigationIconClicked() {
        binding.tbSearchFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_homeFragment)
        }
    }

    private fun onSearchMenuClicked() {
        binding.tbSearchFragment.setOnMenuItemClickListener {menuItem->
            when(menuItem.itemId){
                R.id.searchMenu->{
                        findNavController().navigate(R.id.action_categoryFragment_to_searchFragment)
                    true
                }

                else -> {false}
            }
        }
    }

    private fun fetchCategoryProduct() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.getCategoryProduct(category!!).collect{
                if (it.isEmpty()){
                    binding.rvProducts.visibility = View.GONE
                    binding.tvText.visibility = View.VISIBLE
                }
                else{
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.tvText.visibility = View.GONE
                }
                adapterProduct = AdapterProduct(::onAddButtonClicked,::onIncrementButtonClicked,::onDecrementButtonClicked)
                binding.rvProducts.adapter= adapterProduct
                adapterProduct.differ.submitList(it)
                binding.shimmerViewContainer.visibility =View.GONE
            }
        }

    }

    private fun setToolBarTitle() {
        binding.tbSearchFragment.title = category
    }

    private fun getProductCategory() {
        val bundle = arguments
        category=bundle?.getString("category")

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

    private fun setStatusBarColor(){
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor=statusBarColors
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }

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