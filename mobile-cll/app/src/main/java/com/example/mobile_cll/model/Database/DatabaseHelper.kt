package com.example.mobile_cll.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * DatabaseHelper is a helper class for managing SQLite database operations in an Android application.
 * It handles creating, upgrading, and maintaining the database.
 * It includes the creation of three tables: trips, orders, and drivers, as well as inserting initial data.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Database constants
        private const val DATABASE_NAME = "TripDatabase.db"
        private const val DATABASE_VERSION = 11
        private const val TABLE_TRIPS = "trips"
        private const val TABLE_ORDERS = "orders"
        private const val TABLE_DRIVERS = "drivers"

        // Columns for trips table
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_DISTANCE = "distance"
        private const val COLUMN_ADDRESS = "address"

        // Columns for orders table
        private const val COLUMN_ORDER_ID = "id"
        private const val COLUMN_PRODUCT_DESCRIPTION = "productDescription"
        private const val COLUMN_QUANTITY = "quantity"
        private const val COLUMN_TRIP_ID = "tripId"
        private const val COLUMN_SCANNED_AT = "scannedAt"
        private const val COLUMN_IS_SCANNED = "isScanned"

        // Columns for drivers table
        private const val COLUMN_DRIVER_ID = "id"
        private const val COLUMN_ROLE_DELIVERER = "role_deliverer"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_PHONE_CONF = "phone_conf"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_EMAIL_CONF = "email_conf"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_IS_RGPD_ACCEPTED = "is_rgpd_accepted"
        private const val COLUMN_RGPD_ACCEPTED_AT = "rgpd_accepted_at"
        private const val COLUMN_PASSWORD_HASH = "password_hash"
        private const val COLUMN_COUNTRY = "country"
        private const val COLUMN_CITY = "city"
        private const val COLUMN_POSTAL_CODE = "postal_code"
        private const val COLUMN_STREET = "street"
        private const val COLUMN_LAST_NAME = "last_name"
        private const val COLUMN_FIRST_NAME = "first_name"
    }

    /**
     * Called when the database is created for the first time.
     * Creates the trips, orders, and drivers tables.
     *
     * @param db The database instance used for creating the tables.
     */
    override fun onCreate(db: SQLiteDatabase) {
        // Create trips table
        val createTripsTableQuery = """
            CREATE TABLE $TABLE_TRIPS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT,
                $COLUMN_DISTANCE TEXT,
                $COLUMN_ADDRESS TEXT
            )
        """.trimIndent()
        db.execSQL(createTripsTableQuery)

        // Create orders table
        val createOrdersTableQuery = """
            CREATE TABLE $TABLE_ORDERS (
                $COLUMN_ORDER_ID TEXT PRIMARY KEY,
                $COLUMN_PRODUCT_DESCRIPTION TEXT,
                $COLUMN_QUANTITY INTEGER,
                $COLUMN_TRIP_ID TEXT,
                $COLUMN_SCANNED_AT TEXT,
                $COLUMN_IS_SCANNED INTEGER DEFAULT 0,
                FOREIGN KEY ($COLUMN_TRIP_ID) REFERENCES $TABLE_TRIPS($COLUMN_ID)
            )
        """.trimIndent()
        db.execSQL(createOrdersTableQuery)

        // Create drivers table
        val createDriversTableQuery = """
            CREATE TABLE $TABLE_DRIVERS (
                $COLUMN_DRIVER_ID INTEGER PRIMARY KEY,
                $COLUMN_ROLE_DELIVERER INTEGER,  -- BOOLEAN stocké comme INTEGER (0 ou 1)
                $COLUMN_PHONE TEXT,
                $COLUMN_PHONE_CONF INTEGER,  -- BOOLEAN stocké comme INTEGER (0 ou 1)
                $COLUMN_EMAIL TEXT,
                $COLUMN_EMAIL_CONF INTEGER,  -- BOOLEAN stocké comme INTEGER (0 ou 1)
                $COLUMN_CREATED_AT TEXT,  -- DATETIME stocké comme TEXT
                $COLUMN_IS_RGPD_ACCEPTED INTEGER,  -- BOOLEAN stocké comme INTEGER (0 ou 1)
                $COLUMN_RGPD_ACCEPTED_AT TEXT,  -- DATETIME stocké comme TEXT
                $COLUMN_PASSWORD_HASH TEXT,
                $COLUMN_COUNTRY TEXT,
                $COLUMN_CITY TEXT,
                $COLUMN_POSTAL_CODE TEXT,
                $COLUMN_STREET TEXT,
                $COLUMN_LAST_NAME TEXT,
                $COLUMN_FIRST_NAME TEXT
            )
        """.trimIndent()
        db.execSQL(createDriversTableQuery)

        // Insert initial data into the database
        insertInitialData(db)
    }

    /**
     * Called when the database version is upgraded.
     * Drops the existing tables and recreates them to reflect the updated schema.
     *
     * @param db The database instance used for upgrading the database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop the existing tables
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRIPS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DRIVERS")
        onCreate(db)
    }

    /**
     * Inserts initial data into the trips, orders, and drivers tables.
     * This data will be used for testing and initial setup.
     *
     * @param db The database instance used to insert the data.
     */
    private fun insertInitialData(db: SQLiteDatabase) {
        // Insert trips
        val trips = listOf(
            Trip("1", "Mathys", "10 mi", "Rue Joseph Truffaut 31, 4000 Liege"),
            Trip("2", "Cameron", "11 mi", "Avenue Louise 54, 1050 Bruxelles"),
            Trip("3", "Daive", "12 mi", "Chaussée de Charleroi 17, 1060 Bruxelles"),
            Trip("4", "Jobelin", "13 mi", "Rue du Trône 12, 1000 Bruxelles"),
            Trip("5", "Dounia", "14 mi", "Boulevard Anspach 20, 1000 Bruxelles"),
            Trip("6", "Nassim", "15 mi", "Rue Neuve 123, 1000 Bruxelles"),
            Trip("7", "Benjamin", "16 mi", "Place Flagey 18, 1050 Bruxelles"),
            Trip("8", "Nasser", "17 mi", "Rue de la Loi 200, 1040 Bruxelles"),
            Trip("9", "Charles", "18 mi", "Avenue de Tervuren 300, 1150 Bruxelles"),
            Trip("10", "Test", "19 mi", "Rue Royale 25, 1000 Bruxelles")
        )

        trips.forEach { trip ->
            val values = ContentValues().apply {
                put(COLUMN_ID, trip.id)
                put(COLUMN_NAME, trip.name)
                put(COLUMN_DISTANCE, trip.distance)
                put(COLUMN_ADDRESS, trip.address)
            }
            db.insert(TABLE_TRIPS, null, values)
        }

        // Insert orders
        val orders = listOf(
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

        orders.forEach { order ->
            val orderValues = ContentValues().apply {
                put(COLUMN_ORDER_ID, order.id)
                put(COLUMN_PRODUCT_DESCRIPTION, order.productDescription)
                put(COLUMN_QUANTITY, order.quantity)
                put(COLUMN_TRIP_ID, order.tripId)
                put(COLUMN_SCANNED_AT, order.scannedAt)
                put(COLUMN_IS_SCANNED, order.isScanned)
            }
            db.insert(TABLE_ORDERS, null, orderValues)
        }

        // Insert driver
        val driverValues = ContentValues().apply {
            put(COLUMN_DRIVER_ID, 1)
            put(COLUMN_ROLE_DELIVERER, 1) // BOOLEAN true -> 1
            put(COLUMN_PHONE, "1234567890")
            put(COLUMN_PHONE_CONF, 1) // BOOLEAN true -> 1
            put(COLUMN_EMAIL, "livreur@example.com")
            put(COLUMN_EMAIL_CONF, 1) // BOOLEAN true -> 1
            put(COLUMN_CREATED_AT, "2025-03-30 12:00:00")
            put(COLUMN_IS_RGPD_ACCEPTED, 1) // BOOLEAN true -> 1
            put(COLUMN_RGPD_ACCEPTED_AT, "2025-03-30 12:00:00")
            put(COLUMN_PASSWORD_HASH, "hashed_password_123")
            put(COLUMN_COUNTRY, "Belgium")
            put(COLUMN_CITY, "Brussels")
            put(COLUMN_POSTAL_CODE, "1000")
            put(COLUMN_STREET, "Avenue Louise 54")
            put(COLUMN_LAST_NAME, "Dupont")
            put(COLUMN_FIRST_NAME, "Jean")
        }
        db.insert(TABLE_DRIVERS, null, driverValues)
    }

    /**
     * Retrieves all trips from the database.
     *
     * @return A list of all trips.
     */
    fun getAllTrips(): List<Trip> {
        val tripList = mutableListOf<Trip>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRIPS", null)

        if (cursor.moveToFirst()) {
            do {
                val trip = Trip(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    distance = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)),
                    address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS))
                )
                tripList.add(trip)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return tripList
    }

    /**
     * Retrieves all orders for a specific trip from the database.
     *
     * @param tripId The ID of the trip for which orders need to be fetched.
     * @return A list of orders for the specified trip.
     */
    fun getOrdersForTrip(tripId: String): List<Order> {
        val orderList = mutableListOf<Order>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ORDERS WHERE $COLUMN_TRIP_ID = ?", arrayOf(tripId))

        if (cursor.moveToFirst()) {
            do {
                val order = Order(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORDER_ID)),
                    productDescription = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_DESCRIPTION)),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)),
                    tripId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRIP_ID)),
                    scannedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCANNED_AT))?.toLongOrNull(),
                    isScanned = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SCANNED)) == 1
                )
                orderList.add(order)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return orderList
    }

    // Method to fetch the current driver
    fun getDriver(): Driver? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_DRIVERS LIMIT 1", null)

        var driver: Driver? = null
        if (cursor.moveToFirst()) {
            driver = Driver(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DRIVER_ID)),
                roleDeliverer = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ROLE_DELIVERER)) == 1,
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                phoneConf = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PHONE_CONF)) == 1,
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                emailConf = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EMAIL_CONF)) == 1,
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                isRgpdAccepted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_RGPD_ACCEPTED)) == 1,
                rgpdAcceptedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RGPD_ACCEPTED_AT)),
                passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)),
                country = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY)),
                city = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY)),
                postalCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POSTAL_CODE)),
                street = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STREET)),
                lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME))
            )
        }

        cursor.close()
        db.close()
        return driver
    }

    fun clearDatabase() {
        writableDatabase.apply {
            execSQL("DELETE FROM $TABLE_TRIPS")
            execSQL("DELETE FROM $TABLE_ORDERS")
            execSQL("DELETE FROM $TABLE_DRIVERS")
            close()
        }
    }

    fun updateScannedAt(orderId: String, timestamp: Long) {
        val db = writableDatabase
        val currentTimestamp = timestamp.toString()

        val values = ContentValues().apply {
            put(COLUMN_SCANNED_AT, currentTimestamp)
            put(COLUMN_IS_SCANNED, 1)
        }

        val rowsUpdated = db.update(
            TABLE_ORDERS,
            values,
            "$COLUMN_ORDER_ID = ?",
            arrayOf(orderId)
        )

        if (rowsUpdated > 0) {
            Log.d("Update", "Order scannedAt and isScanned updated successfully for orderId: $orderId")
        } else {
            Log.e("UpdateError", "Failed to update scannedAt and isScanned for orderId: $orderId")
        }

        db.close()
    }
}
