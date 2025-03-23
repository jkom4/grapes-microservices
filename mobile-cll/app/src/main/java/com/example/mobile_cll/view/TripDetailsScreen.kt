package com.example.mobile_cll.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobile_cll.model.Trip
import com.example.mobile_cll.viewmodel.TripDetailsViewModel
import com.example.mobile_cll.view.components.*

@Composable
fun TripDetailsScreen(
    navController: NavController?,
    tripId: String,
    tripName: String,
    tripDistance: String,
    tripAddress: String,
    viewModel: TripDetailsViewModel = viewModel()
) {
    Log.d("TripDetailsScreen", "Received trip -> id: $tripId, name: $tripName, distance: $tripDistance, address: $tripAddress")

    // Update trip data in the ViewModel
    viewModel.updateTrip(Trip(id = tripId, name = tripName, distance = tripDistance, address = tripAddress))

    // Get the trip and orders data, and loading state
    val trip = viewModel.trip
    val orders = viewModel.orders ?: emptyList()
    val deliveryRequestCount = viewModel.deliveryRequestCount
    val isLoading = viewModel.isLoading
    val context = LocalContext.current

    // State for showing the AlertDialog
    var showAlertDialog by remember { mutableStateOf(false) }

    // Function to check if all orders are scanned
    val areAllOrdersScanned = orders.all { it.isScanned } // Assumes 'isScanned' is a property of Order

    Scaffold(
        topBar = { TopSectionDetails(navController) },
        bottomBar = { BottomNavigationBar(navController, context) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // Show a loading indicator while the data is loading
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    // Show trip details if available
                    trip?.let {
                        TripInfoCard(
                            navController = navController,
                            tripId = it.id,
                            customerName = it.name,
                            address = it.address,
                            orderId = it.id,
                            trip = it
                        )
                    } ?: Text("Trip not found", modifier = Modifier.align(Alignment.CenterHorizontally))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Display the delivery request count
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Delivery Requests: $deliveryRequestCount", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Display list of orders
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(orders.size) { index ->
                            val order = orders[index]
                            OrderCard(order = order, onScanClick = { navController?.navigate("scan") })
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (areAllOrdersScanned) {
                                        // Navigate to EmailSentScreen with trip data as parameters
                                        navController?.navigate(
                                            "emailsent?tripId=${tripId}&tripName=${tripName}&tripAddress=${tripAddress}"
                                        )
                                    } else {
                                        // Show the alert dialog if not all orders are scanned
                                        showAlertDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAD7E)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Confirm",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    )

    // AlertDialog that shows when not all orders are scanned
    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = { Text("Incomplete Orders") },
            text = { Text("Please scan all orders before confirming.") },
            confirmButton = {
                Button(
                    onClick = { showAlertDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAD7E))
                ) {
                    Text("OK", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Close the alert dialog and navigate to the EmailSentScreen
                        showAlertDialog = false
                        navController?.navigate(
                            "emailsent?tripId=${tripId}&tripName=${tripName}&tripAddress=${tripAddress}"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAD7E))
                ) {
                    Text("Continue Anyway", color = Color.White)
                }
            }
        )
    }
}
