package com.example.mobile_cll.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class ScanViewModel : ViewModel() {
    // Holds the scanned code
    var scanCode by mutableStateOf("")
        private set // Prevents external modification

    // Indicates if there's an error (e.g., empty code)
    var isError by mutableStateOf(false)
        private set

    // Updates the scan code and resets the error state
    fun onScanCodeChange(newCode: String) {
        scanCode = newCode
        isError = false
    }

    // Validates and submits the scanned code
    fun onSubmitScanCode() {
        if (scanCode.isEmpty()) {
            isError = true // Mark as error if empty
        } else {
            submitScanCode(scanCode)
        }
    }

    // Simulates submission of the scanned code
    private fun submitScanCode(code: String) {
        println("Code submitted: $code")
    }
}