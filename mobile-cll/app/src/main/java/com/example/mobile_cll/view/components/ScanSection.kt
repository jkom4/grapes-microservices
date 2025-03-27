package com.example.mobile_cll

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
/**
 * This composable creates an input field for scanning the code.
 * It displays an error message when the input is invalid.
 */
@Composable
fun ScanCodeInput(
    scanCode: String,
    isError: Boolean,
    onScanCodeChange: (String) -> Unit
) {
    // The Column contains the OutlinedTextField and optional error message
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // The OutlinedTextField allows the user to input the scan code
        OutlinedTextField(
            value = scanCode,
            onValueChange = onScanCodeChange, // Update the value when changed
            label = { Text("Enter Scan Code") },
            singleLine = true,
            isError = isError, // Indicate error state if necessary
            keyboardActions = KeyboardActions(
                onDone = {}
            ),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                unfocusedTextColor = MaterialTheme.colorScheme.tertiary,
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        // Display error message if the input is invalid
        if (isError) {
            Text(
                text = "Please enter a valid scan code", // Error message
                color = MaterialTheme.colorScheme.error, // Red text color to indicate an error
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp) // Add spacing above the error message
            )
        }
    }
}

