package com.example.mobile_cll.views.screens

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.util.Locale

/**
 * MapView is responsible for managing the Google Map and providing map-related functionalities.
 * It handles the setup of the map, adding markers for the delivery center and client,
 * and updating the map with user location or client location.
 */
class MapView(
    private val context: Context,
    private val googleMap: GoogleMap
) {

    private val deliveryCenterLatLng = LatLng(50.6201326, 5.5816244)

    /**
     * Sets up the map by adding a marker for the delivery center and moving the camera.
     */
    fun setupMap() {
        googleMap.addMarker(
            MarkerOptions()
                .position(deliveryCenterLatLng)
                .title("Delivery Center")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(deliveryCenterLatLng))
    }

    /**
     * Adds a marker for the client's location on the map and zooms the camera.
     *
     * @param clientLatLng The LatLng representing the client's location.
     * @param title The title to display in the marker.
     * @param address The address to display in the marker's snippet.
     */
    fun addClientMarker(clientLatLng: LatLng, title: String, address: String) {
        googleMap.addMarker(
            MarkerOptions()
                .position(clientLatLng)
                .title(title)
                .snippet(address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(clientLatLng, 15f))
    }

    /**
     * Updates the map to center and zoom on the user's current location.
     *
     * @param userLocation The LatLng representing the user's location.
     */
    fun updateMapWithUserLocation(userLocation: LatLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
    }
    /**
     * Converts a given address to a LatLng (latitude and longitude) using the Geocoder.
     *
     * @param address The address to convert to LatLng.
     * @return The corresponding LatLng if the address is found, or null if not.
     */
    fun getLatLngFromAddress(address: String): LatLng? {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)

            // Check if addresses is not null and contains elements
            addresses?.firstOrNull()?.let {
                return LatLng(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            Log.e("MapView", "Error getting LatLng from address: ${e.localizedMessage}")
        }
        return null
    }
}
