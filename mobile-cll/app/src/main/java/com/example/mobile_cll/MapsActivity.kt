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

class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapView: MapView
    private val mapsViewModel: MapsViewModel by viewModels()

    private var tripId: String? = null
    private var tripName: String? = null
    private var tripAddress: String? = null
    private var cameFromEmail: Boolean = false // Variable to check the origin of the navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve data from Intent
        tripId = intent.getStringExtra("tripId")
        tripName = intent.getStringExtra("tripName")
        tripAddress = intent.getStringExtra("tripAddress")
        cameFromEmail = intent.getBooleanExtra("cameFromEmail", false)

        Log.d("MapsActivity", "Received trip data: tripId=$tripId, tripName=$tripName, tripAddress=$tripAddress, cameFromEmail=$cameFromEmail")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->

            // Initialize MapView and set up the map
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
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
                return@getMapAsync
            }

            // Enable My Location layer
            googleMap.isMyLocationEnabled = true

            // Observe user location updates
            mapsViewModel.userLocation.observe(this, Observer { userLocation ->
                userLocation?.let {
                    mapView.updateMapWithUserLocation(it)

                    // Make the button visible once location is received
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

            // Observe client location updates
            mapsViewModel.clientLocation.observe(this, Observer { clientLatLng ->
                clientLatLng?.let {
                    mapView.addClientMarker(it, "$tripName", "$tripAddress")
                }
            })
        }
    }

    // Method for the "Arrived at destination" button click
    fun onArrivedButtonClick(view: android.view.View) {
        Toast.makeText(this, "Arrived at destination", Toast.LENGTH_SHORT).show()
        // Add navigation code or other actions here if necessary
    }
}
