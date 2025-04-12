package grapes.microservices.views.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import grapes.microservices.Screen
import grapes.microservices.views.ArticleDetails.ArticleDetailScreen
import grapes.microservices.views.CartScreen.CartScreen
import grapes.microservices.views.Home.AllArticlesScreen
import grapes.microservices.views.Home.HomeScreen

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
    ) { paddingValues -> // Utilisation de paddingValues
        NavHost(
            navController = navController,
            startDestination = Screen.HomeScreen.route,
            modifier = Modifier
                .padding(paddingValues)  // Assurer que le padding ne génère pas un espace supplémentaire
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
                Text("Écran de profil", modifier = Modifier.fillMaxSize())
            }
        }
    }
}
