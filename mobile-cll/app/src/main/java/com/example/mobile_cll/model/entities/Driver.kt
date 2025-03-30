package com.example.mobile_cll.model.entities

/**
 * Data class representing a Driver in the system.
 * This class holds all the details related to a driver such as personal information, contact details,
 * RGPD consent status, and other related attributes.
 *
 * @param id The unique identifier for the driver.
 * @param roleDeliverer A boolean indicating whether the driver is a deliverer (true) or not (false).
 * @param phone The phone number of the driver.
 * @param phoneConf A boolean indicating whether the phone number has been confirmed (true) or not (false).
 * @param email The email address of the driver.
 * @param emailConf A boolean indicating whether the email address has been confirmed (true) or not (false).
 * @param createdAt The date and time when the driver account was created, stored as a string in datetime format.
 * @param isRgpdAccepted A boolean indicating whether the driver has accepted the RGPD (General Data Protection Regulation) consent (true) or not (false).
 * @param rgpdAcceptedAt The date and time when the RGPD consent was accepted, stored as a string in datetime format. This field can be null if the consent is not given.
 * @param passwordHash The hashed version of the driver's password for secure authentication.
 * @param country The country of the driver.
 * @param city The city where the driver resides.
 * @param postalCode The postal code of the driver's address.
 * @param street The street name and number of the driver's address.
 * @param lastName The driver's last name.
 * @param firstName The driver's first name.
 */
data class Driver(
    val id: Int,
    val roleDeliverer: Boolean,
    val phone: String,
    val phoneConf: Boolean,
    val email: String,
    val emailConf: Boolean,
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