package com.example.mobile_cll.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobile_cll.models.DatabaseHelper
import com.example.mobile_cll.repository.TripRepository

/**
 * Factory class for creating an instance of [CompletedOrderViewModel].
 * Ensures the ViewModel is instantiated with the required dependencies.
 */
class CompletedOrderViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompletedOrderViewModel::class.java)) {
            val databaseHelper = DatabaseHelper(context)
            val tripRepository = TripRepository(databaseHelper)
            @Suppress("UNCHECKED_CAST")
            return CompletedOrderViewModel(context, tripRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}