package com.example.mobile_cll.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobile_cll.models.DatabaseHelper
import com.example.mobile_cll.repository.OrderRepository

/**
 * Factory class for creating instances of TripDetailsViewModel.
 *
 * @param context The context to initialize the DatabaseHelper.
 */
class TripDetailsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    /**
     * Creates an instance of TripDetailsViewModel with the necessary repository.
     *
     * @param modelClass The ViewModel class to be created.
     * @return An instance of TripDetailsViewModel.
     * @throws IllegalArgumentException If the ViewModel class is unknown.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripDetailsViewModel::class.java)) {
            val databaseHelper = DatabaseHelper(context)
            val orderRepository = OrderRepository(databaseHelper)
            @Suppress("UNCHECKED_CAST")
            return TripDetailsViewModel(orderRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
