package com.example.mobile_cll.model.entities

/**
 * Data class representing an Order in the system.
 * This class holds details about a specific order, including product information,
 * quantity, associated trip, and scanning status.
 *
 * @param id The unique identifier for the order.
 * @param productDescription A brief description of the product being ordered.
 * @param quantity The quantity of the product being ordered.
 * @param tripId The unique identifier for the trip associated with this order.
 * @param scannedAt The timestamp when the order was scanned, or null if it has not been scanned yet.
 */
data class Order(
    val id: String,
    val productDescription: String,
    val quantity: Int,
    val tripId: String,
    var scannedAt: Long?=null,
    var isScanned: Boolean = false,
)
