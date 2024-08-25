package com.example.userbinkitclone.fragments

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

import com.example.userbinkitclone.R
import com.example.userbinkitclone.adapters.AdapterCartProducts
import com.example.userbinkitclone.databinding.FragmentOrderDetailBinding
import com.example.userbinkitclone.viewmodels.UserViewModel
import kotlinx.coroutines.launch


class OrderDetailFragment : Fragment() {
private lateinit var binding : FragmentOrderDetailBinding
private  var status =0
private  var orderId =""
    private  val viewModel : UserViewModel by viewModels()
    private lateinit var adapterCartProducts: AdapterCartProducts

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderDetailBinding.inflate(layoutInflater)
        setStatusBarColor()

        onButtonBackClicked()
        getValues()
        settingStatus()
        lifecycleScope.launch {
            getOrderedProducts()
        }

        return binding.root
    }

    private fun onButtonBackClicked() {
        binding.tbOrderDetailFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_orderDetailFragment_to_ordersFragment)
        }
    }

    private suspend fun getOrderedProducts() {
    viewModel.getOrderedProducts(orderId).collect{cartList->
        adapterCartProducts = AdapterCartProducts()
        binding.rvProductsItems.adapter=adapterCartProducts
        adapterCartProducts.differ.submitList(cartList)



    }
    }

    private fun settingStatus() {
       val statusToViews = mapOf(
           0 to listOf(binding.iv1),
           1 to listOf(binding.iv1,binding.iv2,binding.view1),
           2 to listOf(binding.iv1,binding.iv2,binding.view1,binding.iv3,binding.view2),
           3 to  listOf(binding.iv1,binding.iv2,binding.view1,binding.iv3,binding.view2,binding.iv4,binding.view3)
       )
        val viewsToTint = statusToViews.getOrDefault(status, emptyList())
        for (view in viewsToTint){
            view.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.blue)
        }
    }

    private fun getValues() {
        val bundle = arguments
        status = bundle?.getInt("status")!!
        orderId = bundle.getString("orderId")!!

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


}