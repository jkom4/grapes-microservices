package com.example.mobile_cll.model.repository

import com.example.mobile_cll.model.DatabaseHelper
import com.example.mobile_cll.model.DatabaseOperations
import com.example.mobile_cll.model.entities.DeliveryDriver

/**
 * DriverRepository is responsible for interacting with the database to fetch driver-related data.
 *
 * @param databaseHelper The helper class that handles database operations.
 */
class DeliveryDriverRepository(private val databaseHelper: DatabaseHelper) {
    private val dbOperations = DatabaseOperations(databaseHelper)
    /**
     * Retrieves the driver data from the database.
     *
     * @return A Driver object if found, or null if no driver data is available.
     */
    fun getDeliveryDriver(): DeliveryDriver? {
        return dbOperations.getDeliveryDriver()
    }
}
