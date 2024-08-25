package com.example.userbinkitclone.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userbinkitclone.CartListener
import com.example.userbinkitclone.Constants
import com.example.userbinkitclone.R
import com.example.userbinkitclone.Utils
import com.example.userbinkitclone.adapters.AdapterBestseller
import com.example.userbinkitclone.adapters.AdapterCategory
import com.example.userbinkitclone.adapters.AdapterProduct
import com.example.userbinkitclone.databinding.BsSeeAllBinding
import com.example.userbinkitclone.databinding.FragmentHomeBinding
import com.example.userbinkitclone.databinding.ItemViewProductBinding
import com.example.userbinkitclone.models.Bestseller
import com.example.userbinkitclone.models.Category
import com.example.userbinkitclone.models.Product
import com.example.userbinkitclone.roomdb.CartProductTable
import com.example.userbinkitclone.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.lang.ClassCastException


class HomeFragment : Fragment() {
private lateinit var binding:FragmentHomeBinding
    private val viewModel : UserViewModel by viewModels()
    private lateinit var adapterBestseller :AdapterBestseller
    private  lateinit var  adapterProduct: AdapterProduct
    private var cartListener : CartListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentHomeBinding.inflate(layoutInflater)
        setStatusBarColor()
        setAllCategories()
        navigationToSearchFragment()
        onProfileClicked()
        fetchBestseller()
        return binding.root
    }

    private fun fetchBestseller() {
        binding.shimmerViewContainer.visibility=View.VISIBLE
       lifecycleScope.launch {
           viewModel.fetchProductType().collect{
               adapterBestseller = AdapterBestseller(::onSeeAllButtonClicked)
               binding.rvBestsellers.adapter = adapterBestseller
               adapterBestseller.differ.submitList(it)
               binding.shimmerViewContainer.visibility=View.GONE

           }
       }
    }

    private fun onProfileClicked() {
      binding.ivProfile.setOnClickListener {
          findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
      }
    }

    private fun navigationToSearchFragment() {
        binding.searchCv.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }


    private fun setAllCategories() {
        val categoryList = ArrayList<Category>()
        for(i in 0 until Constants.allProductsCategoryIcon.size){
            categoryList.add(Category(
                Constants.allProductsCategory[i],
                Constants.allProductsCategoryIcon[i]))
    }
    binding.rvCategories.adapter= AdapterCategory(categoryList,::onCategoryIconClicked)


    }
     fun onCategoryIconClicked(category: Category){
         val bundle = Bundle()
         bundle.putString("category",category.title)
        findNavController().navigate(R.id.action_homeFragment_to_categoryFragment,bundle)
    }

fun onSeeAllButtonClicked(productType:Bestseller){
val bsSeeAllBinding = BsSeeAllBinding.inflate(LayoutInflater.from(requireContext()))
    val bs = BottomSheetDialog(requireContext())
        bs.setContentView(bsSeeAllBinding.root)
    adapterProduct = AdapterProduct(::onAddButtonClicked,::onIncrementButtonClicked,::onDecrementButtonClicked)
    bsSeeAllBinding.rvProducts.adapter =  adapterProduct
    adapterProduct.differ.submitList(productType.products)
    bs.show()
}
    private fun onAddButtonClicked(product: Product, productBinding: ItemViewProductBinding){
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


    private fun onIncrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){
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
    private fun onDecrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){
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
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.orange)
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