package com.example.mobile_cll.repository

import com.example.mobile_cll.model.DatabaseHelper
import com.example.mobile_cll.model.entities.Order

/**
 * OrderRepository is responsible for interacting with the database to fetch order-related data.
 *
 * @param databaseHelper The helper class that handles database operations.
 */
class OrderRepository(private val databaseHelper: DatabaseHelper) {

    /**
     * Retrieves all orders associated with a given trip.
     *
     * @param tripId The ID of the trip for which to fetch orders.
     * @return A list of orders for the specified trip.
     */
    fun getOrdersForTrip(tripId: String): List<Order> {
        return databaseHelper.getOrdersForTrip(tripId)
    }

    fun updateScannedAt(order: Order, timestamp: Long) {
        order.scannedAt = timestamp
        order.isScanned = true
        databaseHelper.updateScannedAt(order.id, timestamp)
    }
}