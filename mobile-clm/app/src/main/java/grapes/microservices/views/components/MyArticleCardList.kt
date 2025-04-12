package grapes.microservices.views.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import grapes.microservices.models.data.Article

@Composable
fun MyArticleCardList(
    title: String,
    articles: List<Article>,
    orientation: Orientation = Vertical,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (articles.isEmpty()) {
        Text(
            text = "Aucun article disponible",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    } else if (orientation == Horizontal) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .horizontalScroll(scrollState)
                .height(210.dp)
        ) {
            Row {
                articles.forEach { item ->
                    MyArticleCard(
                        article = item,
                        navController = navController,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(articles) { item ->
                MyArticleCard(
                    article = item,
                    navController = navController,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}