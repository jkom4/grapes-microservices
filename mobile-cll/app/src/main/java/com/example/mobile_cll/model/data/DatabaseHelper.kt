package com.example.mobile_cll.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.mobile_cll.model.entities.DeliveryDriver
import com.example.mobile_cll.model.entities.Order
import com.example.mobile_cll.model.entities.Trip

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "TripDatabase.db"
        private const val DATABASE_VERSION = 23
        private const val TABLE_TRIPS = "trips"
        private const val TABLE_ORDERS = "orders"
        private const val TABLE_DRIVERS = "drivers"
    }

    private object Tables {
        const val TRIPS_CREATE = """
            CREATE TABLE $TABLE_TRIPS (
                id TEXT PRIMARY KEY,
                name TEXT,
                distance TEXT,
                address TEXT
            )
        """
        const val ORDERS_CREATE = """
            CREATE TABLE $TABLE_ORDERS (
                id TEXT PRIMARY KEY,
                productDescription TEXT,
                quantity INTEGER,
                tripId TEXT,
                scannedAt TEXT,
                isScanned INTEGER DEFAULT 0,
                FOREIGN KEY (tripId) REFERENCES $TABLE_TRIPS(id)
            )
        """
        const val DRIVERS_CREATE = """
            CREATE TABLE $TABLE_DRIVERS (
                id INTEGER PRIMARY KEY,
                role_deliverer INTEGER,
                phone TEXT,
                phone_conf INTEGER,
                email TEXT,
                email_conf INTEGER,
                created_at TEXT,
                is_rgpd_accepted INTEGER,
                rgpd_accepted_at TEXT,
                password_hash TEXT,
                country TEXT,
                city TEXT,
                postal_code TEXT,
                street TEXT,
                last_name TEXT,
                first_name TEXT
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(Tables.TRIPS_CREATE)
        db.execSQL(Tables.ORDERS_CREATE)
        db.execSQL(Tables.DRIVERS_CREATE)
        seedDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRIPS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DRIVERS")
        onCreate(db)
    }

    private fun seedDatabase(db: SQLiteDatabase) {
        TripSeeder.seed(db)
        OrderSeeder.seed(db)
        DeliveryDriverSeeder.seed(db)
    }

    fun getAllTrips(): List<Trip> = readableDatabase.use { db ->
        db.rawQuery("SELECT * FROM $TABLE_TRIPS", null).use { cursor ->
            mutableListOf<Trip>().apply {
                if (cursor.moveToFirst()) {
                    do {
                        add(cursor.toTrip())
                    } while (cursor.moveToNext())
                }
            }
        }
    }

    fun updateTripFinished(tripId: String, isFinished: Boolean) = writableDatabase.use { db ->
        val rowsUpdated = db.update(TABLE_TRIPS, ContentValues(), "id = ?", arrayOf(tripId))
        Log.d("DatabaseHelper", if (rowsUpdated > 0) "Trip $tripId finished: $isFinished" else "Error update trip: $tripId")
    }

    fun getTrip(tripId: String): Trip? = readableDatabase.use { db ->
        db.rawQuery("SELECT * FROM $TABLE_TRIPS WHERE id = ?", arrayOf(tripId)).use { cursor ->
            if (cursor.moveToFirst()) cursor.toTrip() else null
        }
    }

    fun getOrdersForTrip(tripId: String): List<Order> = readableDatabase.use { db ->
        db.rawQuery("SELECT * FROM $TABLE_ORDERS WHERE tripId = ?", arrayOf(tripId)).use { cursor ->
            mutableListOf<Order>().apply {
                if (cursor.moveToFirst()) {
                    do {
                        add(cursor.toOrder())
                    } while (cursor.moveToNext())
                }
            }
        }
    }

    fun getDriver(): DeliveryDriver? = readableDatabase.use { db ->
        db.rawQuery("SELECT * FROM $TABLE_DRIVERS LIMIT 1", null).use { cursor ->
            if (cursor.moveToFirst()) cursor.toDriver() else null
        }
    }

    fun clearDatabase() = writableDatabase.use { db ->
        db.execSQL("DELETE FROM $TABLE_TRIPS")
        db.execSQL("DELETE FROM $TABLE_ORDERS")
        db.execSQL("DELETE FROM $TABLE_DRIVERS")
    }

    fun updateScannedAt(orderId: String, timestamp: Long) = writableDatabase.use { db ->
        val values = ContentValues().apply {
            put("scannedAt", timestamp.toString())
            put("isScanned", 1)
        }
        val rowsUpdated = db.update(TABLE_ORDERS, values, "id = ?", arrayOf(orderId))
        Log.d("Update", if (rowsUpdated > 0) "Order scannedAt updated: $orderId" else "Failed update: $orderId")
    }
}

// Extension functions
fun Trip.toContentValues() = ContentValues().apply {
    put("id", id)
    put("name", name)
    put("distance", distance)
    put("address", address)
}

fun Order.toContentValues() = ContentValues().apply {
    put("id", id)
    put("productDescription", productDescription)
    put("quantity", quantity)
    put("tripId", tripId)
    put("scannedAt", scannedAt)
    put("isScanned", if (isScanned) 1 else 0)
}

fun DeliveryDriver.toContentValues() = ContentValues().apply {
    put("id", id)
    put("role_deliverer", if (roleDeliverer) 1 else 0)
    put("phone", phone)
    put("phone_conf", if (phoneConf) 1 else 0)
    put("email", email)
    put("email_conf", if (emailIsConfirmed) 1 else 0)
    put("created_at", createdAt)
    put("is_rgpd_accepted", if (isRgpdAccepted) 1 else 0)
    put("rgpd_accepted_at", rgpdAcceptedAt)
    put("password_hash", passwordHash)
    put("country", country)
    put("city", city)
    put("postal_code", postalCode)
    put("street", street)
    put("last_name", lastName)
    put("first_name", firstName)
}

private fun android.database.Cursor.toTrip() = Trip(
    id = getString(getColumnIndexOrThrow("id")),
    name = getString(getColumnIndexOrThrow("name")),
    distance = getString(getColumnIndexOrThrow("distance")),
    address = getString(getColumnIndexOrThrow("address"))
)

private fun android.database.Cursor.toOrder() = Order(
    id = getString(getColumnIndexOrThrow("id")),
    productDescription = getString(getColumnIndexOrThrow("productDescription")),
    quantity = getInt(getColumnIndexOrThrow("quantity")),
    tripId = getString(getColumnIndexOrThrow("tripId")),
    scannedAt = getString(getColumnIndexOrThrow("scannedAt"))?.toLongOrNull(),
    isScanned = getInt(getColumnIndexOrThrow("isScanned")) == 1
)

private fun android.database.Cursor.toDriver() = DeliveryDriver(
    id = getInt(getColumnIndexOrThrow("id")),
    roleDeliverer = getInt(getColumnIndexOrThrow("role_deliverer")) == 1,
    phone = getString(getColumnIndexOrThrow("phone")),
    phoneConf = getInt(getColumnIndexOrThrow("phone_conf")) == 1,
    email = getString(getColumnIndexOrThrow("email")),
    emailIsConfirmed = getInt(getColumnIndexOrThrow("email_conf")) == 1,
    createdAt = getString(getColumnIndexOrThrow("created_at")),
    isRgpdAccepted = getInt(getColumnIndexOrThrow("is_rgpd_accepted")) == 1,
    rgpdAcceptedAt = getString(getColumnIndexOrThrow("rgpd_accepted_at")),
    passwordHash = getString(getColumnIndexOrThrow("password_hash")),
    country = getString(getColumnIndexOrThrow("country")),
    city = getString(getColumnIndexOrThrow("city")),
    postalCode = getString(getColumnIndexOrThrow("postal_code")),
    street = getString(getColumnIndexOrThrow("street")),
    lastName = getString(getColumnIndexOrThrow("last_name")),
    firstName = getString(getColumnIndexOrThrow("first_name"))
)