package com.example.mobile_cll.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EmailSentViewModel : ViewModel() {

    var shouldNavigate = mutableStateOf(false)

    fun handleNavigationAfterDelay() {
        viewModelScope.launch {
            delay(5000)
            shouldNavigate.value = true
        }
    }
}
