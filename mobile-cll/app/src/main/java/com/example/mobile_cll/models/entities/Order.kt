package com.example.mobile_cll.models.entities

/**
 * Data class representing an Order in the system.
 */
data class Order(
    val orderItemId: Int,
    val productDescription: String,
    val quantity: Int,
    val tripId: Int,
    val scanned: Boolean
)