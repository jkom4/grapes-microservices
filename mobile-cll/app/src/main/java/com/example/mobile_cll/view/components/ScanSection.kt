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
import androidx.compose.ui.graphics.Color

@Composable
fun ScanCodeInput(
    scanCode: String,
    isError: Boolean,
    onScanCodeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = scanCode,
            onValueChange = onScanCodeChange,
            label = { Text("Enter Scan Code") },
            singleLine = true,
            isError = isError,
            keyboardActions = KeyboardActions(
                onDone = { }
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
