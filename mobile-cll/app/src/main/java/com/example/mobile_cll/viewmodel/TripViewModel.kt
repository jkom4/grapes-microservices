package com.example.mobile_cll.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_cll.model.entities.Order
import com.example.mobile_cll.model.entities.Trip
import com.example.mobile_cll.repository.OrderRepository
import com.example.mobile_cll.repository.TripRepository
import kotlinx.coroutines.launch

/**
 * ViewModel to manage the data and logic for the Home screen.
 * Handles fetching trips and navigation logic.
 */
class TripViewModel(
    private val tripRepository: TripRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> = _trips

    // Map to stock all order by tripId
    private val _ordersForTrips = MutableLiveData<Map<String, List<Order>>>()
    val ordersForTrips: LiveData<Map<String, List<Order>>> = _ordersForTrips

    init {
        fetchTrips()
    }

    /**
     * Function to fetch the list of trips.
     * This will be implemented to load data asynchronously.
     */
    fun fetchTrips() {
        viewModelScope.launch {
            _trips.value = tripRepository.getAllTrips()
        }
    }

    /**
     * Function to fetch all Orders corresponding to a tripId
     *
     * @param tripId The ID of the trip to navigate to.
     */
    fun getOrdersForTrip(tripId: String) {
        viewModelScope.launch {
            val orders = orderRepository.getOrdersForTrip(tripId)
            val updatedOrders = _ordersForTrips.value?.toMutableMap() ?: mutableMapOf()
            updatedOrders[tripId] = orders
            _ordersForTrips.value = updatedOrders
        }
    }

    /**
     * Function to navigate to the Trip Details screen with the given trip ID.
     *
     * @param tripId The ID of the trip to navigate to.
     */
    fun navigateToTripDetails(tripId: String) {
        // Implement navigation logic to the trip details screen
    }
}
