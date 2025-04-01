package com.example.mobile_cll.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.mobile_cll.model.entities.Delivery
import com.example.mobile_cll.model.entities.DeliveryDriver
import com.example.mobile_cll.model.entities.Order
import com.example.mobile_cll.model.entities.Trip

/**
 * Provides database operations for the TripDatabase.
 * Contains methods to retrieve, update, and insert data into the database tables.
 */
class DatabaseOperations(private val dbHelper: DatabaseHelper) {

    /**
     * Retrieves all trips from the database.
     *
     * @return A list of all trips.
     */
    fun getAllTrips(): List<Trip> {
        val tripList = mutableListOf<Trip>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_TRIPS}", null)

        if (cursor.moveToFirst()) {
            do {
                val trip = Trip(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)),
                    distance = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DISTANCE)),
                    address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS)),
                    isFinished = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_FINISHED)) == 1
                )
                tripList.add(trip)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return tripList
    }

    fun updateTripFinished(tripId: String, isFinished: Boolean) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_IS_FINISHED, if (isFinished) 1 else 0)
        }

        val rowsUpdated = db.update(
            DatabaseHelper.TABLE_TRIPS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(tripId)
        )

        if (rowsUpdated > 0) {
            Log.d("DatabaseHelper", "Trip $tripId finished : $isFinished")
        } else {
            Log.e("DatabaseHelper", "Error update trip: $tripId")
        }

        db.close()
    }

    fun getTrip(tripId: String): Trip? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_TRIPS} WHERE ${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(tripId)
        )
        var trip: Trip? = null

        if (cursor.moveToFirst()) {
            trip = Trip(
                id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)),
                distance = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DISTANCE)),
                address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS)),
                isFinished = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_FINISHED)) == 1
            )
        }

        cursor.close()
        db.close()
        return trip
    }

    /**
     * Retrieves all orders for a specific trip from the database.
     *
     * @param tripId The ID of the trip for which orders need to be fetched.
     * @return A list of orders for the specified trip.
     */
    fun getOrdersForTrip(tripId: String): List<Order> {
        val orderList = mutableListOf<Order>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_ORDERS} WHERE ${DatabaseHelper.COLUMN_TRIP_ID} = ?",
            arrayOf(tripId)
        )

        if (cursor.moveToFirst()) {
            do {
                val order = Order(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_ID)),
                    productDescription = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_DESCRIPTION)),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY)),
                    tripId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRIP_ID)),
                    scannedAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCANNED_AT))?.toLongOrNull(),
                    isScanned = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_SCANNED)) == 1
                )
                orderList.add(order)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return orderList
    }

    // Method to fetch the current driver
    fun getDriver(): DeliveryDriver? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_DRIVERS} LIMIT 1", null)

        var driver: DeliveryDriver? = null
        if (cursor.moveToFirst()) {
            driver = DeliveryDriver(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DRIVER_ID)),
                roleDeliverer = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE_DELIVERER)) == 1,
                phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE)),
                phoneConf = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE_CONF)) == 1,
                email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)),
                emailIsConfirmed = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL_CONF)) == 1,
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)),
                isRgpdAccepted = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_RGPD_ACCEPTED)) == 1,
                rgpdAcceptedAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RGPD_ACCEPTED_AT)),
                passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD_HASH)),
                country = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COUNTRY)),
                city = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CITY)),
                postalCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POSTAL_CODE)),
                street = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STREET)),
                lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME)),
                firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME))
            )
        }

        cursor.close()
        db.close()
        return driver
    }

    fun clearDatabase() {
        dbHelper.writableDatabase.apply {
            execSQL("DELETE FROM ${DatabaseHelper.TABLE_TRIPS}")
            execSQL("DELETE FROM ${DatabaseHelper.TABLE_ORDERS}")
            execSQL("DELETE FROM ${DatabaseHelper.TABLE_DRIVERS}")
            close()
        }
    }

    fun updateScannedAt(orderId: String, timestamp: Long) {
        val db = dbHelper.writableDatabase
        val currentTimestamp = timestamp.toString()

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SCANNED_AT, currentTimestamp)
            put(DatabaseHelper.COLUMN_IS_SCANNED, 1)
        }

        val rowsUpdated = db.update(
            DatabaseHelper.TABLE_ORDERS,
            values,
            "${DatabaseHelper.COLUMN_ORDER_ID} = ?",
            arrayOf(orderId)
        )

        if (rowsUpdated > 0) {
            Log.d("Update", "Order scannedAt and isScanned updated successfully for orderId: $orderId")
        } else {
            Log.e("UpdateError", "Failed to update scannedAt and isScanned for orderId: $orderId")
        }
    }

    /**
     * Inserts a new delivery record into the delivery table.
     *
     * @param delivery The Delivery object to insert.
     * @return The row ID of the newly inserted delivery record.
     */
    fun insertDelivery(delivery: Delivery): Long {
        val db = dbHelper.writableDatabase
        val values = delivery.toContentValues()
        val rowId = db.insert(DatabaseHelper.TABLE_DELIVERY, null, values)
        db.close()
        return rowId
    }
}

// Extension functions for ContentValues
fun Trip.toContentValues() = ContentValues().apply {
    put(DatabaseHelper.COLUMN_ID, id)
    put(DatabaseHelper.COLUMN_NAME, name)
    put(DatabaseHelper.COLUMN_DISTANCE, distance)
    put(DatabaseHelper.COLUMN_ADDRESS, address)
    put(DatabaseHelper.COLUMN_IS_FINISHED, if (isFinished) 1 else 0)
}

fun Order.toContentValues() = ContentValues().apply {
    put(DatabaseHelper.COLUMN_ORDER_ID, id)
    put(DatabaseHelper.COLUMN_PRODUCT_DESCRIPTION, productDescription)
    put(DatabaseHelper.COLUMN_QUANTITY, quantity)
    put(DatabaseHelper.COLUMN_TRIP_ID, tripId)
    put(DatabaseHelper.COLUMN_SCANNED_AT, scannedAt)
    put(DatabaseHelper.COLUMN_IS_SCANNED, if (isScanned) 1 else 0)
}

fun DeliveryDriver.toContentValues() = ContentValues().apply {
    put(DatabaseHelper.COLUMN_DRIVER_ID, id)
    put(DatabaseHelper.COLUMN_ROLE_DELIVERER, if (roleDeliverer) 1 else 0)
    put(DatabaseHelper.COLUMN_PHONE, phone)
    put(DatabaseHelper.COLUMN_PHONE_CONF, if (phoneConf) 1 else 0)
    put(DatabaseHelper.COLUMN_EMAIL, email)
    put(DatabaseHelper.COLUMN_EMAIL_CONF, if (emailIsConfirmed) 1 else 0)
    put(DatabaseHelper.COLUMN_CREATED_AT, createdAt)
    put(DatabaseHelper.COLUMN_IS_RGPD_ACCEPTED, if (isRgpdAccepted) 1 else 0)
    put(DatabaseHelper.COLUMN_RGPD_ACCEPTED_AT, rgpdAcceptedAt)
    put(DatabaseHelper.COLUMN_PASSWORD_HASH, passwordHash)
    put(DatabaseHelper.COLUMN_COUNTRY, country)
    put(DatabaseHelper.COLUMN_CITY, city)
    put(DatabaseHelper.COLUMN_POSTAL_CODE, postalCode)
    put(DatabaseHelper.COLUMN_STREET, street)
    put(DatabaseHelper.COLUMN_LAST_NAME, lastName)
    put(DatabaseHelper.COLUMN_FIRST_NAME, firstName)
}

private fun Delivery.toContentValues() = ContentValues().apply {
    put(DatabaseHelper.COLUMN_DELIVERY_ORDER_ID, orderId)
    put(DatabaseHelper.COLUMN_DELIVERY_USER_ID, userId)
    put(DatabaseHelper.COLUMN_DELIVERY_STATUS_ID, deliveryStatusId)
    put(DatabaseHelper.COLUMN_DELIVERY_DATE, deliveryDate)
    put(DatabaseHelper.COLUMN_DELIVERED_AT, deliveredAt)
    put(DatabaseHelper.COLUMN_DELIVERY_COMMENT, comment)
    put(DatabaseHelper.COLUMN_DELIVERY_DOORSTEP, if (doorstep) 1 else 0)
    put(DatabaseHelper.COLUMN_DELIVERY_SIGNATURE, signature)
    put(DatabaseHelper.COLUMN_DELIVERY_PHOTO, photo)
}