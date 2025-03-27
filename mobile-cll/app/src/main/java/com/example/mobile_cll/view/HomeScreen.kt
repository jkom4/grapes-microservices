package com.example.mobile_cll.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.mobile_cll.model.Order
import com.example.mobile_cll.model.Trip
import com.example.mobile_cll.view.components.BottomNavigationBar
import com.example.mobile_cll.view.components.TopSection
import com.example.mobile_cll.view.components.TripCard
import androidx.compose.runtime.setValue

// Initialization of orders
val orders = listOf(
    Order(id = "1", tripId = "123", productDescription = "Product A", quantity = 10, scannedAt = null),
    Order(id = "2", tripId = "123", productDescription = "Product B", quantity = 5, scannedAt = "2025-03-23T10:00:00Z"),
    Order(id = "3", tripId = "456", productDescription = "Product C", quantity = 8, scannedAt = null)
)

// Group orders by TripId
val ordersGroupedByTrip = orders.groupBy { it.tripId }

/**
 * Composable displaying the HomeScreen with:
 * - A top section showing the number of trips.
 * - A tab bar to switch between "Current" and "Completed" trips.
 * - A list of trips shown using a LazyColumn with different details.
 * - A bottom navigation bar for navigation between screens.
 *
 * @param navController The navigation controller that allows navigation between screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    val trips = List(10) { index ->
        Trip(
            id = "$index",
            name = "John Doe $index",
            distance = "${10 + index} mi",
            address = "Rue Brederode 16, 1000 Bruxelles",
        )
    }

    // Stores the currently selected tab index as a state variable.
    var selectedTabIndex by remember { mutableStateOf(0) }
    // State for search query
    var searchQuery by remember { mutableStateOf("") }

    val filteredTrips = trips.filter {
        it.address.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Pass the number of trips to TopSection
        TopSection(tripCount = trips.size)

        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier
                .fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Tab(
                text = {
                    Text(
                        "Current",
                        style = TextStyle(color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    )
                },
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 }
            )
            Tab(
                text = {
                    Text(
                        "Completed",
                        style = TextStyle(color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    )
                },
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 }
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
            items(trips) { trip ->
                TripCard(trip = trip, orders = getOrdersForTrip(trip.id), navController = navController)
            }
        }

        BottomNavigationBar(navController, context)
    }
}

/**
 * Function to get a list of orders for a specific trip.
 *
 * @param tripId The ID of the trip for which orders are to be fetched.
 * @return A list of orders for the specified trip.
 */
fun getOrdersForTrip(tripId: String): List<Order> {
    return ordersGroupedByTrip[tripId] ?: emptyList()
}
