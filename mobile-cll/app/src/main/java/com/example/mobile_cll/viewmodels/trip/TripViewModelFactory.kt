package com.example.mobile_cll.viewmodels.trip

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(TripRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}