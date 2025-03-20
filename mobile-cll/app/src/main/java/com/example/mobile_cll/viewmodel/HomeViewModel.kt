package com.example.mobile_cll.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_cll.model.Order
import kotlinx.coroutines.launch
import com.example.mobile_cll.model.Trip

class HomeViewModel : ViewModel() {

    fun fetchTrips() {
        viewModelScope.launch {
        }
    }



    fun navigateToTripDetails(tripId: String) {
    }
}
