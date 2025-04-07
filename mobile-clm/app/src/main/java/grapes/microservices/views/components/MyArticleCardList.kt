package grapes.microservices.views.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import grapes.microservices.models.data.Article
import grapes.microservices.ui.theme.Black
import kotlin.math.ceil

@Composable
fun MyArticleCardList(title: String, articles : List<Article>, orientation: Orientation = Vertical, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

    if (articles.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = grapes.microservices.R.string.no_articles_found),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(64.dp)
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
            )
        }
    }

    if (orientation == Horizontal) {
        Column(
            modifier = modifier.fillMaxWidth().padding(vertical = 16.dp)
                .horizontalScroll(scrollState)
        ){
            Row {
                articles.forEach { item ->
                    MyArticleCard(item, modifier = Modifier.padding(end = 16.dp))
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // C'est ici que vous spécifiez 2 colonnes
            modifier = Modifier.fillMaxWidth(), // La grille prend toute la largeur disponible
            contentPadding = PaddingValues(vertical = 16.dp), // Espace autour de l'ensemble des items
            verticalArrangement = Arrangement.spacedBy(16.dp), // Espace vertical entre les lignes de la grille

        ) {
            items(articles) { item ->
                MyArticleCard(item, modifier = Modifier.padding(end = 16.dp))
            }
        }
    }
}