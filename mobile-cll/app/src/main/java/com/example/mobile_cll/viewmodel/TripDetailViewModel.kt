package com.example.mobile_cll.viewmodel

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
     * Load the trip ID and trigger loading the trip details.
     *
     * @param id The ID of the trip to be loaded.
     */
    fun loadId(id: String) {
        tripId = id
        loadTripDetails()
    }

    /**
     * Loads the trip details and associated orders.
     * This simulates an API call with a delay.
     */
    private fun loadTripDetails() {
        viewModelScope.launch {
            isLoading = true
            delay(500) // Simulate network delay

            // Simulated trip details
            trip = Trip(
                id = tripId,
                name = "John Doe",
                distance = "15 mi",
                address = "6391 Elgin St. Celina, Delaware 10299",
            )

            // Simulated list of orders for the trip
            orders = listOf(
                Order("1", "Product A", tripId = tripId, quantity = 2),
                Order("2", "Product B", tripId = tripId, quantity = 3),
                Order("3", "Product C", tripId = tripId, quantity =  5)
            )
            isLoading = false
        }
    }

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
}
