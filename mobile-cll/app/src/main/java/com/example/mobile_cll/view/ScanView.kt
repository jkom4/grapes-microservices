package com.example.mobile_cll.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.mobile_cll.ScanCodeInput
import com.example.mobile_cll.view.components.TopSectionScan

@Composable
fun ScanView(navController: NavController?) {
    var scanCode by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = { TopSectionScan(navController) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Scan Code Input Field
                ScanCodeInput(
                    scanCode = scanCode,
                    isError = isError,
                    onScanCodeChange = { newCode ->
                        scanCode = newCode
                        isError = false
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                SubmitButton(
                    scanCode = scanCode,
                    onClick = {
                        // Check if scanCode is not empty and submit the scan code
                        if (scanCode.isNotEmpty()) {
                            submitScanCode(scanCode) // Submit the code
                            navController?.popBackStack() // Navigate back after submission
                            keyboardController?.hide() // Hide the keyboard after submission
                        } else {
                            isError = true // Show error if the scanCode is empty
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun SubmitButton(
    scanCode: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAD7E)),
        enabled = scanCode.isNotEmpty() // Enable button only if scanCode is not empty
    ) {
        Text("Submit", fontSize = 16.sp, color = Color.White)
    }
}

// Function to handle scan code submission
fun submitScanCode(code: String) {
    println("Code submitted: $code")
}

@Preview
@Composable
fun ScanViewPreview() {
    ScanView(navController = null)
}
