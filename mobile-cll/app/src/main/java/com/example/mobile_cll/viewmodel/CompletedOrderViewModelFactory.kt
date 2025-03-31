package com.example.mobile_cll.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobile_cll.model.DatabaseHelper
import com.example.mobile_cll.repository.TripRepository

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