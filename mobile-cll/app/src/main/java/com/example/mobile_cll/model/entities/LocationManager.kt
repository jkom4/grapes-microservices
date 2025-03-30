package com.example.mobile_cll.model.entities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task

/**
 * LocationManager class responsible for retrieving the user's last known location
 * using Google's FusedLocationProviderClient API.
 *
 * @param context The application context, used to check location permissions.
 * @param fusedLocationClient A location client provided by Google to obtain the last known location.
 */
class LocationManager(private val context: Context, private val fusedLocationClient: FusedLocationProviderClient) {

    /**
     * Retrieves the user's last known location.
     *
     * @return A Task<Location> containing the location if permissions are granted,
     *         otherwise returns null.
     */
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
            // Return the last known location as an asynchronous task
            return fusedLocationClient.lastLocation
        } else {
            // Return null if permissions are not granted
            return null
        }
    }
}
