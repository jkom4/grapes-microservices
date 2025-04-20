package com.example.mobile_cll.views.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import com.example.mobile_cll.views.screens.EmailSentScreen
import com.example.mobile_cll.views.screens.HomeScreen
import com.example.mobile_cll.views.screens.ScanView
import com.example.mobile_cll.views.screens.TripDetailsScreen


/**
 * Composable function that sets up the navigation graph for the app.
 *
 * @param navController The NavHostController used to manage app navigation.
 * @param databaseHelper The DatabaseHelper instance for database operations.
 */
@Composable
fun MyNavigation(navController: NavHostController) {
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
                orderId = orderId,
                tripId = tripId
            )
        }

        // Fallback scan route without parameters
        composable("scan") {
            ScanView(
                navController = navController,
                orderId = "",
                tripId = ""
            )
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