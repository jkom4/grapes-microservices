package com.example.mobile_cll.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.example.mobile_cll.model.*

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
        tripId = id
        loadTripDetails()
    }

    private fun loadTripDetails() {
        viewModelScope.launch {
            isLoading = true
            delay(500)

            trip = Trip(
                id = tripId,
                name = "John Doe",
                distance = "15 mi",
                address = "6391 Elgin St. Celina, Delaware 10299",
            )

            orders = listOf(
                Order("1", "Product A", tripId = tripId, quantity = 2),
                Order("2", "Product B", tripId = tripId, quantity = 3),
                Order("3", "Product C", tripId = tripId, quantity =  5)
            )
            isLoading = false
        }
    }

    val deliveryRequestCount: Int
        get() = orders.size

    fun onScanClick(orderId: String) {
    }
}
