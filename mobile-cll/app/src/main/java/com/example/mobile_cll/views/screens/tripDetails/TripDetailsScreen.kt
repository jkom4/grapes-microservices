package com.example.mobile_cll.views.screens.tripDetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mobile_cll.models.entities.Trip
import com.example.mobile_cll.network.RetrofitClient
import com.example.mobile_cll.viewmodels.tripDetails.TripDetailsViewModel
import com.example.mobile_cll.viewmodels.tripDetails.TripDetailsViewModelFactory
import kotlinx.coroutines.launch
import android.util.Log
import com.example.mobile_cll.views.components.tripDetails.OrderCard
import com.example.mobile_cll.views.components.tripDetails.TripInfoCard
import com.example.mobile_cll.views.components.various.BottomNavigationBar
import retrofit2.HttpException

/**
 * Composable function that displays the details of a trip, including trip information,
 * associated orders, and a confirmation button. It also handles navigation and loading states.
 *
 * @param navController The navigation controller for handling navigation between screens.
 * @param tripId The unique identifier of the trip.
 * @param tripName The name of the trip (e.g., customer name).
 * @param tripDistance The distance of the trip.
 * @param tripAddress The address of the trip destination.
 * @param viewModel The ViewModel providing trip data, defaults to an instance created with TripDetailsViewModelFactory.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    navController: NavHostController,
    tripId: String,
    tripName: String,
    tripDistance: String,
    tripAddress: String,
    viewModel: TripDetailsViewModel = viewModel(factory = TripDetailsViewModelFactory())
) {
    viewModel.loadId(Trip(id = tripId, name = tripName, distance = tripDistance, address = tripAddress, isFinished = false))

    val trip by viewModel.trip
    val orders by viewModel.orders
    val deliveryRequestCount by viewModel.deliveryRequestCount
    val isLoading by viewModel.isLoading
    val coroutineScope = rememberCoroutineScope()
    val apiService = RetrofitClient.getService(RetrofitClient.ApiService::class.java)

    var showAlertDialog by remember { mutableStateOf(false) }
    val areAllOrdersScanned by remember { derivedStateOf { orders.all { it.scanned } } }

    // Reload data after a scan
    LaunchedEffect(navController) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scannedOrderId")?.observeForever { _ ->
            viewModel.loadId(Trip(id = tripId, name = tripName, distance = tripDistance, address = tripAddress, isFinished = false), forceRefresh = true)
        }
    }

    Scaffold(
        topBar = {
            com.example.mobile_cll.views.components.various.TopSection(
                title = "Trip Details",
                navController = navController
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) },
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
                                    Log.d("TripDetailsScreen", "Navigating to scan with orderId: ${order.orderItemId}, tripId: $tripId")
                                    navController.navigate("scan/${order.orderItemId}/$tripId")
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (areAllOrdersScanned) {
                                        coroutineScope.launch {
                                            Log.d("TripDetailsScreen", "Confirm button clicked. Orders: ${orders.map { it.orderItemId }}")
                                            orders.forEach { order ->
                                                val orderId = order.orderItemId.toString()
                                                Log.d("TripDetailsScreen", "Calling PATCH: http://10.0.2.2:8092/cll/deliveries/update-status/$orderId?newStatus=In%20Progress")
                                                try {
                                                    apiService.updateDeliveryStatus(
                                                        orderId = tripId,
                                                        newStatus = "In Progress"
                                                    )
                                                    Log.d("TripDetailsScreen", "Successfully updated status for order ID: $orderId to In Progress")
                                                } catch (e: HttpException) {
                                                    Log.e("TripDetailsScreen", "Failed to update status for order ID: $orderId, HTTP ${e.code()}: ${e.response()?.errorBody()?.string()}", e)
                                                } catch (e: Exception) {
                                                    Log.e("TripDetailsScreen", "Failed to update status for order ID: $orderId, error: ${e.message}", e)
                                                }
                                            }
                                            Log.d("TripDetailsScreen", "Navigating to EmailSentScreen with tripId: $tripId, tripName: $tripName, tripAddress: $tripAddress")
                                            navController.navigate(
                                                "emailsent?tripId=${tripId}&tripName=${tripName}&tripAddress=${tripAddress}"
                                            )
                                        }
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
                        coroutineScope.launch {
                            Log.d("TripDetailsScreen", "Continue Anyway clicked. Orders: ${orders.map { it.orderItemId }}")
                            orders.forEach { order ->
                                val orderId = order.orderItemId.toString()
                                Log.d("TripDetailsScreen", "Calling PATCH: http://10.0.2.2:8092/cll/deliveries/update-status/$orderId?newStatus=In%20Progress")
                                try {
                                    apiService.updateDeliveryStatus(
                                        orderId = orderId,
                                        newStatus = "In Progress" // Essayez "IN_PROGRESS" si l'erreur persiste
                                    )
                                    Log.d("TripDetailsScreen", "Successfully updated status for order ID: $orderId to In Progress")
                                } catch (e: HttpException) {
                                    Log.e("TripDetailsScreen", "Failed to update status for order ID: $orderId, HTTP ${e.code()}: ${e.response()?.errorBody()?.string()}", e)
                                } catch (e: Exception) {
                                    Log.e("TripDetailsScreen", "Failed to update status for order ID: $orderId, error: ${e.message}", e)
                                }
                            }
                            Log.d("TripDetailsScreen", "Navigating to EmailSentScreen with tripId: $tripId, tripName: $tripName, tripAddress: $tripAddress")
                            showAlertDialog = false
                            navController.navigate(
                                "emailsent?tripId=${tripId}&tripName=${tripName}&tripAddress=${tripAddress}"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Continue Anyway", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        )
    }
}