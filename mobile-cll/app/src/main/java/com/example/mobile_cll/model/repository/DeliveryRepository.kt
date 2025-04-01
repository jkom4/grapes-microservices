package com.example.mobile_cll.repository

import android.content.Context
import com.example.mobile_cll.model.DatabaseHelper
import com.example.mobile_cll.model.DatabaseOperations
import com.example.mobile_cll.model.entities.Delivery

/**
 * Repository class for handling Delivery-related database operations.
 *
 * @param context The application context used to initialize the database helper.
 */
class DeliveryRepository(context: Context) {
    private val dbOperations = DatabaseOperations(DatabaseHelper(context))

    /**
     * Inserts a new delivery record into the database.
     *
     * @param delivery The delivery entity to be inserted.
     * @return The ID of the inserted delivery record.
     */
    suspend fun insertDelivery(delivery: Delivery): Long {
        return dbOperations.insertDelivery(delivery)
    }

    /**
     * Retrieves the driver's ID from the database.
     *
     * @return The driver's ID if found, otherwise null.
     */
    suspend fun getDeliveryDriverId(): Int? {
        return dbOperations.getDeliveryDriver()?.id
    }
}
