package com.example.android.arcgis

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.android.arcgis.databinding.ActivityMainBinding
import com.example.android.arcgis.map.MapFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}