package com.example.mobile_cll.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel to handle the logic for email sending and navigation after a delay.
 */
class EmailSentViewModel : ViewModel() {

    /**
     * This function waits for 5 seconds before triggering the provided navigation callback.
     *
     * @param onNavigationComplete The callback function that gets triggered after the delay.
     */
    fun handleNavigationAfterDelay(onNavigationComplete: () -> Unit) {
        // Launch a coroutine to handle the delay
        viewModelScope.launch {
            delay(5000) // Wait for 5 seconds
            onNavigationComplete() // Trigger the navigation callback
        }
    }
}
