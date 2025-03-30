package com.example.mobile_cll.repository

import com.example.mobile_cll.model.DatabaseHelper
import com.example.mobile_cll.model.Trip

/**
 * TripRepository is responsible for interacting with the database to fetch trip-related data.
 *
 * @param databaseHelper The helper class that handles database operations.
 */
class TripRepository(private val databaseHelper: DatabaseHelper) {

    /**
     * Retrieves all trips from the database.
     *
     * @return A list of all trips.
     */
    fun getAllTrips(): List<Trip> {
        return databaseHelper.getAllTrips()
    }
}
