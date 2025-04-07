package grapes.microservices

import androidx.navigation.NavHostController

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Cart : Screen("cart")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Details : Screen("details")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }

    companion object {
        fun refresh(navController: NavHostController) {
            val currentRoute = navController.currentDestination?.route.toString()
            navController.navigate(currentRoute) {
                popUpTo(currentRoute) {
                    inclusive = true
                }
            }
        }
    }
}