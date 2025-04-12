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
    val repository = ArticleRepository(RetrofitClient.articleApiService) // À remplacer par ton repository
    val apiService = RetrofitClient.articleApiService // Ton API service

    val viewModelFactory = ArticleViewModelFactory(repository, apiService)
    val viewModel: ArticleViewModel = viewModel(factory = viewModelFactory)

    val space = 16.dp
    val state = viewModel.articlePaginationState.collectAsState()

    // Déclare un état pour gérer la valeur de la recherche
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
            // Ajouter le titre "All products"
            Text(
                text = "All products",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold // Met le texte en gras
                ),
                modifier = Modifier.padding(bottom = space) // Ajoute une marge en bas du titre
            )

            MySearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp), // Ajoute une marge en bas de la barre de recherche
                query = searchQuery, // La valeur de la recherche
                onValueChange = { newQuery ->
                    searchQuery = newQuery // Met à jour la valeur de la recherche
                }
            )

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
                            // Créer une row avec deux articles
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = space),
                                horizontalArrangement = Arrangement.spacedBy(space)
                            ) {
                                // Afficher le premier article de la paire
                                MyArticleCard(
                                    article = articlePair[0],
                                    navController = navController,
                                    modifier = Modifier
                                        .weight(1f) // Occuper la moitié de la ligne
                                )

                                // Si la paire contient un second article, l'afficher aussi
                                articlePair.getOrNull(1)?.let { secondArticle ->
                                    MyArticleCard(
                                        article = secondArticle,
                                        navController = navController,
                                        modifier = Modifier
                                            .weight(1f) // Occuper l'autre moitié de la ligne
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

@Preview(showBackground = true)
@Composable
fun AllArticlesScreenPreview() {
    AllArticlesScreen(navController = rememberNavController())
}