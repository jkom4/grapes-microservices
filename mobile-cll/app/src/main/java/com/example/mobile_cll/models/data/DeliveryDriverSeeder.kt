package com.example.mobile_cll.models

import android.database.sqlite.SQLiteDatabase
import com.example.mobile_cll.models.entities.DeliveryDriver

object DeliveryDriverSeeder {
    private val driver = DeliveryDriver(
        id = 1,
        roleDeliverer = true,
        phone = "1234567890",
        phoneConf = true,
        email = "livreur@example.com",
        emailIsConfirmed = true,
        createdAt = "2025-03-30 12:00:00",
        isRgpdAccepted = true,
        rgpdAcceptedAt = "2025-03-30 12:00:00",
        passwordHash = "hashed_password_123",
        country = "Belgium",
        city = "Brussels",
        postalCode = "1000",
        street = "Avenue Louise 54",
        lastName = "Dupont",
        firstName = "Jean"
    )

    fun seed(db: SQLiteDatabase) {
        db.insert("drivers", null, driver.toContentValues())
    }
}