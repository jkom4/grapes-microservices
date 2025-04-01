package com.example.mobile_cll.model

import android.database.sqlite.SQLiteDatabase
import com.example.mobile_cll.model.entities.Delivery

object DeliverySeeder {
    /**
     * Inserts initial data into the delivery table.
     * This data will be used for testing and initial setup.
     *
     * @param db The database instance used to insert the data.
     */
    fun seed(db: SQLiteDatabase) {
        /*
        val deliveries = listOf(
            Delivery("1_1", 1, 1, "2025-03-30", null, "Test comment", true, null, null)
        )
        deliveries.forEach { delivery ->
            db.insert("delivery", null, delivery.toContentValues())
        }
        */
    }
}