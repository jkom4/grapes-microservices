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
                ScanCodeInput(
                    scanCode = scanCode,
                    isError = isError,
                    onScanCodeChange = { newCode ->
                        scanCode = newCode
                        isError = false
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SubmitButton(
                    scanCode = scanCode,
                    onClick = {
                        if (scanCode.isNotEmpty()) {
                            submitScanCode(scanCode)
                            navController?.navigate("trip_details/{id}")
                            keyboardController?.hide()
                        } else {
                            isError = true
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
        enabled = scanCode.isNotEmpty()
    ) {
        Text("Submit", fontSize = 16.sp, color = Color.White)
    }
}


fun submitScanCode(code: String) {
    println("Code submitted: $code")
}

@Preview
@Composable
fun ScanViewPreview() {
    ScanView(navController = null)
}