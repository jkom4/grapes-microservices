package com.example.mobile_cll.repository

import android.content.Context
import com.example.mobile_cll.model.DatabaseHelper
import com.example.mobile_cll.model.entities.Delivery

class DeliveryRepository(context: Context) {
    private val databaseHelper = DatabaseHelper(context)

    suspend fun insertDelivery(delivery: Delivery): Long {
        return databaseHelper.insertDelivery(delivery)
    }

    suspend fun getDriverId(): Int? {
        return databaseHelper.getDriver()?.id
    }
}