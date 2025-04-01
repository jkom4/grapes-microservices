package com.example.mobile_cll.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobile_cll.models.DatabaseHelper
import com.example.mobile_cll.repository.OrderRepository
import com.example.mobile_cll.repository.TripRepository

/**
 * Factory class for creating instances of HomeViewModel.
 *
 * @param context The context to initialize the DatabaseHelper.
 */
class TripViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    /**
     * Creates an instance of HomeViewModel with the necessary repositories.
     *
     * @param modelClass The ViewModel class to be created.
     * @return An instance of HomeViewModel.
     * @throws IllegalArgumentException If the ViewModel class is unknown.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            val databaseHelper = DatabaseHelper(context)
            val repository = TripRepository(databaseHelper)
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(repository, orderRepository = OrderRepository(databaseHelper)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}