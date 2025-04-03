package grapes.microservices.views.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import grapes.microservices.ui.theme.MobileCLMTheme
import grapes.microservices.viewmodels.HomeViewModel
import grapes.microservices.views.components.MyArticleCardList
import grapes.microservices.views.components.MyTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(navController: NavHostController) {
    val space = 16.dp
    val vm = koinViewModel<HomeViewModel>()

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
                MyArticleCardList("Popular", vm.getArticles().take(5), Horizontal)

                MyArticleCardList("For You", vm.getArticles().take(5), Horizontal)
            } else {
                MyArticleCardList(title = "Results", articles = vm.getArticles())
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