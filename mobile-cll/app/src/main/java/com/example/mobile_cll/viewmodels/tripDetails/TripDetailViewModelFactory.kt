package com.example.mobile_cll.viewmodels.tripDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobile_cll.repository.OrderRepository

/**
 * Factory class for creating instances of TripDetailsViewModel.
 */
class TripDetailsViewModelFactory : ViewModelProvider.Factory {

    /**
     * Creates an instance of TripDetailsViewModel with the necessary repository.
     *
     * @param modelClass The ViewModel class to be created.
     * @return An instance of TripDetailsViewModel.
     * @throws IllegalArgumentException If the ViewModel class is unknown.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripDetailsViewModel(OrderRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}