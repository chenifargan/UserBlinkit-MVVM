package com.example.userbinkitclone.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.userbinkitclone.R
import com.example.userbinkitclone.Utils
import com.example.userbinkitclone.activity.AuthMainActivity
import com.example.userbinkitclone.databinding.AddressBookLayoutBinding
import com.example.userbinkitclone.databinding.FragmentProfileBinding
import com.example.userbinkitclone.viewmodels.UserViewModel


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private val viewModel : UserViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        onBackButtonClicked()
        onOrdersLayoutClicked()
        onAddressBookClicked()
        onLogOutClicked()
        return binding.root
    }

    private fun onLogOutClicked() {
    binding.llLogOut.setOnClickListener {
        val builder = AlertDialog.Builder(requireContext())
        val alertDialog =builder.create()

            builder.setTitle("Log out")
            .setMessage("Do you want log out?")
            .setPositiveButton("Yes"){_,_->
                viewModel.logOutUser()
                startActivity(Intent(requireContext(),AuthMainActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("No"){_,_->
                alertDialog.dismiss()
            }
                .show()
                .setCancelable(false)

    }
    }

    private fun onAddressBookClicked() {
        binding.llAddress.setOnClickListener {
            val addressBookLayoutBinding = AddressBookLayoutBinding.inflate(LayoutInflater.from(requireContext()))
            viewModel.getUserAddress {address->
                addressBookLayoutBinding.etAddress.setText(address.toString())

            }
            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(addressBookLayoutBinding.root)
                .create()
            alertDialog.show()
            addressBookLayoutBinding.btnEdit.setOnClickListener {
                addressBookLayoutBinding.etAddress.isEnabled=true

            }
            addressBookLayoutBinding.btnSave.setOnClickListener {
                viewModel.saveAddress(addressBookLayoutBinding.etAddress.text.toString())
                alertDialog.dismiss()
                Utils.showToast(requireContext(),"Address update...")

            }

        }
    }

    private fun onOrdersLayoutClicked() {
        binding.llOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_ordersFragment)
        }
    }

    private fun onBackButtonClicked() {
        binding.tbProfileFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }
    }


}