package com.example.mobile_cll.model.entities

/**
 * Data class representing a Trip in the system.
 * This class contains details about a specific trip, including its name, distance, and address.
 *
 * @param id The unique identifier for the trip.
 * @param name The name of the trip.
 * @param distance The distance covered by the trip.
 * @param address The address associated with the trip.
 */
data class Trip(
    val id: String,
    val name: String,
    val distance: String,
    val address: String,
    val isFinished: Boolean = false
)
