package com.example.mobile_cll.views.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mobile_cll.views.components.BottomNavigationBar
import com.example.mobile_cll.views.components.TopSection
import com.example.mobile_cll.views.components.TripCard
import com.example.mobile_cll.viewmodels.TripViewModel
import com.example.mobile_cll.viewmodels.TripViewModelFactory

/**
 * Composable displaying the HomeScreen with:
 * - A top section showing the number of trips and their status.
 * - A tab bar to switch between "Current" and "Completed" trips.
 * - A list of trips shown using a LazyColumn.
 * - A bottom navigation bar for navigation between screens.
 *
 * @param navController The navigation controller that allows navigation between screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: TripViewModel = viewModel(factory = TripViewModelFactory(LocalContext.current))
) {
    val trips by viewModel.trips.observeAsState(initial = emptyList())

    // State for the currently selected tab index (0 = Current, 1 = Completed)
    var selectedTabIndex by remember { mutableStateOf(0) }
    // State for the search query
    var searchQuery by remember { mutableStateOf("") }

    // Filter trips based on the tab and search query
    val filteredTrips = trips.filter { trip ->
        val matchesSearch = trip.address.contains(searchQuery, ignoreCase = true)
        val matchesTab = when (selectedTabIndex) {
            0 -> !trip.isFinished // Current: isFinished = false
            1 -> trip.isFinished  // Completed: isFinished = true
            else -> true
        }
        matchesSearch && matchesTab
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Display the number of filtered trips and their status in TopSection
        TopSection(
            tripCount = filteredTrips.size,
            isFinished = selectedTabIndex == 1
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
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

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredTrips) { trip ->
                TripCard(trip = trip, navController = navController)
            }
        }

        BottomNavigationBar(navController = navController)
    }
}