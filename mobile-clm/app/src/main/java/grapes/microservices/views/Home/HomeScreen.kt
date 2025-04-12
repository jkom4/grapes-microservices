package grapes.microservices.views.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.ui.theme.MobileCLMTheme
import grapes.microservices.viewmodels.HomeViewModel
import grapes.microservices.viewmodels.HomeViewModelFactory
import grapes.microservices.views.components.MyArticleCardList
import grapes.microservices.views.components.MyTopBar

@Composable
fun HomeScreen(navController: NavHostController) {
    val space = 16.dp
    // Crée manuellement les dépendances
    val repository = ArticleRepository(RetrofitClient.articleApiService)
    val vm: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(repository)
    )

    Scaffold(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(space)
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            MyTopBar(
                modifier = Modifier.padding(bottom = space),
                navController = navController
            )
            MyFilterSearchLayer(modifier = Modifier.padding(bottom = space))

            val query = vm.filterSettings.collectAsState().value.query
            val articles = vm.articles.collectAsState().value

            if (query.isEmpty()) {
                MyArticleCardList(
                    title = "Popular",
                    articles = articles.take(3),
                    orientation = Orientation.Horizontal,
                    navController = navController, // Pass navController
                    modifier = Modifier.padding(bottom = space)
                )

                MyArticleCardList(
                    title = "For You",
                    articles = articles.take(6),
                    orientation = Orientation.Horizontal,
                    navController = navController, // Pass navController
                    modifier = Modifier.padding(bottom = space)
                )
            } else {
                MyArticleCardList(
                    title = "Results",
                    articles = articles.filter {
                        it.name.contains(query, ignoreCase = true)
                    },
                    navController = navController, // Pass navController
                    modifier = Modifier.padding(bottom = space)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MobileCLMTheme(false) {
        HomeScreen(rememberNavController())
    }
}