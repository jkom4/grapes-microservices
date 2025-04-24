package grapes.microservices.views.Home


import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import grapes.microservices.models.data.Article
import grapes.microservices.views.components.MySearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFilterSearchLayer(
    modifier: Modifier,
    query: String,
    onSearchStarted: () -> Unit,
    onSearchResults: (List<Article>) -> Unit,
    onQueryChanged: (String) -> Unit
) {

    Row(modifier = modifier) {
        // Search bar with callbacks
        MySearchBar(
            modifier = Modifier.weight(1f),
            query = query,
            onQueryChanged = { onQueryChanged(it) },
            onSearchStarted = { onSearchStarted() },
            onResults = { response ->
                onSearchResults(response)
            }
        )
    }
}