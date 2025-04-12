package grapes.microservices.views.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import grapes.microservices.views.components.PromoBox

@Composable
fun HomeScreen(navController: NavHostController) {
    val space = 16.dp

    val repository = ArticleRepository(RetrofitClient.articleApiService)
    val vm: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))

    // Récupère les articles et le query
    val query = vm.filterSettings.collectAsState().value.query
    val articles = vm.articles.collectAsState().value

    Scaffold(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(space)
    ) { paddingValues ->
        // Remplacer la Column par LazyColumn pour un défilement performant
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize() // Garantir que la taille de la LazyColumn occupe tout l'espace disponible
        ) {
            item {
                MyTopBar(
                    modifier = Modifier.padding(bottom = space),
                    navController = navController
                )
            }

            item {
                MyFilterSearchLayer(modifier = Modifier.padding(bottom = space))
            }

            item {
                PromoBox()
            }

            if (query.isEmpty()) {
                item {
                    MyArticleCardList(
                        title = "Popular",
                        articles = articles.take(3),
                        orientation = Orientation.Horizontal,
                        navController = navController,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(bottom = 0.dp)
                    )
                }

                item {
                    MyArticleCardList(
                        title = "For You",
                        articles = articles.take(6),
                        orientation = Orientation.Vertical,
                        navController = navController,
                        modifier = Modifier.padding(bottom = space)
                    )
                }
            } else {
                item {
                    MyArticleCardList(
                        title = "Results",
                        articles = articles.filter {
                            it.name.contains(query, ignoreCase = true)
                        },
                        orientation = Orientation.Vertical,
                        navController = navController,
                        modifier = Modifier.padding(bottom = space)
                    )
                }
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
