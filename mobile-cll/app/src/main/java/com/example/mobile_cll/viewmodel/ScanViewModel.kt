package com.example.mobile_cll.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class ScanViewModel : ViewModel() {
    var scanCode by mutableStateOf("")
        private set

    var isError by mutableStateOf(false)
        private set

    fun onScanCodeChange(newCode: String) {
        scanCode = newCode
        isError = false
    }

    fun onSubmitScanCode() {
        if (scanCode.isEmpty()) {
            isError = true
        } else {
            submitScanCode(scanCode)
        }
    }

    private fun submitScanCode(code: String) {
        println("Code submitted: $code")
    }
}