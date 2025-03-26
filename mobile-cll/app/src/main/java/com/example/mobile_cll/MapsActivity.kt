package com.example.mobile_cll

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.example.mobile_cll.databinding.ActivityMapsBinding
import com.example.mobile_cll.view.MapView
import com.example.mobile_cll.viewmodel.MapsViewModel
import com.google.android.gms.maps.SupportMapFragment
import android.util.Log
import android.widget.Toast

/**
 * MapsActivity is responsible for displaying a map and handling trip-related information.
 * It retrieves trip data from the Intent, sets up the map, tracks user and client locations,
 * and displays relevant markers and navigation options.
 */
class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapView: MapView
    private val mapsViewModel: MapsViewModel by viewModels()

    private var tripId: String? = null
    private var tripName: String? = null
    private var tripAddress: String? = null
    private var cameFromEmail: Boolean = false // Variable to check the origin of the navigation

    /**
     * Called when the activity is first created.
     * This method initializes the UI, retrieves the trip data from Intent, and sets up the map view.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and set the content view
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve trip-related data from Intent
        tripId = intent.getStringExtra("tripId")  // The ID of the trip being shown
        tripName = intent.getStringExtra("tripName")  // The name of the trip
        tripAddress = intent.getStringExtra("tripAddress")  // The destination address of the trip
        cameFromEmail = intent.getBooleanExtra("cameFromEmail", false)  // Check if navigation is from email

        Log.d("MapsActivity", "Received trip data: tripId=$tripId, tripName=$tripName, tripAddress=$tripAddress, cameFromEmail=$cameFromEmail")

        // Initialize the map fragment and setup the map asynchronously
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->

            // Initialize MapView and setup the map
            mapView = MapView(this, googleMap)
            mapView.setupMap()

            // Check for location permissions
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request location permissions if not granted
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
                return@getMapAsync
            }

            // Enable the 'My Location' layer on the map
            googleMap.isMyLocationEnabled = true

            // Observe user location and update the map with user's location
            mapsViewModel.userLocation.observe(this, Observer { userLocation ->
                userLocation?.let {
                    mapView.updateMapWithUserLocation(it)

                    // Make the "Arrived" button visible once the user's location is received
                    if (cameFromEmail) {
                        binding.arrivedButton.visibility = android.view.View.VISIBLE
                    }
                }
            })

            // Handle the trip address and convert it to LatLng if available
            tripAddress?.let {
                val tripLatLng = mapView.getLatLngFromAddress(it)
                tripLatLng?.let {
                    mapsViewModel.updateClientLocation(it)
                } ?: run {
                    Log.e("MapsActivity", "Trip address not found, placing marker at fallback location.")
                }
            }

            // Observe client location updates and display it on the map
            mapsViewModel.clientLocation.observe(this, Observer { clientLatLng ->
                clientLatLng?.let {
                    mapView.addClientMarker(it, "$tripName", "$tripAddress")
                }
            })
        }
    }

    /**
     * Method for handling the click event of the "Arrived at destination" button.
     * Displays a toast message when the button is clicked.
     */
    fun onArrivedButtonClick(view: android.view.View) {
        Toast.makeText(this, "Arrived at destination", Toast.LENGTH_SHORT).show()
        // Additional actions (e.g., navigation) can be added here
    }
}
