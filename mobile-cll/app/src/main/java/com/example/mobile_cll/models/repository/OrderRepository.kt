package com.example.mobile_cll.repository

import android.util.Log
import com.example.mobile_cll.models.entities.Order
import com.example.mobile_cll.network.RetrofitClient

/**
 * Repository for fetching order-related data from the API.
 */
class OrderRepository {
    private val apiService = RetrofitClient.getService(RetrofitClient.ApiService::class.java)
    private val TAG = "OrderRepository"

    /**
     * Retrieves all orders for a specific trip from the API.
     *
     * @param tripId The ID of the trip whose orders are to be fetched.
     * @return A list of orders for the trip.
     */
    suspend fun getOrdersForTrip(tripId: String): List<Order> {
        Log.d(TAG, "Fetching orders for tripId=$tripId")
        return try {
            val orders = apiService.getOrdersForTrip(tripId)
            Log.d(TAG, "Received ${orders.size} orders: $orders")
            orders
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching orders: ${e.message}", e)
            emptyList()
        }
    }
}