package com.example.userbinkitclone.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import com.example.userbinkitclone.R

class AuthMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            requestWindowFeature(Window.FEATURE_NO_TITLE)



        supportActionBar?.setDisplayShowTitleEnabled(false)


        setContentView(R.layout.activity_main)

    }
}