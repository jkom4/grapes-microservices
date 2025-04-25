package grapes.microservices.views.AllArticles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import grapes.microservices.R
import grapes.microservices.models.data.Article
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.viewmodel.ArticlePaginationState
import grapes.microservices.viewmodel.ArticleViewModel
import grapes.microservices.viewmodel.ArticleViewModelFactory
import grapes.microservices.views.components.MyArticleCard
import grapes.microservices.views.components.MySearchBar

@Composable
fun AllArticlesScreen(navController: NavHostController) {
    // Obtain context to load CartManager
    val context = LocalContext.current

    val repository = remember { ArticleRepository(RetrofitClient.articleApiService) }
    val apiService = remember { RetrofitClient.articleApiService }

    val viewModelFactory = remember {
        ArticleViewModelFactory.createFactory(
            context = context,
            repository = repository,
            apiService = apiService
        )
    }

    // Init viewModel
    val viewModel: ArticleViewModel = viewModel(factory = viewModelFactory)

    val space = 16.dp
    val state = viewModel.articlePaginationState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Article>?>(null) }
    var isSearching by remember { mutableStateOf(false) }

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
            Text(
                text = stringResource(R.string.all_products_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = space)
            )

            // Search bar
            MySearchBar(
                modifier = Modifier.fillMaxWidth(),
                query = searchQuery,
                onQueryChanged = { newQuery ->
                    searchQuery = newQuery
                },
                onSearchStarted = {
                    isSearching = true
                    searchResults = null
                },
                onResults = { results ->
                    searchResults = results
                    isSearching = false
                }
            )

            // Search in progress
            if (isSearching) {
                Text("Searching...", style = MaterialTheme.typography.titleMedium)
            }
            // Show search results if any
            else if (searchResults != null) {
                if (searchResults!!.isEmpty()) {
                    Text("No articles found.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(bottom = space)
                    ) {
                        items(searchResults!!.chunked(2)) { articlePair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = space),
                                horizontalArrangement = Arrangement.spacedBy(space)
                            ) {
                                MyArticleCard(
                                    article = articlePair[0],
                                    navController = navController,
                                    modifier = Modifier.weight(1f)
                                )
                                articlePair.getOrNull(1)?.let { secondArticle ->
                                    MyArticleCard(
                                        article = secondArticle,
                                        navController = navController,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Default pagination view
            else {
                when (val result = state.value) {
                    is ArticlePaginationState.Loading -> {
                        Text("Loading articles...", style = MaterialTheme.typography.titleMedium)
                    }
                    is ArticlePaginationState.Success -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentPadding = PaddingValues(bottom = space)
                        ) {
                            items(result.articles.chunked(2)) { articlePair ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = space),
                                    horizontalArrangement = Arrangement.spacedBy(space)
                                ) {
                                    MyArticleCard(
                                        article = articlePair[0],
                                        navController = navController,
                                        modifier = Modifier.weight(1f)
                                    )
                                    articlePair.getOrNull(1)?.let { secondArticle ->
                                        MyArticleCard(
                                            article = secondArticle,
                                            navController = navController,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is ArticlePaginationState.Error -> {
                        Text(result.message, style = MaterialTheme.typography.bodyMedium)
                    }
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