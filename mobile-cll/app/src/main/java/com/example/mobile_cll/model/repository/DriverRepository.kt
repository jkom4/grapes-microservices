package com.example.mobile_cll.model.repository

import com.example.mobile_cll.model.DatabaseHelper
import com.example.mobile_cll.model.entities.Driver

/**
 * DriverRepository is responsible for interacting with the database to fetch driver-related data.
 *
 * @param databaseHelper The helper class that handles database operations.
 */
class DriverRepository(private val databaseHelper: DatabaseHelper) {

    /**
     * Retrieves the driver data from the database.
     *
     * @return A Driver object if found, or null if no driver data is available.
     */
    fun getDriver(): Driver? {
        return databaseHelper.getDriver()
    }
}
