package com.example.mobile_cll.model.entities

/**
 * Represents a delivery entity.
 *
 * @property id Unique identifier of the delivery.
 * @property orderId Identifier of the related order.
 * @property userId Identifier of the user associated with the delivery.
 * @property deliveryStatusId Status identifier of the delivery.
 * @property deliveryDate Scheduled date for the delivery.
 * @property deliveredAt Timestamp of when the delivery was completed.
 * @property comment Additional comments regarding the delivery.
 * @property doorstep Indicates whether the delivery was left at the doorstep.
 * @property signature Digital signature of the recipient (if applicable).
 * @property photo Photo proof of the delivery (if available).
 */
data class Delivery(
    val id: Int = 0,
    val orderId: String,
    val userId: Int,
    val deliveryStatusId: Int,
    val deliveryDate: String,
    val deliveredAt: String,
    val comment: String,
    val doorstep: Boolean,
    val signature: ByteArray?,
    val photo: ByteArray?
)