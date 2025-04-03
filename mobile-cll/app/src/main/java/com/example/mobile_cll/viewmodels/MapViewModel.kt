package com.example.mobile_cll.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

/**
 * MapsViewModel manages the location-related data for the MapsActivity.
 * It retrieves and updates the user's location, the client's location, and the delivery center's location.
 */
class MapsViewModel(application: Application) : AndroidViewModel(application) {

    val LATITUDE = 50.6201326
    val LONGITUDE = 5.5816244

    // FusedLocationProviderClient instance to access location services
    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    // LiveData to observe user location
    private val _userLocation = MutableLiveData<LatLng>()
    val userLocation: LiveData<LatLng> get() = _userLocation

    // LiveData for delivery center location
    private val _deliveryCenter = MutableLiveData<LatLng>()
    val deliveryCenter: LiveData<LatLng> get() = _deliveryCenter

    // LiveData for client location
    private val _clientLocation = MutableLiveData<LatLng>()
    val clientLocation: LiveData<LatLng> get() = _clientLocation


    init {
        _deliveryCenter.value = LatLng(LATITUDE, LONGITUDE) // Set initial delivery center location

        // Retrieve the user's last known location
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                _userLocation.value = LatLng(it.latitude, it.longitude) // Set user location if available
            }
        }
    }

    // Method to update the client's location
    fun updateClientLocation(clientLatLng: LatLng) {
        _clientLocation.value = clientLatLng // Update client location
    }
}
