package com.example.mobile_cll.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mobile_cll.model.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener

class MapsViewModel(application: Application) : AndroidViewModel(application) {

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
        _deliveryCenter.value = LatLng(50.6201326, 5.5816244) // Set initial delivery center location

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
