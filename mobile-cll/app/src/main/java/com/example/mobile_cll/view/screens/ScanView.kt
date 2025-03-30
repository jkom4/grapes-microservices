package com.example.mobile_cll.view.screens

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
import com.example.mobile_cll.model.DatabaseHelper
import com.example.mobile_cll.repository.OrderRepository
import com.example.mobile_cll.ScanCodeInput
import com.example.mobile_cll.view.components.TopSection

/**
 * Composable function that provides a UI for scanning an order code. It allows the user to input
 * a scan code and submit it to update the corresponding order in the database.
 *
 * @param navController The navigation controller for handling navigation, nullable.
 * @param databaseHelper The helper class for database operations.
 * @param orderId The ID of the order to scan, defaults to an empty string.
 * @param tripId The ID of the trip associated with the order, defaults to an empty string.
 */
@Composable
fun ScanView(
    navController: NavController?,
    databaseHelper: DatabaseHelper,
    orderId: String = "",
    tripId: String = ""
) {
    val orderRepository = remember { OrderRepository(databaseHelper) }
    var scanCode by remember { mutableStateOf(orderId) }
    var isError by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Log navigation details and check for the order on composition
    LaunchedEffect(Unit) {
        Log.d("ScanView", "Navigated to ScanView with orderId: $orderId and tripId: $tripId")
        val orders = orderRepository.getOrdersForTrip(tripId)
        val order = orders.find { it.id == orderId }
        if (order != null) {
            Log.d("ScanView", "Found order to update: ${order.id}")
        } else {
            Log.w("ScanView", "No order found for orderId: $orderId in tripId: $tripId")
        }
    }

    Scaffold(
        topBar = {
            TopSection(navController = rememberNavController(), title = "Scan")
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
                        isError = false
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SubmitButton(
                    scanCode = scanCode,
                    onClick = {
                        if (scanCode.isNotEmpty()) {
                            submitScanCode(orderId, tripId, orderRepository)
                            // Signal that the scan was performed
                            navController?.previousBackStackEntry?.savedStateHandle?.set("scannedOrderId", orderId)
                            navController?.popBackStack()
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

/**
 * Composable function that displays a submit button for the scan code input.
 * The button is enabled only if the scan code is not empty.
 *
 * @param scanCode The current scan code entered by the user.
 * @param onClick The action to perform when the button is clicked.
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
        Text(
            text = "Submit",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * Updates the scanned status of an order in the database using the provided order repository.
 *
 * @param orderId The ID of the order to update.
 * @param tripId The ID of the trip associated with the order.
 * @param orderRepository The repository used to interact with the order data.
 */
private fun submitScanCode(orderId: String, tripId: String, orderRepository: OrderRepository) {
    val timestamp = System.currentTimeMillis()
    val orders = orderRepository.getOrdersForTrip(tripId)
    val order = orders.find { it.id == orderId }

    if (order != null) {
        orderRepository.updateScannedAt(order, timestamp)
        Log.d("ScanView", "Updated order with ID: $orderId - scannedAt: $timestamp, isScanned: true")
    } else {
        Log.e("ScanView", "Order with ID: $orderId not found in trip $tripId, no update performed")
    }
}

/**
 * Preview function for the ScanView composable. Displays a dummy version of the screen for design-time preview.
 */
@Preview(showBackground = true)
@Composable
fun ScanViewPreview() {
    val dummyContext = null
    val dummyDatabaseHelper = dummyContext?.let { DatabaseHelper(it) }
    ScanView(navController = null, databaseHelper = dummyDatabaseHelper!!, orderId = "PreviewID", tripId = "PreviewTrip")
}