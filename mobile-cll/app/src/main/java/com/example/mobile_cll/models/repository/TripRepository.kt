package com.example.mobile_cll.repository

import com.example.mobile_cll.models.DatabaseHelper
import com.example.mobile_cll.models.DatabaseOperations
import com.example.mobile_cll.models.entities.Trip
import com.example.mobile_cll.network.RetrofitClient

/**
 * TripRepository is responsible for fetching trip-related data from the API and database.
 *
 * @param databaseHelper The helper class that handles database operations.
 */
class TripRepository(private val databaseHelper: DatabaseHelper) {
    private val dbOperations = DatabaseOperations(databaseHelper)
    private val apiService = RetrofitClient.getService(RetrofitClient.ApiService::class.java)

    /**
     * Retrieves all trips for a specific user from the API.
     *
     * @return A list of all trips for user ID "2".
     */
    suspend fun getAllTrips(): List<Trip> {
        return apiService.getTrips("2")
    }

    /**
     * Retrieves a specific trip by its ID from the database.
     *
     * @param tripId The ID of the trip to retrieve.
     * @return The Trip object if found, or null if not found.
     */
    fun getTrip(tripId: String): Trip? {
        return dbOperations.getTrip(tripId)
    }

    /**
     * Updates the isFinished status of a trip in the database.
     *
     * @param tripId The ID of the trip to update.
     * @param isFinished The new value for isFinished (true for finished, false otherwise).
     */
    fun updateTripFinished(tripId: String, isFinished: Boolean) {
        dbOperations.updateTripFinished(tripId, isFinished)
    }
}