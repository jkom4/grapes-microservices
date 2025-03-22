package com.example.mobile_cll.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.example.mobile_cll.model.* // Assure-toi que tes classes Trip et Order sont bien importées

class TripDetailsViewModel : ViewModel() {

    var tripId by mutableStateOf("")
        private set
    var trip by mutableStateOf<Trip?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var orders by mutableStateOf<List<Order>>(emptyList())
        private set

    fun updateTripId(id: String) {
        Log.d("TripDetailsViewModel", "updateTripId called with id: $id")
        tripId = id
        loadTripDetails()
    }

    private fun loadTripDetails() {
        Log.d("TripDetailsViewModel", "Loading trip details for tripId: $tripId")
        viewModelScope.launch {
            isLoading = true

            delay(500)

            trip = Trip(
                id = tripId,
                name = "John Doe",
                distance = "15 mi",
                address = "6391 Elgin St. Celina, Delaware 10299"
            )

            orders = listOf(
                Order("1", "Product A", tripId = tripId, quantity = 2),
                Order("2", "Product B", tripId = tripId, quantity = 3),
                Order("3", "Product C", tripId = tripId, quantity = 5)
            )

            Log.d("TripDetailsViewModel", "Trip loaded: $trip, Orders: $orders")

            isLoading = false
        }
    }

    val deliveryRequestCount: Int
        get() = orders.size

    fun onScanClick(orderId: String) {
    }

    fun updateTrip(trip: Trip) {
        if (this@TripDetailsViewModel.trip != trip) {
            Log.d("TripDetailsViewModel", "updateTrip called with trip: $trip")
            viewModelScope.launch {
                isLoading = true // Le chargement commence
                delay(500) // Simuler un délai de mise à jour

                // Mettre à jour le voyage
                this@TripDetailsViewModel.trip = trip
                orders = listOf(
                    Order("1", "Product A", tripId = trip.id, quantity = 2),
                    Order("2", "Product B", tripId = trip.id, quantity = 3),
                    Order("3", "Product C", tripId = trip.id, quantity = 5)
                )

                Log.d("TripDetailsViewModel", "Trip updated: $trip, Orders: $orders")

                isLoading = false // Le chargement est terminé
            }
        }
    }
}
