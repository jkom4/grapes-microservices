package com.example.mobile_cll.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_cll.models.entities.Trip
import com.example.mobile_cll.repository.TripRepository
import kotlinx.coroutines.launch

/**
 * ViewModel to manage trip data for the Home screen.
 * Handles fetching trips from the API.
 */
class TripViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> = _trips

    init { fetchTrips() }

    /**
     * Fetches the list of trips from the API.
     */
    fun fetchTrips() {
        viewModelScope.launch {
            try {
                _trips.value = tripRepository.getAllTrips()
            } catch (e: Exception) {
                _trips.value = emptyList()
                println("Error fetching trips: ${e.message}")
            }
        }
    }
}