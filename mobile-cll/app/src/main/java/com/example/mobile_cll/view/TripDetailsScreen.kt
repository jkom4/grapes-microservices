package com.example.mobile_cll.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mobile_cll.viewmodel.TripDetailsViewModel
import com.example.mobile_cll.view.components.*

/**
 * Composable displaying the Trip Details screen with:
 * - Trip information card (define in view > components).
 * - A list of orders associated with the trip (define in view > components).
 * - A button to confirm the trip (when all scans are done).
 *
 * @param navController The navigation controller that allows navigation between screens.
 * @param viewModel The view model that holds the trip details and logic for the screen.
 */
@Composable
fun TripDetailsScreen(navController: NavController?, viewModel: TripDetailsViewModel = viewModel()) {
    // Retrieve the trip ID from the navigation arguments
    val tripId = navController?.currentBackStackEntry?.arguments?.getString("id") ?: ""
    viewModel.loadId(tripId)

    val trip = viewModel.trip
    val orders = viewModel.orders
    val deliveryRequestCount = viewModel.deliveryRequestCount

    Scaffold(
        topBar = { TopSectionDetails(navController) },
        bottomBar = { BottomNavigationBar(navController) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
            ) {
                // Show a loading indicator while the data is loading
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    // Display trip info if available
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

                    // Display orders and delivery request count
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Delivery Requests: $deliveryRequestCount", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(16.dp))

                        // List of orders for the trip
                        orders.forEach { order ->
                            OrderCard(order = order, onScanClick = { viewModel.onScanClick(order.id) })
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Button to confirm the trip
                        Button(
                            onClick = { /* Action for confirming the trip */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAD7E)), // Green color
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
    )
}
