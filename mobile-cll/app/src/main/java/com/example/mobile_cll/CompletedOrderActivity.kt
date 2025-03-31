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

class CompletedOrderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tripId = intent.getStringExtra("tripId") ?: "0"
        Log.d("CompletedOrderActivity", "tripId reçu brut: ${intent.getStringExtra("tripId")}")
        Log.d("CompletedOrderActivity", "tripId après gestion: $tripId")

        setContent {
            val navController = rememberNavController()
            MobileCLLTheme {
                NavHost(navController, startDestination = "completedOrder/{tripId}") {
                    composable(
                        route = "completedOrder/{tripId}",
                        arguments = listOf(navArgument("tripId") { type = androidx.navigation.NavType.StringType })
                    ) { backStackEntry ->
                        val tripIdArg = backStackEntry.arguments?.getString("tripId") ?: "0"
                        Log.d("CompletedOrderActivity", "tripId dans NavHost: $tripIdArg")
                        CompletedOrderScreen(
                            navController = navController,
                            tripId = tripIdArg
                        )
                    }
                    composable("lastScreen") {
                        LastScreen(navController, this@CompletedOrderActivity)
                    }
                }
                // Navigation explicite pour passer tripId correctement
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