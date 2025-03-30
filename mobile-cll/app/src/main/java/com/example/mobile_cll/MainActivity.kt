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
import com.example.mobile_cll.model.DatabaseHelper
import com.example.mobile_cll.ui.theme.MobileCLLTheme
import com.example.mobile_cll.view.screens.EmailSentScreen
import com.example.mobile_cll.view.screens.HomeScreen
import com.example.mobile_cll.view.screens.ScanView
import com.example.mobile_cll.view.screens.TripDetailsScreen

/**
 * Main entry point of the application, responsible for setting up navigation and the UI theme.
 * It initializes the DatabaseHelper and defines the navigation graph for the app.
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is first created. Sets up the database helper, UI content,
     * and navigation structure using Jetpack Compose and Navigation.
     *
     * @param savedInstanceState If non-null, this activity is being re-initialized with previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val databaseHelper = DatabaseHelper(this)

        setContent {
            MobileCLLTheme {
                val navController = rememberNavController()

                // Define the navigation host with all possible routes
                NavHost(navController = navController, startDestination = "home") {
                    // Home screen route
                    composable("home") { HomeScreen(navController) }

                    // Trip details route with trip parameters
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
                        TripDetailsScreen(
                            navController = navController,
                            tripId = tripId,
                            tripName = tripName,
                            tripDistance = tripDistance,
                            tripAddress = tripAddress
                        )
                    }

                    // Scan route with orderId and tripId parameters
                    composable(
                        route = "scan/{orderId}/{tripId}",
                        arguments = listOf(
                            navArgument("orderId") { type = NavType.StringType },
                            navArgument("tripId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                        val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                        ScanView(
                            navController = navController,
                            databaseHelper = databaseHelper,
                            orderId = orderId,
                            tripId = tripId
                        )
                    }

                    // Fallback scan route without parameters (optional)
                    composable("scan") {
                        ScanView(navController = navController, databaseHelper = databaseHelper)
                    }

                    // Email sent confirmation route with trip parameters
                    composable(
                        "emailsent?tripId={tripId}&tripName={tripName}&tripAddress={tripAddress}",
                        arguments = listOf(
                            navArgument("tripId") { type = NavType.StringType },
                            navArgument("tripName") { type = NavType.StringType },
                            navArgument("tripAddress") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
                        val tripName = backStackEntry.arguments?.getString("tripName") ?: ""
                        val tripAddress = backStackEntry.arguments?.getString("tripAddress") ?: ""
                        Log.d("NavHost", "Trip ID: $tripId, Trip Name: $tripName, Trip Address: $tripAddress")
                        EmailSentScreen(navController, tripId, tripName, tripAddress)
                    }
                }
            }
        }
    }
}