package com.example.mobile_cll.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.example.mobile_cll.model.* // Ensure Trip and Order classes are imported

class TripDetailsViewModel : ViewModel() {

    // State variables for trip details, loading status, and orders
    var tripId by mutableStateOf("") // Trip ID, initially empty
        private set
    var trip by mutableStateOf<Trip?>(null) // Holds the trip object, initially null
        private set
    var isLoading by mutableStateOf(true) // Flag indicating loading state
        private set
    var orders by mutableStateOf<List<Order>>(emptyList()) // List of orders for the trip
        private set

    // Update the trip ID and trigger loading the trip details
    fun updateTripId(id: String) {
        Log.d("TripDetailsViewModel", "updateTripId called with id: $id")
        tripId = id
        loadTripDetails()
    }

    // Simulate loading trip details (fetching data)
    private fun loadTripDetails() {
        Log.d("TripDetailsViewModel", "Loading trip details for tripId: $tripId")
        viewModelScope.launch {
            isLoading = true // Start loading

            delay(500) // Simulate network delay

            // Mock data for trip and orders
            trip = Trip(
                id = tripId,
                name = "John Doe",
                distance = "15 mi",
                address = "6391 Elgin St. Celina, Delaware 10299"
            )

            orders = listOf(
                Order(id = "1", productDescription = "Product A", tripId = tripId, quantity = 2, scannedAt = null),
                Order(id = "2", productDescription = "Product B", tripId = tripId, quantity = 3, scannedAt = null),
                Order(id = "3", productDescription = "Product C", tripId = tripId, quantity = 5, scannedAt = null)
            )

            Log.d("TripDetailsViewModel", "Trip loaded: $trip, Orders: $orders")

            isLoading = false // End loading
        }
    }

    // Computed property to return the count of orders
    val deliveryRequestCount: Int
        get() = orders.size

    // Placeholder function for scan action (no action defined yet)
    fun onScanClick(orderId: String) {
    }

    // Update the trip with new data (if changed)
    fun updateTrip(trip: Trip) {
        if (this@TripDetailsViewModel.trip != trip) {
            Log.d("TripDetailsViewModel", "updateTrip called with trip: $trip")
            viewModelScope.launch {
                isLoading = true // Start loading
                delay(500) // Simulate network delay

                // Update trip and orders
                this@TripDetailsViewModel.trip = trip
                orders = listOf(
                    Order(id = "1", productDescription = "Product A", tripId = tripId, quantity = 2, scannedAt = null),
                    Order(id = "2", productDescription = "Product B", tripId = tripId, quantity = 3, scannedAt = null),
                    Order(id = "3", productDescription = "Product C", tripId = tripId, quantity = 5, scannedAt = null)
                )

                Log.d("TripDetailsViewModel", "Trip updated: $trip, Orders: $orders")

                isLoading = false // End loading
            }
        }
    }
}
