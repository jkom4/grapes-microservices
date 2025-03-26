package com.example.mobile_cll.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel to handle the logic for email sending and navigation after a delay.
 */
class EmailSentViewModel : ViewModel() {

    // State to control whether navigation should occur
    var shouldNavigate = mutableStateOf(false)

    /**
     * This function waits for 5 seconds before triggering the provided navigation callback.
     *
     * @param onNavigationComplete The callback function that gets triggered after the delay.
     */
    fun handleNavigationAfterDelay() {
        viewModelScope.launch {
            delay(5000) // Wait for 5 seconds
            shouldNavigate.value = true // Set navigation flag to true after delay
        }
    }
}