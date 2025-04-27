package grapes.microservices.views.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import grapes.microservices.Screen
import grapes.microservices.views.ArticleDetails.ArticleDetailScreen
import grapes.microservices.views.AllArticles.AllArticlesScreen
import grapes.microservices.views.CartScreen.CartScreen
import grapes.microservices.views.Home.HomeScreen
import grapes.microservices.views.OrderHistory.OrderHistoryScreen
import grapes.microservices.views.Settings.DeliveryTrackingScreen
import grapes.microservices.views.Settings.SettingsScreen

@Composable
fun MyNavigation() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomBar(
                navController = navController,
                currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.HomeScreen.route,
            modifier = Modifier
                .padding(paddingValues)
        ) {
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
            composable("catalog") {
                AllArticlesScreen(navController = navController)
            }
            composable("profile") {
                SettingsScreen(context = LocalContext.current, navController)
            }
            composable("order_history") {
                OrderHistoryScreen(navController = navController)
            }
            composable("delivery_tracking") {
                DeliveryTrackingScreen(navController = navController)
            }
        }
    }
}
