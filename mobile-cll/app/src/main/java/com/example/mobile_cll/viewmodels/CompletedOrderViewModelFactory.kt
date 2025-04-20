package com.example.mobile_cll.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

class CompletedOrderViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(CompletedOrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CompletedOrderViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}