package com.example.mobile_cll.models

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * DatabaseHelper is a helper class for managing SQLite database operations in an Android application.
 * It handles creating, upgrading, and maintaining the database.
 * It includes the creation of three tables: trips, orders, and drivers, as well as inserting initial data.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Database constants
        private const val DATABASE_NAME = "TripDatabase.db"
        private const val DATABASE_VERSION = 24
        const val TABLE_TRIPS = "trips"
        const val TABLE_ORDERS = "orders"
        const val TABLE_DRIVERS = "drivers"
        const val TABLE_DELIVERY = "delivery"

        // Columns for trips table
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DISTANCE = "distance"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_IS_FINISHED = "isFinished"

        // Columns for orders table
        const val COLUMN_ORDER_ID = "id"
        const val COLUMN_PRODUCT_DESCRIPTION = "productDescription"
        const val COLUMN_QUANTITY = "quantity"
        const val COLUMN_TRIP_ID = "tripId"
        const val COLUMN_SCANNED_AT = "scannedAt"
        const val COLUMN_IS_SCANNED = "isScanned"

        // Columns for drivers table
        const val COLUMN_DRIVER_ID = "id"
        const val COLUMN_ROLE_DELIVERER = "role_deliverer"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_PHONE_CONF = "phone_conf"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_EMAIL_CONF = "email_conf"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_IS_RGPD_ACCEPTED = "is_rgpd_accepted"
        const val COLUMN_RGPD_ACCEPTED_AT = "rgpd_accepted_at"
        const val COLUMN_PASSWORD_HASH = "password_hash"
        const val COLUMN_COUNTRY = "country"
        const val COLUMN_CITY = "city"
        const val COLUMN_POSTAL_CODE = "postal_code"
        const val COLUMN_STREET = "street"
        const val COLUMN_LAST_NAME = "last_name"
        const val COLUMN_FIRST_NAME = "first_name"

        // Columns for delivery table
        const val COLUMN_DELIVERY_ID = "id"
        const val COLUMN_DELIVERY_ORDER_ID = "order_id"
        const val COLUMN_DELIVERY_USER_ID = "user_id"
        const val COLUMN_DELIVERY_STATUS_ID = "delivery_status_id"
        const val COLUMN_DELIVERY_DATE = "delivery_date"
        const val COLUMN_DELIVERED_AT = "delivered_at"
        const val COLUMN_DELIVERY_COMMENT = "comment"
        const val COLUMN_DELIVERY_DOORSTEP = "doorstep"
        const val COLUMN_DELIVERY_SIGNATURE = "signature"
        const val COLUMN_DELIVERY_PHOTO = "photo"
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
                $COLUMN_ADDRESS TEXT,
                $COLUMN_IS_FINISHED INTEGER DEFAULT 0
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
                $COLUMN_ROLE_DELIVERER INTEGER,
                $COLUMN_PHONE TEXT,
                $COLUMN_PHONE_CONF INTEGER, 
                $COLUMN_EMAIL TEXT,
                $COLUMN_EMAIL_CONF INTEGER,
                $COLUMN_CREATED_AT TEXT,
                $COLUMN_IS_RGPD_ACCEPTED INTEGER,
                $COLUMN_RGPD_ACCEPTED_AT TEXT,
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

        val createDeliveryTableQuery = """
            CREATE TABLE $TABLE_DELIVERY (
                $COLUMN_DELIVERY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DELIVERY_ORDER_ID TEXT,
                $COLUMN_DELIVERY_USER_ID INTEGER,
                $COLUMN_DELIVERY_STATUS_ID INTEGER,
                $COLUMN_DELIVERY_DATE TEXT,
                $COLUMN_DELIVERED_AT TEXT,
                $COLUMN_DELIVERY_COMMENT TEXT,
                $COLUMN_DELIVERY_DOORSTEP INTEGER,
                $COLUMN_DELIVERY_SIGNATURE BLOB,
                $COLUMN_DELIVERY_PHOTO BLOB,
                FOREIGN KEY ($COLUMN_DELIVERY_ORDER_ID) REFERENCES $TABLE_ORDERS($COLUMN_ORDER_ID),
                FOREIGN KEY ($COLUMN_DELIVERY_USER_ID) REFERENCES $TABLE_DRIVERS($COLUMN_DRIVER_ID)
            )
        """.trimIndent()
        db.execSQL(createDeliveryTableQuery)

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
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DELIVERY")
        onCreate(db)
    }

    /**
     * Inserts initial data into the trips, orders, and drivers tables.
     * This data will be used for testing and initial setup.
     *
     * @param db The database instance used to insert the data.
     */
    private fun insertInitialData(db: SQLiteDatabase) {
        TripSeeder.seed(db)
        OrderSeeder.seed(db)
        DeliveryDriverSeeder.seed(db)
        DeliverySeeder.seed(db)
    }
}