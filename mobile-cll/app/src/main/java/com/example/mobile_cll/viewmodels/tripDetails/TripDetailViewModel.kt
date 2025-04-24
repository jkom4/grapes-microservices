package com.example.mobile_cll.viewmodels.tripDetails

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
 * and loading states. It interacts with the OrderRepository to fetch order data from the API.
 *
 * @param orderRepository The repository used to fetch orders for a given trip.
 */
class TripDetailsViewModel(private val orderRepository: OrderRepository) : ViewModel() {
    private val TAG = "TripDetailsViewModel"

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
            Log.d(TAG, "loadId called with trip: $trip, forceRefresh: $forceRefresh")
            viewModelScope.launch {
                _isLoading.value = true
                _trip.value = trip
                _tripId.value = trip.id
                try {
                    _orders.value = orderRepository.getOrdersForTrip(trip.id)
                    Log.d(TAG, "Orders loaded: ${_orders.value.map { "Order(orderItemId=${it.orderItemId}, scanned=${it.scanned})" }}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading orders: ${e.message}", e)
                    _orders.value = emptyList()
                }
                _deliveryRequestCount.value = _orders.value.size
                _isLoading.value = false
            }
        } else {
            Log.d(TAG, "No update needed, trip is unchanged: $trip")
        }
    }
}