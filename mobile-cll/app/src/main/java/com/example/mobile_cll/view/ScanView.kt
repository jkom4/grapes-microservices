package com.example.mobile_cll.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.mobile_cll.ScanCodeInput
import com.example.mobile_cll.view.components.TopSectionScan

/**
 * This composable displays a scan input screen with a text field to input the scan code.
 * It also includes a submit button that triggers an action when clicked.
 */
@Composable
fun ScanView(navController: NavController?) {
    // State to hold the current scan code and error flag.
    var scanCode by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Local keyboard controller to hide the keyboard when necessary.
    val keyboardController = LocalSoftwareKeyboardController.current

    // Scaffold provides the basic structure of the screen (with top bar, content, etc.).
    Scaffold(
        topBar = { TopSectionScan(navController) }, // Top section of the screen, passing navController.
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues) // Ensures that the content respects the safe area
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Input for the scan code, with an error flag to show validation message.
                ScanCodeInput(
                    scanCode = scanCode,
                    isError = isError,
                    onScanCodeChange = { newCode ->
                        // Updates scan code and resets error when the code changes
                        scanCode = newCode
                        isError = false
                    }
                )

                Spacer(modifier = Modifier.height(16.dp)) // Adds space between the input and button

                // The Submit Button, only enabled when the scan code is not empty.
                SubmitButton(
                    scanCode = scanCode,
                    onClick = {
                        // Validates if the scan code is not empty before submitting.
                        if (scanCode.isNotEmpty()) {
                            submitScanCode(scanCode)
                            navController?.popBackStack() // Navigate back after submitting
                            keyboardController?.hide() // Hide the keyboard
                        } else {
                            // If scan code is empty, show an error
                            isError = true
                        }
                    }
                )
            }
        }
    )
}

/**
 * Submit button composable that is enabled only when scan code is not empty.
 */
@Composable
fun SubmitButton(
    scanCode: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        enabled = scanCode.isNotEmpty()
    ) {
        // Text displayed inside the button
        Text("Submit", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
    }
}

// Function to simulate the submission of the scan code (e.g., printing it to the console).
fun submitScanCode(code: String) {
    println("Code submitted: $code")
}

/**
 * Preview function for ScanView composable to see a preview of this screen.
 */
@Preview
@Composable
fun ScanViewPreview() {
    ScanView(navController = null) // Preview the screen with no navigation controller
}
