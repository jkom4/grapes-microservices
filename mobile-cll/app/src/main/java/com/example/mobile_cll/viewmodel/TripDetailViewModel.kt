package com.example.mobile_cll.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.example.mobile_cll.model.*

/**
 * ViewModel to manage the details of a specific trip.
 * Handles fetching trip details, loading orders, and managing loading state.
 */
class TripDetailsViewModel : ViewModel() {
    // The ID of the trip being viewed
    var tripId by mutableStateOf("")
        private set

    // The trip details object
    var trip by mutableStateOf<Trip?>(null)
        private set

    // The loading state of the trip details
    var isLoading by mutableStateOf(true)
        private set

    // List of orders associated with the trip
    var orders by mutableStateOf<List<Order>>(emptyList())
        private set

    /**
     * Loads the trip details and associated orders.
     * This simulates an API call with a delay.
     */

    // Returns the number of delivery requests (orders) for the trip
    val deliveryRequestCount: Int
        get() = orders.size

    /**
     * Function called when the scan button is clicked for an order.
     *
     * @param orderId The ID of the order to be processed.
     */
    fun onScanClick(orderId: String) {
        // Handle scan action for the order
    }

    /**
     * This function updates the current trip details and loads associated orders.
     * It checks if the trip passed is different from the current one, and if so, it updates the trip details.
     */
    fun loadId(trip: Trip) {
        // Check if the trip passed is different from the current one
        if (this@TripDetailsViewModel.trip != trip) {
            Log.d("TripDetailsViewModel", "updateTrip called with trip: $trip")

            // Start loading the trip details asynchronously
            viewModelScope.launch {
                isLoading = true // Indicate that the loading process has started
                delay(500) // Simulate a delay for updating

                // Update the current trip details
                this@TripDetailsViewModel.trip = trip

                // Load orders associated with the trip
                orders = listOf(
                    Order(id = "1", productDescription = "Product A", tripId = tripId, quantity = 2, scannedAt = null),
                    Order(id = "2", productDescription = "Product B", tripId = tripId, quantity = 3, scannedAt = null),
                    Order(id = "3", productDescription = "Product C", tripId = tripId, quantity = 5, scannedAt = null)
                )

                // Mark loading as complete
                isLoading = false // Indicate that the loading process is finished
            }
        }
    }
}
