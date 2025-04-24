package grapes.microservices.models.repository

import android.util.Log
import grapes.microservices.models.data.Order
import grapes.microservices.models.network.OrderApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository(
    private val api: OrderApiService
) {
    suspend fun getOrderHistory(
        userId: Int,
        page: Int,
        size: Int,
        code: Int? = null,
        date: String? = null
    ): List<Order> {
        return withContext(Dispatchers.IO) {
            try {
                val orders = api.getOrderHistory(userId, page, size, code, date)
                Log.d("OrderRepository", "Fetched ${orders.size} orders for user $userId, page $page")
                orders
            } catch (e: Exception) {
                Log.e("OrderRepository", "Failed to fetch orders: ${e.message}", e)
                emptyList()
            }
        }
    }
}