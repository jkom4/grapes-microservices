package com.example.mobile_cll.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobile_cll.models.DatabaseHelper
import com.example.mobile_cll.repository.TripRepository

/**
 * Factory class for creating instances of TripViewModel.
 *
 * @param context The context to initialize the DatabaseHelper.
 */
class TripViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    /**
     * Creates an instance of TripViewModel with the TripRepository.
     *
     * @param modelClass The ViewModel class to be created.
     * @return An instance of TripViewModel.
     * @throws IllegalArgumentException If the ViewModel class is unknown.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            val databaseHelper = DatabaseHelper(context)
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(TripRepository(databaseHelper)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}