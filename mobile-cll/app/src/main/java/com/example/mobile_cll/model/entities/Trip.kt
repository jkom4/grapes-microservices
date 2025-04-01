package com.example.mobile_cll.model.entities

/**
 * Data class representing a Trip in the system.
 * This class contains details about a specific trip, including its name, distance, and address.
 */
data class Trip(
    val id: String,
    val name: String,
    val distance: String,
    val address: String,
)
