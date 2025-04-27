package com.example.mobile_cll.repository

import com.example.mobile_cll.models.entities.Trip
import com.example.mobile_cll.network.RetrofitClient

/**
 * TripRepository is responsible for fetching trip-related data from the API and database.
 *
 */
class TripRepository() {
    private val apiService = RetrofitClient.getService(RetrofitClient.ApiService::class.java)

    /**
     * Retrieves all trips for a specific user from the API.
     *
     * @return A list of all trips for user ID "2".
     */
    suspend fun getAllTrips(): List<Trip> {
        return apiService.getTrips("2")
    }
}
