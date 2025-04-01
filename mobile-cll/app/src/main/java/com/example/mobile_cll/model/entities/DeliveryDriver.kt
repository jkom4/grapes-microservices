package com.example.mobile_cll.model.entities

/**
 * Data class representing a Driver in the system.
 * This class holds all the details related to a driver such as personal information, contact details,
 * RGPD consent status, and other related attributes.
 */
data class DeliveryDriver(
    val id: Int,
    val roleDeliverer: Boolean,
    val phone: String,
    val phoneConf: Boolean,
    val email: String,
    val emailIsConfirmed: Boolean,
    val createdAt: String,
    val isRgpdAccepted: Boolean,
    val rgpdAcceptedAt: String?,
    val passwordHash: String,
    val country: String,
    val city: String,
    val postalCode: String,
    val street: String,
    val lastName: String,
    val firstName: String
)