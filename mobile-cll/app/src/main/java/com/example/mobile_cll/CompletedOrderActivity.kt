package com.example.mobile_cll

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mobile_cll.ui.theme.MobileCLLTheme
import com.example.mobile_cll.view.CompletedOrderScreen
import com.example.mobile_cll.view.LastScreen

/**
 * Activity that manages the navigation for the completed order process.
 */
class CompletedOrderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tripId = intent.getStringExtra("tripId") ?: "0"
        Log.d("CompletedOrderActivity", "Received tripId: ${intent.getStringExtra("tripId")}")
        Log.d("CompletedOrderActivity", "Processed tripId: $tripId")

        setContent {
            val navController = rememberNavController()
            MobileCLLTheme {
                NavHost(navController, startDestination = "completedOrder/{tripId}") {
                    composable(
                        route = "completedOrder/{tripId}",
                        arguments = listOf(navArgument("tripId") { type = androidx.navigation.NavType.StringType })
                    ) { backStackEntry ->
                        val tripIdArg = backStackEntry.arguments?.getString("tripId") ?: "0"
                        Log.d("CompletedOrderActivity", "tripId in NavHost: $tripIdArg")
                        CompletedOrderScreen(
                            navController = navController,
                            tripId = tripIdArg
                        )
                    }
                    composable("lastScreen") {
                        LastScreen(navController, this@CompletedOrderActivity)
                    }
                }

                // Explicit navigation to correctly pass tripId
                LaunchedEffect(Unit) {
                    navController.navigate("completedOrder/$tripId") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}
