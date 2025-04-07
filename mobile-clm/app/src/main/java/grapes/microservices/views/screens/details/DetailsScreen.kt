package grapes.microservices.views.screens.details

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import grapes.microservices.viewmodels.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DetailsScreen(navController: NavHostController) {
    val vm = koinViewModel<HomeViewModel>()


}