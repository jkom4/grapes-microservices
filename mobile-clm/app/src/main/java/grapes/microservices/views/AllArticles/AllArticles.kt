package grapes.microservices.views.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.viewmodel.ArticlePaginationState
import grapes.microservices.viewmodel.ArticleViewModel
import grapes.microservices.viewmodel.ArticleViewModelFactory
import grapes.microservices.views.components.MyArticleCard
import grapes.microservices.views.components.MySearchBar

@Composable
fun AllArticlesScreen(navController: NavHostController) {
    val repository = ArticleRepository(RetrofitClient.articleApiService) // Replace with your repository
    val apiService = RetrofitClient.articleApiService // Your API service

    val viewModelFactory = ArticleViewModelFactory(repository, apiService)
    val viewModel: ArticleViewModel = viewModel(factory = viewModelFactory)

    val space = 16.dp
    val state = viewModel.articlePaginationState.collectAsState()

    // Declare a state to handle search query value
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(space)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
                )
                .fillMaxSize()
        ) {
            // Add the title "All products"
            Text(
                text = "All products",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold // Make the text bold
                ),
                modifier = Modifier.padding(bottom = space) // Add margin at the bottom of the title
            )

            // Search bar for entering query
            MySearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp), // Add margin below the search bar
                query = searchQuery, // The search query value
                onValueChange = { newQuery ->
                    searchQuery = newQuery // Update the search query
                }
            )

            when (val result = state.value) {
                // Show loading state
                is ArticlePaginationState.Loading -> {
                    Text("Loading articles...", style = MaterialTheme.typography.titleMedium)
                }
                // Show articles if the loading is successful
                is ArticlePaginationState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(bottom = space)
                    ) {
                        items(result.articles.chunked(2)) { articlePair ->
                            // Create a row with two articles
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = space),
                                horizontalArrangement = Arrangement.spacedBy(space)
                            ) {
                                // Display the first article in the pair
                                MyArticleCard(
                                    article = articlePair[0],
                                    navController = navController,
                                    modifier = Modifier
                                        .weight(1f) // Take half of the row's space
                                )

                                // If the pair contains a second article, display it as well
                                articlePair.getOrNull(1)?.let { secondArticle ->
                                    MyArticleCard(
                                        article = secondArticle,
                                        navController = navController,
                                        modifier = Modifier
                                            .weight(1f) // Take the other half of the row's space
                                    )
                                }
                            }
                        }
                    }
                }
                // Show error state if an error occurs
                is ArticlePaginationState.Error -> {
                    Text(result.message, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllArticlesScreenPreview() {
    AllArticlesScreen(navController = rememberNavController())
}
