package com.example.mobile_cll

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

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
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Gray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF4CAD7E),
                unfocusedBorderColor = Color(0xFF4CAD7E),
                cursorColor = Color(0xFF4CAD7E)
            )
        )

        // Display error message if the input is invalid
        if (isError) {
            Text(
                text = "Please enter a valid scan code",
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
