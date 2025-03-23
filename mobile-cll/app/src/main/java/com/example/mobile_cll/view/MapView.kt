package com.example.mobile_cll.view

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.util.Locale

class MapView(
    private val context: Context,
    private val googleMap: GoogleMap
) {

    private val deliveryCenterLatLng = LatLng(50.6201326, 5.5816244)

    fun setupMap() {
        googleMap.addMarker(
            MarkerOptions()
                .position(deliveryCenterLatLng)
                .title("Delivery Center")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(deliveryCenterLatLng))
    }

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

    fun updateMapWithUserLocation(userLocation: LatLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
    }

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
