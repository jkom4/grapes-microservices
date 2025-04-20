package com.example.mobile_cll.views.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobile_cll.network.RetrofitClient
import com.example.mobile_cll.repository.OrderRepository
import com.example.mobile_cll.ScanCodeInput
import com.example.mobile_cll.views.components.TopSection
import kotlinx.coroutines.launch

/**
 * Composable function that provides a UI for scanning an order code. It allows the user to input
 * a scan code and submit it to update the corresponding order via an API call.
 *
 * @param navController The navigation controller for handling navigation, nullable.
 * @param databaseHelper The helper class for database operations.
 * @param orderId The ID of the order to scan, defaults to an empty string.
 * @param tripId The ID of the trip associated with the order, defaults to an empty string.
 */
@Composable
fun ScanView(
    navController: NavController?,
    orderId: String = "",
    tripId: String = ""
) {
    val orderRepository = remember { OrderRepository() }
    var scanCode by remember { mutableStateOf("") } // Initialize empty, not with orderId
    var isError by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = RetrofitClient.getService(RetrofitClient.ApiService::class.java)

    // Log navigation details and check for the order on composition
    LaunchedEffect(Unit) {
        Log.d("ScanView", "Navigated to ScanView with orderId: $orderId and tripId: $tripId")
        val orders = orderRepository.getOrdersForTrip(tripId)
        // Convert orderId to Int for comparison
        val orderIdInt = orderId.toIntOrNull()
        if (orderIdInt != null) {
            val order = orders.find { it.orderItemId == orderIdInt }
            if (order != null) {
                Log.d("ScanView", "Order found: $order")
            } else {
                Log.w("ScanView", "No order found for orderId: $orderId in tripId: $tripId")
            }
        } else {
            Log.e("ScanView", "Invalid orderId: $orderId, cannot convert to Int")
        }
    }

    Scaffold(
        topBar = {
            TopSection(navController = navController ?: rememberNavController(), title = "Scan")
        },
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
                        isError = newCode.isNotEmpty() && newCode != orderId
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SubmitButton(
                    scanCode = scanCode,
                    orderId = orderId,
                    onClick = {
                        if (scanCode == orderId) {
                            coroutineScope.launch {
                                submitScanCode(orderId, apiService)
                                // Signal that the scan was performed
                                navController?.previousBackStackEntry?.savedStateHandle?.set("scannedOrderId", orderId)
                                navController?.popBackStack()
                                keyboardController?.hide()
                            }
                        } else {
                            isError = true
                        }
                    }
                )
            }
        }
    )
}

/**
 * Composable function that displays a submit button for the scan code input.
 * The button is enabled only if the scan code matches the orderId.
 *
 * @param scanCode The current scan code entered by the user.
 * @param orderId The expected order ID to validate against.
 * @param onClick The action to perform when the button is clicked.
 */
@Composable
fun SubmitButton(
    scanCode: String,
    orderId: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        enabled = scanCode == orderId // Enable only if scanCode matches orderId
    ) {
        Text(
            text = "Submit",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * Sends a PATCH request to mark an order as scanned using the provided API service.
 *
 * @param orderId The ID of the order to mark as scanned.
 * @param apiService The Retrofit API service for making the PATCH request.
 */
private suspend fun submitScanCode(orderId: String, apiService: RetrofitClient.ApiService) {
    try {
        apiService.scanOrder(orderId)
        Log.d("ScanView", "Successfully scanned order with ID: $orderId")
    } catch (e: Exception) {
        Log.e("ScanView", "Failed to scan order with ID: $orderId, error: ${e.message}", e)
    }
}

/**
 * Preview function for the ScanView composable. Displays a dummy version of the screen for design-time preview.
 */
@Preview(showBackground = true)
@Composable
fun ScanViewPreview() {
    val dummyContext = null
}