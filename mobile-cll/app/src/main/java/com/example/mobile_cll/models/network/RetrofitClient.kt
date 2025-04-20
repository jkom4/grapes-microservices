package com.example.mobile_cll.network

import android.util.Log
import com.example.mobile_cll.models.entities.Trip
import com.example.mobile_cll.models.entities.Order
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

object RetrofitClient {
    private const val BASE_URL = "http://192.168.129.10:8092/cll/"
    private const val TAG = "RetrofitClient"

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "OkHttp: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        Log.d(TAG, "Initializing OkHttpClient")
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Log.d(TAG, "Initializing Retrofit with base URL: $BASE_URL")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> getService(service: Class<T>): T {
        Log.d(TAG, "Creating service: ${service.simpleName}")
        return retrofit.create(service)
    }

    interface ApiService {
        @GET("trips/{userId}")
        suspend fun getTrips(@Path("userId") userId: String): List<Trip>

        @GET("trips/{tripId}/orders")
        suspend fun getOrdersForTrip(@Path("tripId") tripId: String): List<Order>

        @PATCH("trips/orders/{orderItemId}/scan")
        suspend fun scanOrder(@Path("orderItemId") orderItemId: String)

        @PATCH("deliveries/update-status/{orderId}")
        suspend fun updateDeliveryStatus(
            @Path("orderId") orderId: String,
            @Query("newStatus") newStatus: String
        )

        @PATCH("deliveries/feedback/{orderId}")
        suspend fun updateDeliveryFeedback(
            @Path("orderId") orderId: Int,
            @Body feedback: DeliveryFeedback
        )

        @PATCH("trips/{tripId}/finish")
        suspend fun finishTrip(@Path("tripId") tripId: String)
    }

    data class DeliveryFeedback(
        val orderId: Int,
        val comment: String,
        val doorstep: Boolean,
        val signature: String?,
        val deliveryStatusId: Int
    )
}