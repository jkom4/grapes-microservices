package com.example.mobile_cll

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mobile_cll.view.EmailSentScreen
import com.example.mobile_cll.view.HomeScreen
import com.example.mobile_cll.view.ScanView
import com.example.mobile_cll.view.TripDetailsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Initialize navigation controller
            val navController = rememberNavController()

            // Set up NavHost with the starting screen and routes
            NavHost(navController = navController, startDestination = "home") {

                // Define "home" route and associated screen
                composable("home") { HomeScreen(navController) }

                // Define "trip_details" route with parameters
                composable(
                    route = "trip_details/{id}/{name}/{distance}/{address}",
                    arguments = listOf(
                        navArgument("id") { type = NavType.StringType },
                        navArgument("name") { type = NavType.StringType },
                        navArgument("distance") { type = NavType.StringType },
                        navArgument("address") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    // Retrieve arguments passed to the route
                    val tripId = backStackEntry.arguments?.getString("id") ?: ""
                    val tripName = backStackEntry.arguments?.getString("name") ?: ""
                    val tripDistance = backStackEntry.arguments?.getString("distance") ?: ""
                    val tripAddress = backStackEntry.arguments?.getString("address") ?: ""

                    Log.d("NavHost", "TripDetails args -> id: $tripId, name: $tripName, distance: $tripDistance, address: $tripAddress")

                    // Pass arguments to TripDetailsScreen
                    TripDetailsScreen(
                        navController = navController,
                        tripId = tripId,
                        tripName = tripName,
                        tripDistance = tripDistance,
                        tripAddress = tripAddress
                    )
                }

                // Define "scan" route and associated screen
                composable("scan") { ScanView(navController) }

                // Define "emailsent" route with optional parameters
                composable(
                    "emailsent?tripId={tripId}&tripName={tripName}&tripAddress={tripAddress}",
                    arguments = listOf(
                        navArgument("tripId") { type = NavType.StringType },
                        navArgument("tripName") { type = NavType.StringType },
                        navArgument("tripAddress") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    // Retrieve optional parameters passed to the route
                    val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                    val tripName = backStackEntry.arguments?.getString("tripName") ?: ""
                    val tripAddress = backStackEntry.arguments?.getString("tripAddress") ?: ""

                    Log.d("NavHost", "Trip ID: $tripId, Trip Name: $tripName, Trip Address: $tripAddress")

                    // Pass the data to EmailSentScreen
                    EmailSentScreen(navController, tripId, tripName, tripAddress)
                }
            }
        }
    }
}
