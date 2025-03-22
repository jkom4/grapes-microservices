package com.example.mobile_cll

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.example.mobile_cll.databinding.ActivityMapsBinding
import com.example.mobile_cll.viewmodel.MapsViewModel
import android.location.Geocoder
import android.util.Log
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val mapsViewModel: MapsViewModel by viewModels()

    private val deliveryCenterLatLng = LatLng(50.6201326, 5.5816244) // Point de livraison par défaut
    private val fallbackLatLng = LatLng(50.0, 5.0) // Point de secours en cas d'échec de l'adresse

    // Récupérer les données passées de EmailSentScreen
    private var tripId: String? = null
    private var tripName: String? = null
    private var tripAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Récupérer les données envoyées via l'Intent
        tripId = intent.getStringExtra("tripId")
        tripName = intent.getStringExtra("tripName")
        tripAddress = intent.getStringExtra("tripAddress")

        Log.d("MapsActivity", "Received trip data: tripId=$tripId, tripName=$tripName, tripAddress=$tripAddress")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Observer la position actuelle de l'utilisateur
        mapsViewModel.userLocation.observe(this, Observer { userLocation ->
            userLocation?.let {
                if (::mMap.isInitialized) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                }
            }
        })

        // Géocoder pour obtenir la latitude et longitude à partir de l'adresse du voyage
        tripAddress?.let {
            val tripLatLng = getLatLngFromAddress(it)
            tripLatLng?.let {
                // Mettre à jour la position du client dans le ViewModel
                mapsViewModel.updateClientLocation(it)
            } ?: run {
                // Si l'adresse n'est pas trouvée, placer un marqueur à un autre endroit par défaut
                Log.e("MapsActivity", "Trip address not found, placing marker at fallback location.")
                mapsViewModel.updateClientLocation(fallbackLatLng)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

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
            return
        }

        mMap.isMyLocationEnabled = true

        mMap.addMarker(
            MarkerOptions()
                .position(deliveryCenterLatLng)
                .title("Centrale de Livraison")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(deliveryCenterLatLng))

        // Observer la position du client et ajouter un marqueur
        mapsViewModel.clientLocation.observe(this, Observer { clientLatLng ->
            clientLatLng?.let {
                addClientMarker(it, "$tripName", "$tripAddress")
            }
        })
    }

    // Fonction pour ajouter un marqueur avec l'adresse du client
    private fun addClientMarker(location: LatLng, title: String, address: String) {
        mMap.addMarker(
            MarkerOptions()
                .position(location)
                .title(title)
                .snippet(address)  // Afficher l'adresse dans les détails du marqueur
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // Marqueur rouge pour le client
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun getLatLngFromAddress(address: String): LatLng? {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)

            // Safe call pour vérifier si addresses n'est pas null et non vide
            addresses?.let {
                if (it.isNotEmpty()) {
                    val location = it[0]
                    return LatLng(location.latitude, location.longitude)
                }
            }
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error getting LatLng from address: ${e.localizedMessage}")
        }
        return null // Retourne null si aucune adresse n'a été trouvée
    }
}