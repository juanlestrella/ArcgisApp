package com.example.android.arcgis.map

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.example.android.arcgis.BuildConfig.ArcgisToken

import com.example.android.arcgis.R
import com.example.android.arcgis.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    private lateinit var binding: FragmentMapBinding

    private lateinit var mapView: MapView

    private val locationDisplay: LocationDisplay by lazy {
        mapView.locationDisplay
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()
    }
    override fun onPause() {
        mapView.pause()
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        mapView.resume()
    }
    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        mapView = binding.mapView
        val navController = findNavController()
        setApiKey()
        setMap(binding.spinner)

        return binding.root
    }

    private fun setSpinner(spinner: Spinner) {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            BasemapStyle.values().map {it.name.replace("_", " ")}.toTypedArray()
        ).also{ adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long) {
                mapView.map = ArcGISMap(BasemapStyle.valueOf(parent.getItemAtPosition(position).toString().replace(" ", "_")))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Toast.makeText(context, "Nothing selected", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun setMap(spinner: Spinner){
        setSpinner(spinner)
        locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
        locationDisplay.startAsync()
    }

    private fun setApiKey() {
        val apiKey = ArcgisToken
        ArcGISRuntimeEnvironment.setApiKey(apiKey)
    }

    private fun requestPermission() {
        // check if both permissions are granted otherwise show an error
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted
                    locationDisplay.startAsync()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted
                    locationDisplay.startAsync()
                }
                else -> {
                    // no location granted
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.location_permission_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }
}