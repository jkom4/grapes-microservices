package grapes.microservices.views.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import grapes.microservices.Screen
import grapes.microservices.views.ArticleDetails.ArticleDetailScreen
import grapes.microservices.views.CartScreen.CartScreen
import grapes.microservices.views.Home.HomeScreen

@Composable
fun MyNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
        composable(route = Screen.HomeScreen.route) {
            HomeScreen(navController)
        }
        composable(
            route = "article_detail/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.IntType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getInt("articleId") ?: 0
            ArticleDetailScreen(articleId = articleId, navController = navController)
        }
        composable("cart") {
            CartScreen(navController = navController)
        }
    }
}