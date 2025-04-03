package com.example.mobile_cll.models.entities

/**
 * Data class representing an Order in the system.
 * This class holds details about a specific order, including product information,
 * quantity, associated trip, and scanning status.
 */
data class Order(
    val id: String,
    val productDescription: String,
    val quantity: Int,
    val tripId: String,
    var scannedAt: Long?=null,
    var isScanned: Boolean = false,
)
