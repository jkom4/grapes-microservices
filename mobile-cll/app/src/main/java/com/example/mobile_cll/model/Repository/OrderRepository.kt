package com.example.mobile_cll.model.Repository

import com.example.mobile_cll.model.Order

class OrderRepository  {
    private val orders = listOf(
        Order(id = "1", productDescription = "Product A", tripId = "101", quantity = 2, scannedAt = null),
        Order(id = "2", productDescription = "Product B", tripId = "102", quantity = 3, scannedAt = null),
        Order(id = "3", productDescription = "Product C", tripId = "101", quantity = 5, scannedAt = null)
    )

    suspend fun getOrdersForTrip(tripId: String): List<Order> {
        return orders.filter { it.tripId == tripId }
    }

}
