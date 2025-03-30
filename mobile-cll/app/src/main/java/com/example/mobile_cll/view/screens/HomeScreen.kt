package com.example.mobile_cll.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobile_cll.view.components.BottomNavigationBar
import com.example.mobile_cll.view.components.TopSection
import com.example.mobile_cll.view.components.TripCard
import com.example.mobile_cll.viewmodel.TripViewModel
import com.example.mobile_cll.viewmodel.TripViewModelFactory

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
fun HomeScreen(
    navController: NavController,
    viewModel: TripViewModel = viewModel(factory = TripViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val trips by viewModel.trips.observeAsState(initial = emptyList())
    val ordersForTrips by viewModel.ordersForTrips.observeAsState(initial = emptyMap())

    // Stores the currently selected tab index as a state variable.
    var selectedTabIndex by remember { mutableStateOf(0) }
    // State for search query
    var searchQuery by remember { mutableStateOf("") }

    val filteredTrips = trips.filter {
        it.address.contains(searchQuery, ignoreCase = true)
    }

    filteredTrips.forEach { trip ->
        if (!ordersForTrips.containsKey(trip.id)) {
            viewModel.getOrdersForTrip(trip.id)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Pass the number of trips to TopSection
        TopSection(tripCount = filteredTrips.size)

        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
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
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                unfocusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        // LazyColumn to display a list of filtered trips dynamically
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredTrips) { trip ->
                val orders = ordersForTrips[trip.id] ?: emptyList()
                TripCard(trip = trip, orders = orders, navController = navController)
            }
        }

        BottomNavigationBar(navController, context)
    }
}