package com.example.mobile_cll.model

data class Order(
    val id: String,
    val productDescription: String,
    val quantity: Int,
    val tripId: String,
    val scannedAt: String?
) {
    val isScanned: Boolean
        get() = scannedAt != null
}
