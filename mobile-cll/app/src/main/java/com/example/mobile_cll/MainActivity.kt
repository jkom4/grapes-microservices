package com.example.mobile_cll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobile_cll.view.HomeScreen
import com.example.mobile_cll.view.TripDetailsScreen

/**
 * MainActivity is the entry point of the application.
 * It sets up the navigation for the app and defines the start destination.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Initialize the NavController for navigation between screens
            val navController = rememberNavController()

            // Set up the NavHost for managing navigation
            NavHost(navController = navController, startDestination = "home") {
                // Home screen route
                composable("home") { HomeScreen(navController) }

                // Trip details screen route with dynamic trip ID
                composable("trip_details/{id}") { backStackEntry ->
                    // Pass the navController to TripDetailsScreen
                    TripDetailsScreen(navController)
                }
            }
        }
    }
}
