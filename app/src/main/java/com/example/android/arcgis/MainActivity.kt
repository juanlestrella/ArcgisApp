package com.example.android.arcgis

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.databinding.DataBindingUtil
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.view.MapView
import com.example.android.arcgis.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    /** TODO: start thinking about the following
     * what api am i going to use
     * how the app is going to be structured/ designed
     *
     * App Structure Options: Single Activity Architecture
     * https://developer.android.com/guide/navigation/navigation-migrate
     *
     */
}