package com.example.android.arcgis.map

import android.Manifest
import android.content.res.ColorStateList
import android.database.MatrixCursor
import android.graphics.drawable.BitmapDrawable
import android.nfc.Tag
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment.setApiKey
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.example.android.arcgis.BuildConfig.ArcgisToken
import com.example.android.arcgis.Constants

import com.example.android.arcgis.R
import com.example.android.arcgis.databinding.FragmentMapBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.roundToInt

class MapFragment : Fragment() {

    companion object {
        private val TAG: String = MapFragment::class.java.simpleName
    }

    private lateinit var binding: FragmentMapBinding

    private lateinit var mapView: MapView

    private lateinit var addressSearchView: SearchView

    private var callout: Callout? = null

    private var addressGeocodeParameters: GeocodeParameters ?= null

    private val pinSymbol: PictureMarkerSymbol? by lazy {
        createPinSymbol()
    }

    private val locatorTask: LocatorTask by lazy {
        LocatorTask(Constants.locatorTask)
    }

    private val locationDisplay: LocationDisplay by lazy {
        mapView.locationDisplay
    }

    private val graphicsOverlay: GraphicsOverlay by lazy {
        GraphicsOverlay()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()

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
        addressSearchView = binding.searchAddress

        val navController = findNavController()

        setApiKey()

        mapView.apply {
            // init map for addFeatureLayers
            map = ArcGISMap(BasemapStyle.valueOf(resources.getString(R.string.OSM_STANDARD)))

            // graphicsOverlay requires ApiKey
            graphicsOverlays.add(graphicsOverlay)
            onTouchListener = object : DefaultMapViewOnTouchListener(requireContext(),mapView){
                override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
                    identifyGraphic(motionEvent)
                    return true
                }
            }
        }

        setMap(binding.spinner)
        addFeatureLayers()
        setAddressSearchView()
        recenterToCurrentLocation(binding.recenterCurrentLocation)


        return binding.root
    }

    private fun setApiKey() {
        val apiKey = ArcgisToken
        setApiKey(apiKey)
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
                when (position){
                    0 -> {
                        mapView.apply{
                            map.basemap = Basemap(BasemapStyle.valueOf(resources.getString(R.string.OSM_STANDARD)))
                        }
                    }
                    1 -> {
                        mapView.apply{
                            map.basemap = Basemap(BasemapStyle.valueOf(resources.getString(R.string.OSM_STANDARD_RELIEF)))
                        }
                    }
                    else -> {
                        mapView.apply{
                            map.basemap = Basemap(BasemapStyle.valueOf(resources.getString(R.string.OSM_STREETS)))
                        }
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
    private fun addFeatureLayers() {
        val portal = Portal(Constants.portal)
        addFeatureLayerHelper(portal, Constants.portalItemIdShops)
        addFeatureLayerHelper(portal, Constants.portalItemIdTouristAttractions)
    }

    /**
     * Helps remove redundancy for addFeatureLayers
     */
    private fun addFeatureLayerHelper(portal: Portal, portalId: String, layerId: Long = 0.toLong()) {
        val portalItem = PortalItem(portal, portalId)
        val layer = FeatureLayer(portalItem, layerId)
        mapView.map.operationalLayers.add(layer)
        portalLoadingListener(portalItem)
    }

    /**
     * Add a listener for if portalItem fails to load
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

    /**
     * Set up the address search view
     */
    private fun setAddressSearchView() {
        addressGeocodeParameters = GeocodeParameters().apply {
            resultAttributeNames.addAll(listOf("PlaceName", "Place_addr"))
            maxResults = 1

            addressSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    geocodeTypedAddress(query)
                    addressSearchView.clearFocus()
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean {
                    if(query.isNotEmpty()){
                        val suggestions = locatorTask.suggestAsync(query)
                        suggestions.addDoneListener {
                            try {
                                val suggestResults = suggestions.get()
                                // set up params for searching with MatrixCursor
                                val address = "address"
                                val columnNames = arrayOf(BaseColumns._ID, address)
                                val suggestionsCursor = MatrixCursor(columnNames)

                                // add each address suggestion to a new row
                                for((key, result) in suggestResults.withIndex()){
                                    suggestionsCursor.addRow(arrayOf<Any>(key,result.label))
                                }
                                // column names for the adapter to look at when mapping data
                                val cols = arrayOf(address)
                                // ids that show where data should be assign in the layout
                                val to = intArrayOf(R.id.suggestion_address)
                                // define SimpleCursorAdapter
                                val suggestionAdapter = SimpleCursorAdapter(
                                    requireContext(),
                                    R.layout.suggestions,
                                    suggestionsCursor,
                                    cols,
                                    to,
                                    0)
                                addressSearchView.suggestionsAdapter = suggestionAdapter
                                // handle an address suggestion being chosen
                                addressSearchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener{
                                    override fun onSuggestionSelect(position: Int): Boolean {
                                        return false
                                    }

                                    override fun onSuggestionClick(position: Int): Boolean {
                                       // get selected row
                                        (suggestionAdapter.getItem(position) as? MatrixCursor)?.let { selectedRow ->
                                            // get row's index
                                            val selectedCursorIndex = selectedRow.getColumnIndex(address)
                                            // get the string from the row at index
                                            val selectedAddress = selectedRow.getString(selectedCursorIndex)
                                            // use clicked suggestion as query
                                            addressSearchView.setQuery(selectedAddress, true)
                                        }
                                        return true
                                    }
                                })
                            } catch (e: Exception){
                                Log.e(TAG, "Geocode Suggestion Error: " + e.message)
                                Toast.makeText(requireContext(), "Geocode Suggestion Error", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    return true
                }
            })
        }
    }

    /**
     * Geocode the address sent by user
     */
    private fun geocodeTypedAddress(address : String){
        locatorTask.addDoneLoadingListener {
            if (locatorTask.loadStatus == LoadStatus.LOADED){
                val geocodeResultFuture = locatorTask.geocodeAsync(address, addressGeocodeParameters)
                geocodeResultFuture.addDoneListener {
                    try {
                        // get result of async operation
                        val geocodeResult = geocodeResultFuture.get()
                        if(geocodeResult.isNotEmpty()) {
                            displaySearchResultOnMap(geocodeResult[0])
                        } else {
                            Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception){
                        Log.e(TAG, "Geocode Failed: " + e.message)
                        Toast.makeText(requireContext(), "Geocode failed on address: " + e.message, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                locatorTask.retryLoadAsync()
            }
        }
        locatorTask.loadAsync()
    }

    /**
     * Add a GeocodeResult to the graphicsOverlay
     */
    private fun displaySearchResultOnMap(geocodeResult: GeocodeResult) {
        // clear previous graphics overlay
        graphicsOverlay.graphics.clear()
        // create graphic object for resulting location
        val resultPoint = geocodeResult.displayLocation
        val resultLocationGraphic = Graphic(resultPoint, geocodeResult.attributes, pinSymbol)
        // add graphic to location layer
        graphicsOverlay.graphics.add(resultLocationGraphic)
        mapView.setViewpointAsync(Viewpoint(geocodeResult.extent), 1f)
    }

    /**
     * Identifies and shows a call out on tapped graphic
     */
    private fun identifyGraphic(motionEvent: MotionEvent){
        val screenPoint: android.graphics.Point = android.graphics.Point(
            motionEvent.x.roundToInt(), motionEvent.y.roundToInt()
        )
        // get the graphics near the tapped location
        val identifyResultFuture: ListenableFuture<IdentifyGraphicsOverlayResult> =
            mapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 10.0, false)
        identifyResultFuture.addDoneListener {
            try{
                val identifyGraphicsOverlayResult: IdentifyGraphicsOverlayResult = identifyResultFuture.get()
                val graphics = identifyGraphicsOverlayResult.graphics
                // gets first graphic
                if (graphics.isNotEmpty()){
                    val identifiedGraphic : Graphic = graphics[0]
                    // show call out of the identified graphic
                    showCallout(identifiedGraphic)
                } else {
                    callout?.dismiss()
                }
            }catch (e: Exception){
                Log.e(TAG, "Identify error: " + e.message)
                Toast.makeText(requireContext(), "Identify error: " + e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Shows the given graphic's attributes as a call out
     * TODO: Add more information to callout
     */
    private fun showCallout(identifiedGraphic: Graphic) {
        // create textview for callout
        val calloutContent = TextView(requireActivity().applicationContext).apply {

            setTextColor(ContextCompat.getColor(context, R.color.black))

            this.text = if (identifiedGraphic.attributes["PlaceName"].toString().isNotEmpty()) {
                identifiedGraphic.attributes["PlaceName"].toString() + "\n" + identifiedGraphic.attributes["Place_addr"].toString()
            } else {
                identifiedGraphic.attributes["Place_addr"].toString()
            }
        }
        // get the center of the graphic to set the callout location
        val centerOfGraphic = identifiedGraphic.geometry.extent.center
        val calloutLocation = identifiedGraphic.computeCalloutLocation(centerOfGraphic, mapView)

        callout = mapView.callout.apply {
            showOptions = Callout.ShowOptions(true, true, true)
            content = calloutContent
            // set the leader position using the callout location
            setGeoElement(identifiedGraphic, calloutLocation)
            // show callout beneath graphic
            style.leaderPosition = Callout.Style.LeaderPosition.UPPER_MIDDLE
            // show callout
            if(!isShowing){
                show()
            }
        }
    }

    /**
     * Create a Picture Marker Symbol from the pin icon
     */
    private fun createPinSymbol() : PictureMarkerSymbol? {
        val pinDrawable = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.pin_location_removebg_preview) as BitmapDrawable ?
        val pinSymbol : PictureMarkerSymbol
        try {
            pinSymbol = PictureMarkerSymbol.createAsync(pinDrawable).get()
            pinSymbol.width = 125f
            pinSymbol.height = 100f
            return pinSymbol
        } catch (e : Exception){
            Toast.makeText(requireContext(), "Failed to load pin", Toast.LENGTH_LONG).show()
        }
        return null
    }

    /**
     * Recenter the mapView to the current location when currentLocationButton is pressed
     */
    private fun recenterToCurrentLocation(fab: FloatingActionButton) {
        fab.setOnClickListener {
            locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
            if (!locationDisplay.isStarted) locationDisplay.startAsync()
        }
    }
}