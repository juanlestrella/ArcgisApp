package com.example.android.arcgis

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.databinding.DataBindingUtil
import com.example.android.arcgis.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    /**
     * TODO:
     * 1) Add button to recenter to current location
     * 2) Add button to save current screen and add it to the home page
     *
     */
}