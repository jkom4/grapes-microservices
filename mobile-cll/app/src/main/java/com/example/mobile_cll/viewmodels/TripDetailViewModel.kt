package com.example.mobile_cll.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_cll.models.entities.Order
import com.example.mobile_cll.models.entities.Trip
import com.example.mobile_cll.repository.OrderRepository
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing trip details, including trip data, associated orders,
 * and loading states. It interacts with the OrderRepository to fetch order data.
 *
 * @param orderRepository The repository used to fetch orders for a given trip.
 */
class TripDetailsViewModel(private val orderRepository: OrderRepository) : ViewModel() {

    private val _tripId = mutableStateOf("")
    val tripId: State<String> = _tripId

    private val _trip = mutableStateOf<Trip?>(null)
    val trip: State<Trip?> = _trip

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _orders = mutableStateOf<List<Order>>(emptyList())
    val orders: State<List<Order>> = _orders

    private val _deliveryRequestCount = mutableStateOf(0)
    val deliveryRequestCount: State<Int> = _deliveryRequestCount

    /**
     * Loads trip data and associated orders into the ViewModel. Updates the state only if the trip
     * has changed or a refresh is forced.
     *
     * @param trip The trip object containing details to load.
     * @param forceRefresh Whether to force a reload of data, even if the trip hasn't changed. Defaults to false.
     */
    fun loadId(trip: Trip, forceRefresh: Boolean = false) {
        if (_trip.value != trip || forceRefresh) {
            Log.d("TripDetailsViewModel", "loadId called with trip: $trip, forceRefresh: $forceRefresh")
            viewModelScope.launch {
                _isLoading.value = true
                _trip.value = trip
                _tripId.value = trip.id
                _orders.value = orderRepository.getOrdersForTrip(trip.id)
                _deliveryRequestCount.value = _orders.value.size // Update the count of delivery requests
                Log.d("TripDetailsViewModel", "Orders loaded: ${_orders.value.map { "Order(id=${it.id}, isScanned=${it.isScanned})" }}")
                _isLoading.value = false
            }
        } else {
            Log.d("TripDetailsViewModel", "No update needed, trip is unchanged: $trip")
        }
    }

    /**
     * Forces a reload of orders for the current trip, if a trip is already loaded.
     */
    fun refreshOrders() {
        _trip.value?.let {
            loadId(it, forceRefresh = true)
        }
    }

    /**
     * Placeholder function for handling order scan clicks. Currently not implemented.
     *
     * @param orderId The ID of the order that was scanned.
     */
    fun onScanClick(orderId: String) {
        // Can be used for additional actions if needed
    }
}