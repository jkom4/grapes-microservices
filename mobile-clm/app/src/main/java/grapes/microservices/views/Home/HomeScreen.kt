package grapes.microservices.views.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import grapes.microservices.R
import grapes.microservices.models.data.Article
import grapes.microservices.views.components.MySearchBar
import grapes.microservices.views.components.PromoBox

@Composable
fun HomeScreen(navController: NavHostController) {
    val space = 16.dp
    val repository = ArticleRepository(RetrofitClient.articleApiService)
    val vm: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))

    val articles = vm.articles.collectAsState().value

    // State variables for search functionality
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Article>?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(space)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
                )
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Top bar with user icon and nav
            item {
                MyTopBar(
                    modifier = Modifier.padding(bottom = space),
                    navController = navController
                )
            }

            // Search bar and filter layer
            item {
                // Search bar
                MySearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = space),
                    query = searchQuery,
                    onQueryChanged = { newQuery ->
                        searchQuery = newQuery
                    },
                    onSearchStarted = {
                        isSearching = true
                        searchResults = null // Optional: reset results during search
                    },
                    onResults = { results ->
                        searchResults = results
                        isSearching = false
                    }
                )
            }

            // Promo banner
            item {
                PromoBox()
            }

            // Show UI based on search state
            when {
                isSearching -> {
                    item {
                        Text("Searching...", style = MaterialTheme.typography.titleMedium)
                    }
                }

                !searchResults.isNullOrEmpty() -> {
                    item {
                        MyArticleCardList(
                            title = "Results",
                            articles = searchResults!!,
                            orientation = Orientation.Vertical,
                            navController = navController,
                            modifier = Modifier.padding(bottom = space)
                        )
                    }
                }

                searchQuery.isNotEmpty() -> {
                    item {
                        Text("No results found.", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                else -> {
                    // Default content: popular & recommended
                    item {
                        MyArticleCardList(
                            title = stringResource(R.string.home_popular),
                            articles = articles.take(3),
                            orientation = Orientation.Horizontal,
                            navController = navController,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    item {
                        MyArticleCardList(
                            title = stringResource(R.string.home_for_you),
                            articles = articles.take(6),
                            orientation = Orientation.Vertical,
                            navController = navController,
                            modifier = Modifier.padding(bottom = space)
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Preview function to display HomeScreen in the UI
    MobileCLMTheme(false) {
        HomeScreen(rememberNavController())
    }
}
