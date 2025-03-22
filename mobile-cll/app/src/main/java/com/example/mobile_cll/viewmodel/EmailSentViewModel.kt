package com.example.mobile_cll.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EmailSentViewModel : ViewModel() {

    fun handleNavigationAfterDelay(onNavigationComplete: () -> Unit) {
        viewModelScope.launch {
            delay(5000)
            onNavigationComplete()
        }
    }
}
