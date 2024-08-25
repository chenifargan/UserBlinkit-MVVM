package com.example.userbinkitclone.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController

import com.example.userbinkitclone.R
import com.example.userbinkitclone.Utils
import com.example.userbinkitclone.activity.UsersMainActivity
import com.example.userbinkitclone.databinding.FragmentOTPBinding
import com.example.userbinkitclone.models.Users
import com.example.userbinkitclone.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

class OTPFragment : Fragment() {
    private lateinit var binding :FragmentOTPBinding
    private lateinit var userNumber :String
    private val viewModel: AuthViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOTPBinding.inflate(layoutInflater)
        getUserNumber()
        customizingEnteringOTP()
        sendOTP()
        onLoginButtonClicked()
        onBackButtonClicked()
        return binding.root
    }

    private fun onLoginButtonClicked() {
        binding.btnLogin.setOnClickListener {
            Utils.showDialog(requireContext(),"Signing you....")
            val editText = arrayOf(binding.etOtp1,binding.etOtp2,binding.etOtp3,binding.etOtp4,binding.etOtp5,binding.etOtp6)
            val otp= editText.joinToString (""){ it.text.toString() }
            if(otp.length<editText.size){
                Utils.showToast(requireContext(),"Please enter right OTP")
            }
            else{
                editText.forEach { it.text?.clear(); it.clearFocus() }
                verifyOtp(otp)
             }
        }
    }

    private fun verifyOtp(otp: String) {

        val user = Users(uid = null, userPhoneNumber =userNumber , userAddress =" " )
        viewModel.signInWithPhoneAuthCredential(otp,userNumber,user)
        lifecycleScope.launch {
            viewModel.isSignedInSuccessfully.collect{
                if(it){
                    Utils.hideDialog()
                    Utils.showToast(requireContext(),"Logged in...")
                    startActivity(Intent(requireActivity(),UsersMainActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }

    private fun sendOTP() {
        Utils.showDialog(requireContext(),"Sending OTP...")
        viewModel.apply {
            viewModel.sendOTP(userNumber,requireActivity())
            lifecycleScope.launch {
                otpSent.collect{otpSent->
                 if(otpSent){
                     Utils.hideDialog()
                     Utils.showToast(requireContext(),"OTP sent")

                    }


                }
            }
            }


    }

    private fun onBackButtonClicked() {
        binding.tbOtpFragment.setNavigationOnClickListener{
            findNavController().navigate(R.id.action_OTPFragment_to_signInFragment)
        }
    }

    private fun customizingEnteringOTP() {
        val editText = arrayOf(binding.etOtp1,binding.etOtp2,binding.etOtp3,binding.etOtp4,binding.etOtp5,binding.etOtp6)
        for ( i in editText.indices){
            editText[i].addTextChangedListener (object :TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if(s?.length==1){
                        if(i<editText.size-1){
                            editText[i+1].requestFocus()
                        }
                        else if(s?.length==0){
                            if(i>0){
                                editText[i-1].requestFocus()
                            }
                        }
                    }
                }

            })
        }
    }

    private fun getUserNumber() {
        val bundle = arguments
        userNumber = bundle?.getString("number").toString()
        binding.txUserNumber.text = userNumber
    }

}