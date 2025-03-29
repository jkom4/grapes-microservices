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
import com.example.mobile_cll.ui.theme.MobileCLLTheme
import com.example.mobile_cll.view.EmailSentScreen
import com.example.mobile_cll.view.HomeScreen
import com.example.mobile_cll.view.ScanView
import com.example.mobile_cll.view.TripDetailsScreen

/**
 * MainActivity is the entry point of the application.
 * It sets up the navigation for the app and defines the start destination.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileCLLTheme {
                val navController = rememberNavController()
                // Initialize the NavController for navigation between screens

                // Set up the NavHost for managing navigation
                NavHost(navController = navController, startDestination = "home") {
                    // Home screen route
                    composable("home") { HomeScreen(navController) }
                    // Trip details screen route with dynamic trip ID
                    composable(
                        route = "trip_details/{id}/{name}/{distance}/{address}",
                        arguments = listOf(
                            navArgument("id") { type = NavType.StringType },
                            navArgument("name") { type = NavType.StringType },
                            navArgument("distance") { type = NavType.StringType },
                            navArgument("address") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val tripId = backStackEntry.arguments?.getString("id") ?: ""
                        val tripName = backStackEntry.arguments?.getString("name") ?: ""
                        val tripDistance = backStackEntry.arguments?.getString("distance") ?: ""
                        val tripAddress = backStackEntry.arguments?.getString("address") ?: ""

                        // Pass the navController to TripDetailsScreen
                        TripDetailsScreen(
                            navController = navController,
                            tripId = tripId,
                            tripName = tripName,
                            tripDistance = tripDistance,
                            tripAddress = tripAddress
                        )
                    }
                    // Scan screen route
                    composable("scan") { ScanView(navController) }
                    // EmailSent screen route
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

                        Log.d(
                            "NavHost",
                            "Trip ID: $tripId, Trip Name: $tripName, Trip Address: $tripAddress"
                        )

                        // Pass the data to EmailSentScreen
                        EmailSentScreen(navController, tripId, tripName, tripAddress)
                    }
                }
            }
        }
    }
}