package com.example.mobile_cll.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobile_cll.model.Order
import com.example.mobile_cll.model.Trip
import com.example.mobile_cll.view.components.BottomNavigationBar
import com.example.mobile_cll.view.components.TopSection
import com.example.mobile_cll.view.components.TripCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    // Creating a list of trips dynamically for display (10 trips for demo purposes)
    val trips = List(10) { index ->
        Trip(
            id = "$index",  // Dynamic trip ID
            name = "John Doe $index",  // Dynamic name
            distance = "${10 + index} mi",  // Dynamic distance
            address = "Rue Brederode 16, 1000 Bruxelles",  // Static address for demo
        )
    }

    // State for search query
    var searchQuery by remember { mutableStateOf("") }

    // Filter trips based on search query (filter by address)
    val filteredTrips = trips.filter {
        it.address.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top section of the screen (e.g., header or navigation bar)
        TopSection()

        // TabRow for selecting between Current and Completed trips
        TabRow(
            selectedTabIndex = 0,  // Default selected tab
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color(0xFF4CAD7E),  // Tab background color
            contentColor = Color.White  // Tab text color
        ) {
            // Tab for 'Current' trips
            Tab(
                text = {
                    Text(
                        "Current",  // Tab text
                        style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                },
                selected = true,  // 'Current' tab is selected by default
                onClick = {}  // On click action, currently does nothing
            )
            // Tab for 'Completed' trips
            Tab(
                text = {
                    Text(
                        "Completed",  // Tab text
                        style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                },
                selected = false,  // 'Completed' tab is not selected by default
                onClick = {}  // On click action, currently does nothing
            )
        }

        // Search bar for filtering trips by address
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by City") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF4CAD7E),
                unfocusedBorderColor = Color(0xFF4CAD7E),
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color(0xFF4CAD7E)
            )
        )

        // LazyColumn to display a list of filtered trips dynamically
        LazyColumn(modifier = Modifier.weight(1f)) {
            // Dynamically creating a list of TripCard items
            items(filteredTrips) { trip ->
                // For each trip, create a TripCard with associated orders
                TripCard(trip = trip, orders = getOrdersForTrip(trip.id), navController = navController)
            }
        }

        // Bottom navigation bar at the bottom of the screen
        BottomNavigationBar(navController, context)
    }
}

// Function to get orders for a specific trip ID
fun getOrdersForTrip(tripId: String): List<Order> {
    return orders.filter { it.tripId == tripId }  // Filter orders by tripId
}

// Sample orders list
val orders = listOf(
    Order(id = "1", tripId = "123", productDescription = "Product A", quantity = 10, scannedAt = null),
    Order(id = "2", tripId = "123", productDescription = "Product B", quantity = 5, scannedAt = "2025-03-23T10:00:00Z"),
    Order(id = "3", tripId = "456", productDescription = "Product C", quantity = 8, scannedAt = null)
)
