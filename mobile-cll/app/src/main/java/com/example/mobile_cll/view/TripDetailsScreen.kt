package com.example.mobile_cll.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobile_cll.model.Trip
import com.example.mobile_cll.view.components.*
import com.example.mobile_cll.viewmodel.TripDetailsViewModel
import com.example.mobile_cll.viewmodel.TripDetailsViewModelFactory

/**
 * Composable function that displays the details of a trip, including trip information,
 * associated orders, and a confirmation button. It also handles navigation and loading states.
 *
 * @param navController The navigation controller for handling navigation between screens, nullable.
 * @param tripId The unique identifier of the trip.
 * @param tripName The name of the trip (e.g., customer name).
 * @param tripDistance The distance of the trip.
 * @param tripAddress The address of the trip destination.
 * @param viewModel The ViewModel providing trip data, defaults to an instance created with TripDetailsViewModelFactory.
 */
@Composable
fun TripDetailsScreen(
    navController: NavController?,
    tripId: String,
    tripName: String,
    tripDistance: String,
    tripAddress: String,
    viewModel: TripDetailsViewModel = viewModel(factory = TripDetailsViewModelFactory(LocalContext.current))
) {
    viewModel.loadId(Trip(id = tripId, name = tripName, distance = tripDistance, address = tripAddress))

    val trip by viewModel.trip
    val orders by viewModel.orders // Must be a State or MutableStateFlow
    val deliveryRequestCount by viewModel.deliveryRequestCount
    val isLoading by viewModel.isLoading
    val context = LocalContext.current

    var showAlertDialog by remember { mutableStateOf(false) }
    val areAllOrdersScanned by remember { derivedStateOf { orders.all { it.isScanned } } }

    // Reload data after a scan
    LaunchedEffect(navController) {
        navController?.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scannedOrderId")?.observeForever { _ ->
            viewModel.loadId(Trip(id = tripId, name = tripName, distance = tripDistance, address = tripAddress))
        }
    }

    Scaffold(
        topBar = { TopSectionDetails(navController) },
        bottomBar = { BottomNavigationBar(navController, context) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
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

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Delivery Requests: $deliveryRequestCount", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(orders.size) { index ->
                            val order = orders[index]
                            OrderCard(
                                order = order,
                                onScanClick = {
                                    navController?.navigate("scan/${order.id}/$tripId")
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (areAllOrdersScanned) {
                                        navController?.navigate(
                                            "emailsent?tripId=${tripId}&tripName=${tripName}&tripAddress=${tripAddress}"
                                        )
                                    } else {
                                        showAlertDialog = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Confirm",
                                    color = MaterialTheme.colorScheme.onPrimary,
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

    // Alert dialog shown when not all orders are scanned
    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = { Text("Incomplete Orders") },
            text = { Text("Please scan all orders before confirming.") },
            confirmButton = {
                Button(
                    onClick = { showAlertDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showAlertDialog = false
                        navController?.navigate(
                            "emailsent?tripId=${tripId}&tripName=${tripName}&tripAddress=${tripAddress}"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Continue Anyway", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        )
    }
}