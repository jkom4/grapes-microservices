package com.example.mobile_cll.repository

import com.example.mobile_cll.models.DatabaseHelper
import com.example.mobile_cll.models.DatabaseOperations
import com.example.mobile_cll.models.entities.Trip

/**
 * TripRepository is responsible for interacting with the database to fetch trip-related data.
 *
 * @param databaseHelper The helper class that handles database operations.
 */
class TripRepository(private val databaseHelper: DatabaseHelper) {
    private val dbOperations = DatabaseOperations(databaseHelper)

    /**
     * Retrieves all trips from the database.
     *
     * @return A list of all trips.
     */
    fun getAllTrips(): List<Trip> {
        return dbOperations.getAllTrips()
    }

    /**
     * Retrieves a specific trip by its ID.
     *
     * @param tripId The ID of the trip to retrieve.
     * @return The Trip object if found, or null if not found.
     */
    fun getTrip(tripId: String): Trip? {
        return dbOperations.getTrip(tripId)
    }

    /**
     * Updates the isFinished status of a trip.
     *
     * @param tripId The ID of the trip to update.
     * @param isFinished The new value for isFinished (true for finished, false otherwise).
     */
    fun updateTripFinished(tripId: String, isFinished: Boolean) {
        dbOperations.updateTripFinished(tripId, isFinished)
    }
}