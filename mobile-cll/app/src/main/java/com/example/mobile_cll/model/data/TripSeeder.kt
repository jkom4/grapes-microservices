package com.example.mobile_cll.model

import android.database.sqlite.SQLiteDatabase
import com.example.mobile_cll.model.entities.Trip

object TripSeeder {
    private val trips = listOf(
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

    fun seed(db: SQLiteDatabase) {
        trips.forEach { db.insert("trips", null, it.toContentValues()) }
    }
}