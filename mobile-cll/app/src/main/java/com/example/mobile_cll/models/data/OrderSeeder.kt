package com.example.mobile_cll.models

import android.database.sqlite.SQLiteDatabase
import com.example.mobile_cll.models.entities.Order

object OrderSeeder {
    private val orders = listOf(
        Order("1_1", "Product A", 2, "1", null),
        Order("2_1", "Product A", 2, "2", null),
        Order("2_2", "Product B", 4, "2", null),
        Order("3_1", "Product A", 2, "3", null),
        Order("3_2", "Product B", 4, "3", null),
        Order("3_3", "Product C", 6, "3", null),
        Order("4_1", "Product A", 2, "4", null),
        Order("4_2", "Product B", 4, "4", null),
        Order("4_3", "Product C", 6, "4", null),
        Order("4_4", "Product D", 8, "4", null),
        Order("5_1", "Product A", 2, "5", null),
        Order("5_2", "Product B", 4, "5", null),
        Order("5_3", "Product C", 6, "5", null),
        Order("5_4", "Product D", 8, "5", null),
        Order("5_5", "Product E", 10, "5", null),
        Order("6_1", "Product A", 2, "6", null),
        Order("6_2", "Product B", 4, "6", null),
        Order("6_3", "Product C", 6, "6", null),
        Order("6_4", "Product D", 8, "6", null),
        Order("6_5", "Product E", 10, "6", null),
        Order("6_6", "Product F", 12, "6", null),
        Order("7_1", "Product A", 2, "7", null),
        Order("7_2", "Product B", 4, "7", null),
        Order("7_3", "Product C", 6, "7", null),
        Order("7_4", "Product D", 8, "7", null),
        Order("7_5", "Product E", 10, "7", null),
        Order("7_6", "Product F", 12, "7", null),
        Order("7_7", "Product G", 14, "7", null),
        Order("8_1", "Product A", 2, "8", null),
        Order("8_2", "Product B", 4, "8", null),
        Order("8_3", "Product C", 6, "8", null),
        Order("8_4", "Product D", 8, "8", null),
        Order("8_5", "Product E", 10, "8", null),
        Order("8_6", "Product F", 12, "8", null),
        Order("8_7", "Product G", 14, "8", null),
        Order("8_8", "Product H", 16, "8", null),
        Order("9_1", "Product A", 2, "9", null),
        Order("9_2", "Product B", 4, "9", null),
        Order("9_3", "Product C", 6, "9", null),
        Order("9_4", "Product D", 8, "9", null),
        Order("9_5", "Product E", 10, "9", null),
        Order("9_6", "Product F", 12, "9", null),
        Order("9_7", "Product G", 14, "9", null),
        Order("9_8", "Product H", 16, "9", null),
        Order("9_9", "Product I", 18, "9", null),
        Order("10_1", "Product A", 2, "10", null),
        Order("10_2", "Product B", 4, "10", null),
        Order("10_3", "Product C", 6, "10", null),
        Order("10_4", "Product D", 8, "10", null),
        Order("10_5", "Product E", 10, "10", null),
        Order("10_6", "Product F", 12, "10", null),
        Order("10_7", "Product G", 14, "10", null),
        Order("10_8", "Product H", 16, "10", null),
        Order("10_9", "Product I", 18, "10", null),
        Order("10_10", "Product J", 20, "10", null)
    )

    fun seed(db: SQLiteDatabase) {
        orders.forEach { db.insert("orders", null, it.toContentValues()) }
    }
}