package grapes.microservices.views.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import grapes.microservices.R
import grapes.microservices.Screen
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.ArticleFilter
import grapes.microservices.ui.theme.MobileCLMTheme
import grapes.microservices.viewmodels.home.HomeState
import grapes.microservices.viewmodels.home.HomeViewModel
import grapes.microservices.views.components.MyArticleCardList
import grapes.microservices.views.components.MyBottomBar
import grapes.microservices.views.components.MyErrorMessage
import grapes.microservices.views.components.MyLoadingCircle
import grapes.microservices.views.components.MyTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(navController: NavHostController) {
    val space = 16.dp
    val vm = koinViewModel<HomeViewModel>()

    Scaffold(
        Modifier
            .background(color = MaterialTheme.colorScheme.background),
        bottomBar = {
            MyBottomBar(
                modifier = Modifier.padding(bottom = space),
                navController = navController,
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(space)
            .padding(top = paddingValues.calculateTopPadding())
            .padding(bottom = paddingValues.calculateBottomPadding())
            .fillMaxSize()
        ) {
            val fetchState: HomeState<Unit> by vm.state
            val filters: ArticleFilter? by vm.filterSettings.collectAsState()
            val articles: List<Article> by vm.articles.collectAsState()

            MyTopBar(Modifier.padding(bottom = space))

            MyFilterSearchLayer(
                Modifier.padding(bottom = space),
                currentFilters = filters,
                viewModel = vm
            )

            // On fetch data, check status and display accordingly
            when (fetchState) {
                is HomeState.Loading -> MyLoadingCircle()
                is HomeState.Success -> Articles(filters = filters, articles = articles)
                // if error, reload page
                is HomeState.Error -> MyErrorMessage((fetchState as HomeState.Error).message) {
                    Screen.refresh(navController)
                }
            }
        }
    }
}

@Composable
private fun Articles(
    filters: ArticleFilter?,
    articles: List<Article>
) {
    if (filters == null) return
    val filterIsReset = filters.isReset()

    // if filter is not empty, then searching mode is displayed
    if (filterIsReset) {
        MyArticleCardList(stringResource(R.string.popular), articles, Horizontal)

        MyArticleCardList(stringResource(R.string.for_you), articles, Horizontal)
    } else {
        MyArticleCardList(stringResource(R.string.results), articles, Vertical)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MobileCLMTheme(false) {
        HomeScreen(rememberNavController())
    }
}