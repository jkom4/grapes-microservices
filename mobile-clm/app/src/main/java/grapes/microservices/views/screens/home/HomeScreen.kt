package grapes.microservices.views.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import grapes.microservices.ui.theme.MobileCLMTheme
import grapes.microservices.viewmodels.HomeViewModel
import grapes.microservices.views.components.MyArticleCardList
import grapes.microservices.views.components.MyTopBar
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(navController: NavHostController) {
    val space = 16.dp
    val vm = koinViewModel<HomeViewModel>()

    val articles = vm.articles.collectAsState().value

    Scaffold(
        Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(space)
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
        ) {
            MyTopBar(Modifier.padding(bottom = space))

            MyFilterSearchLayer(Modifier.padding(bottom = space))

            val query = vm.filterSettings.collectAsState().value.query
            if (query == "") {
                // Affichage des 5 premiers articles (par exemple, populaires)
                MyArticleCardList(
                    title = "Popular",
                    articles = articles.take(5), // Prend les 5 premiers articles
                    orientation = Orientation.Horizontal
                )

                MyArticleCardList(
                    title = "For You",
                    articles = articles.take(5), // Prend les 5 premiers articles
                    orientation = Orientation.Horizontal
                )
            } else {
                // Affichage des articles filtrés selon la recherche
                MyArticleCardList(
                    title = "Results",
                    articles = articles // Affiche tous les articles correspondant à la recherche
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
