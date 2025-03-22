package com.example.mobile_cll.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task

class LocationManager(private val context: Context, private val fusedLocationClient: FusedLocationProviderClient) {

    fun getUserLocation(): Task<Location>? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return fusedLocationClient.lastLocation
        } else {
            return null
        }
    }
}
