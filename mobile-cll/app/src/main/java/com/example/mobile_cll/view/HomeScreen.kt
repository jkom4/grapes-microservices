package com.example.mobile_cll.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.mobile_cll.model.Order
import com.example.mobile_cll.model.Trip
import com.example.mobile_cll.view.components.BottomNavigationBar
import com.example.mobile_cll.view.components.TopSection
import com.example.mobile_cll.view.components.TripCard

/**
 * Composable displaying the HomeScreen with:
 * - A top section showing the number of trips (define in view > components).
 * - A tab bar to switch between "Current" and "Completed" trips.
 * - A list of trips shown using a LazyColumn with different details.
 * - A bottom navigation bar for navigation between screens (define in view > components).
 *
 * @param navController The navigation controller that allows navigation between screens.
 */
@Composable
fun HomeScreen(navController: NavController) {
    val trips = List(10) { index ->
        Trip(
            id = "$index",
            name = "John Doe $index",
            distance = "${10 + index} mi",
            address = "Rue Brederode 16, 1000 Bruxelles",
        )
    }

    var selectedTabIndex by remember { mutableStateOf(0) }

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

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(trips) { trip ->
                TripCard(trip = trip, orders = getOrdersForTrip(trip.id), navController = navController)
            }
        }

        BottomNavigationBar(navController)
    }
}

/**
 * Function to get a list of orders for a specific trip.
 *
 * @param tripId The ID of the trip for which orders are to be fetched.
 * @return A list of orders for the specified trip.
 */
fun getOrdersForTrip(tripId: String): List<Order> {
    return orders.filter { it.tripId == tripId }
}

val orders = listOf(
    Order(id = "1", tripId = "123", productDescription = "Product A", quantity = 10),
    Order(id = "2", tripId = "123", productDescription = "Product B", quantity = 5),
    Order(id = "3", tripId = "456", productDescription = "Product C", quantity = 8)
)
