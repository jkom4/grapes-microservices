package com.example.mobile_cll.model.entities

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