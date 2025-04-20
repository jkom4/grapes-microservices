package com.example.mobile_cll.network

import android.util.Log
import com.example.mobile_cll.models.entities.Trip
import com.example.mobile_cll.models.entities.Order
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Singleton object to provide a configured Retrofit instance for API calls.
 */
object RetrofitClient {
    private const val BASE_URL = "http://192.168.129.10:8092/cll/"
    private const val TAG = "RetrofitClient"

    // Create an OkHttpClient with a logging interceptor for debugging
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "OkHttp: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY // Log request and response body
        }
        Log.d(TAG, "Initializing OkHttpClient")
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // Create and configure the Retrofit instance
    private val retrofit: Retrofit by lazy {
        Log.d(TAG, "Initializing Retrofit with base URL: $BASE_URL")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Get a service instance for the specified API interface.
     *
     * @param service The API interface class (e.g., ApiService::class.java).
     * @return An implementation of the API interface.
     */
    fun <T> getService(service: Class<T>): T {
        Log.d(TAG, "Creating service: ${service.simpleName}")
        return retrofit.create(service)
    }

    /**
     * Interface defining API endpoints for trip-related operations.
     */
    interface ApiService {
        /**
         * Fetches all trips for a given user ID.
         *
         * @param userId The ID of the user whose trips are to be fetched.
         * @return A list of Trip objects.
         */
        @GET("trips/{userId}")
        suspend fun getTrips(@Path("userId") userId: String): List<Trip>

        /**
         * Fetches all orders for a given trip ID.
         *
         * @param tripId The ID of the trip whose orders are to be fetched.
         * @return A list of Order objects.
         */
        @GET("trips/{tripId}/orders")
        suspend fun getOrdersForTrip(@Path("tripId") tripId: String): List<Order>

        /**
         * Marks an order as scanned by sending a PATCH request.
         *
         * @param orderItemId The ID of the order item to mark as scanned.
         */
        @PATCH("trips/orders/{orderItemId}/scan")
        suspend fun scanOrder(@Path("orderItemId") orderItemId: String)

        /**
         * Updates the status of a delivery for a given order ID.
         *
         * @param orderId The ID of the order to update.
         * @param newStatus The new status to set (e.g., "In Progress").
         */
        @PATCH("deliveries/update-status/{orderId}")
        suspend fun updateDeliveryStatus(
            @Path("orderId") orderId: String,
            @Query("newStatus") newStatus: String
        )
    }
}