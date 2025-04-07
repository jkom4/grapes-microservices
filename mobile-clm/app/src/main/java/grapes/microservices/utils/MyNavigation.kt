package grapes.microservices.utils

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import grapes.microservices.Screen
import grapes.microservices.views.screens.home.HomeScreen

@Composable
fun MyNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(route = Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(route = Screen.Cart.route) {

        }
        composable(route = Screen.Profile.route) {

        }
        composable(route = Screen.Settings.route) {

        }
        composable(route = Screen.Details.route) {

        }
    }
}
