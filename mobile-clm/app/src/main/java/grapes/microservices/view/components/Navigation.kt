package grapes.microservices.view.components

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import grapes.microservices.Screen
import grapes.microservices.model.repository.ArticleRepository
import grapes.microservices.view.screens.HomeScreen
import grapes.microservices.viewmodel.HomeViewModel

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
        composable(route = Screen.HomeScreen.route) {
            val homeVM = HomeViewModel(ArticleRepository())
            HomeScreen(navController, homeVM)
        }
//        composable("details") {
//            DetailsScreen()
//        }
    }
}
