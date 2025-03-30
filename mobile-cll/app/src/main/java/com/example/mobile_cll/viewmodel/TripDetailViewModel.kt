package com.example.mobile_cll.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_cll.model.Order
import com.example.mobile_cll.model.Trip
import com.example.mobile_cll.repository.OrderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the details of a trip, including loading associated orders.
 *
 * @param orderRepository The repository for fetching orders associated with a trip.
 */
class TripDetailsViewModel(private val orderRepository: OrderRepository) : ViewModel() {

    var tripId by mutableStateOf("")
        private set

    var trip by mutableStateOf<Trip?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var orders by mutableStateOf<List<Order>>(emptyList())
        private set

    val deliveryRequestCount: Int
        get() = orders.size

    /**
     * Placeholder function for handling order scan clicks. Currently not implemented.
     *
     * @param orderId The ID of the order that was scanned.
     */
    fun onScanClick(orderId: String) {
    }

    /**
     * Loads the details of the provided trip, including associated orders.
     *
     * @param trip The trip object whose details need to be loaded.
     */
    fun loadId(trip: Trip) {
        if (this@TripDetailsViewModel.trip != trip) {
            Log.d("TripDetailsViewModel", "updateTrip called with trip: $trip")
            viewModelScope.launch {
                isLoading = true
                delay(500)
                this@TripDetailsViewModel.trip = trip
                this@TripDetailsViewModel.tripId = trip.id
                orders = orderRepository.getOrdersForTrip(trip.id)
                Log.d("TripDetailsViewModel", "Orders loaded: $orders")

                isLoading = false
            }
        }
    }
}
