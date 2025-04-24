package grapes.microservices.views.ArticleDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.viewmodel.ArticleState
import grapes.microservices.viewmodel.ArticleViewModel
import grapes.microservices.viewmodel.ArticleViewModelFactory
import grapes.microservices.views.components.ArticleDetailsComponents.ArticleDetailsCard
import grapes.microservices.views.components.ArticleDetailsComponents.ArticleHeader
import grapes.microservices.views.components.ArticleDetailsComponents.ArticleInfo

@Composable
fun ArticleDetailScreen(
    articleId: Int,
    navController: NavController
) {
    // Get the Context to initialize dependencies
    val context = LocalContext.current

    // Initialize dependencies
    val repository = remember { ArticleRepository(RetrofitClient.articleApiService) }
    val apiService = remember { RetrofitClient.articleApiService }

    // Create the factory for ViewModel
    val viewModelFactory = remember {
        ArticleViewModelFactory.createFactory(
            context = context,
            repository = repository,
            apiService = apiService
        )
    }

    // Initialize the ViewModel
    val viewModel: ArticleViewModel = viewModel(factory = viewModelFactory)
    val articleState = viewModel.articleState.collectAsState()
    val cartState = viewModel.cartState.collectAsState()
    val isFavorite = viewModel.isFavorite.collectAsState()

    // Fetch article details by ID
    LaunchedEffect(articleId) {
        viewModel.fetchArticleById(articleId)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD6F4))
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArticleHeader(navController = navController)
        when (val state = articleState.value) {
            is ArticleState.Loading -> Text("Loading...")
            is ArticleState.Success -> {
                ArticleInfo(
                    article = state.article,
                    isFavorite = isFavorite.value,
                    onFavoriteClick = { viewModel.toggleFavorite() }
                )
                ArticleDetailsCard(
                    article = state.article,
                    cartState = cartState.value,
                    viewModel = viewModel,
                    navController = navController
                )
            }
            is ArticleState.Error -> Text(
                "Error: ${state.message}",
                color = Color.Red
            )
        }
    }
}