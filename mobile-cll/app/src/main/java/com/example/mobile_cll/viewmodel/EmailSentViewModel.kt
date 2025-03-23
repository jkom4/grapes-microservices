package com.example.mobile_cll.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EmailSentViewModel : ViewModel() {

    // State to control whether navigation should occur
    var shouldNavigate = mutableStateOf(false)

    // Method to trigger navigation after a delay
    fun handleNavigationAfterDelay() {
        viewModelScope.launch {
            delay(5000) // Wait for 5 seconds
            shouldNavigate.value = true // Set navigation flag to true after delay
        }
    }
}
