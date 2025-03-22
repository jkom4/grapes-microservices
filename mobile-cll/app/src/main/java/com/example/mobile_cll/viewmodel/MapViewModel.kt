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

    // Utilisation de LocationServices pour obtenir une instance de FusedLocationProviderClient
    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    private val _userLocation = MutableLiveData<LatLng>()
    val userLocation: LiveData<LatLng> get() = _userLocation

    private val _deliveryCenter = MutableLiveData<LatLng>()
    val deliveryCenter: LiveData<LatLng> get() = _deliveryCenter

    init {
        _deliveryCenter.value = LatLng(50.6201326, 5.5816244)

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                _userLocation.value = LatLng(it.latitude, it.longitude)
            }
        }
    }
}
