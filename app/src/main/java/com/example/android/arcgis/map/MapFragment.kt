package com.example.android.arcgis.map

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment.setApiKey
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.example.android.arcgis.BuildConfig.ArcgisToken
import com.example.android.arcgis.Constants
import com.example.android.arcgis.MainActivity

import com.example.android.arcgis.R
import com.example.android.arcgis.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    companion object {
        private val TAG: String = MapFragment::class.java.simpleName
    }

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

    private fun setMap(spinner: Spinner){
        setSpinner(spinner)
        locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
        locationDisplay.startAsync()
    }

    /**
     * Change the basemap style (OSM Standard, OSM Standard Relief, OSM Streets)
     */
    private fun setSpinner(spinner: Spinner) {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            //BasemapStyle.values().map {it.name.replace("_", " ")}.toTypedArray() // this would show all the basemap
            resources.getStringArray(R.array.maps_array)
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
                //mapView.map = ArcGISMap(BasemapStyle.valueOf(parent.getItemAtPosition(position).toString().replace(" ", "_"))) // this would show all the basemap
                when (position.toInt()){
                    0 -> {
                        addFeatureLayers(ArcGISMap(BasemapStyle.valueOf(resources.getString(R.string.OSM_STANDARD))))
                    }
                    1 -> {
                        addFeatureLayers(ArcGISMap(BasemapStyle.valueOf(resources.getString(R.string.OSM_STANDARD_RELIEF))))
                    }
                    else -> {
                        addFeatureLayers(ArcGISMap(BasemapStyle.valueOf(resources.getString(R.string.OSM_STREETS))))
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Toast.makeText(context, "Nothing selected", Toast.LENGTH_LONG).show()
            }

        }
    }

    /**
     * Adds feature layers to the different types of map from Spinner
     */
    private fun addFeatureLayers(map: ArcGISMap) {
        val portal = Portal(Constants.portal)
        val layerId = 0.toLong()
        mapView.map = map
        addFeatureLayerHelper(portal, Constants.portalItemIdShops, layerId)
        addFeatureLayerHelper(portal, Constants.portalItemIdTouristAttractions, layerId)
    }

    /**
     * helps remove redundancy for addFeatureLayers
     */
    private fun addFeatureLayerHelper(portal: Portal, portalId: String, layerId: Long) {
        val portalItem = PortalItem(portal, portalId)
        val layer = FeatureLayer(portalItem, layerId)

        mapView.map.apply{
            operationalLayers.add(layer)
        }

        portalLoadingListener(portalItem)
    }

    /**
     * add a listener for if portalItem fails to load
     */
    private fun portalLoadingListener(portalItem : PortalItem){
        portalItem.addDoneLoadingListener {
            if(portalItem.loadStatus != LoadStatus.LOADED){
                val error = "Failed to load portal item ${portalItem.loadError.message}"
                Log.e(TAG, error)
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                return@addDoneLoadingListener
            }
        }
    }

    private fun setApiKey() {
        val apiKey = ArcgisToken
        ArcGISRuntimeEnvironment.setApiKey(apiKey)
    }

    /**
     * Request Permission from the user to get their current location
     */
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