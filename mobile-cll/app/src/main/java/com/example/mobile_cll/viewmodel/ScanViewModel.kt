package com.example.mobile_cll.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class ScanViewModel : ViewModel() {
    // State variables to hold the scan code and error status
    var scanCode by mutableStateOf("") // Holds the scanned code, initially empty
        private set

    var isError by mutableStateOf(false) // Flag indicating if there's an error (empty code)
        private set

    // Update the scan code and reset error flag
    fun onScanCodeChange(newCode: String) {
        scanCode = newCode
        isError = false // Reset error when the code changes
    }

    // Submit the scanned code, check for errors (empty code)
    fun onSubmitScanCode() {
        if (scanCode.isEmpty()) {
            isError = true // Set error if code is empty
        } else {
            submitScanCode(scanCode) // Submit the scan code if valid
        }
    }

    // Simulate submitting the scan code (e.g., send to server or process)
    private fun submitScanCode(code: String) {
        println("Code submitted: $code") // Mock submission, print the code
    }
}
