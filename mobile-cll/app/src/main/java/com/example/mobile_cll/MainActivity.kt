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
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "home") {
                composable("home") { HomeScreen(navController) }

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

                    Log.d("NavHost", "TripDetails args -> id: $tripId, name: $tripName, distance: $tripDistance, address: $tripAddress")

                    TripDetailsScreen(
                        navController = navController,
                        tripId = tripId,
                        tripName = tripName,
                        tripDistance = tripDistance,
                        tripAddress = tripAddress
                    )
                }

                composable("scan") { ScanView(navController) }
                composable("emailsent") { EmailSentScreen(navController) }
            }
        }
    }
}

