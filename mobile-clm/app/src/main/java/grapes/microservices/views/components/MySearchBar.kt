package grapes.microservices.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import grapes.microservices.models.data.Article
import grapes.microservices.models.network.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun MySearchBar(
    modifier: Modifier,
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchStarted: () -> Unit,
    onResults: (List<Article>) -> Unit
) {
    var text by remember { mutableStateOf(query) }
    val coroutineScope = rememberCoroutineScope()

    fun search(query: String) {
        onSearchStarted() // Notify parent that search is starting
        coroutineScope.launch {
            try {
                val results = RetrofitClient.articleApiService.searchArticles(query)
                onResults(results) // Send back the results
            } catch (e: Exception) {
                onResults(emptyList()) // Or handle errors appropriately
            }
        }
    }

    TextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onQueryChanged(newText) // Update query state upstream
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { search(text) }),
        placeholder = {
            Text(
                text = "Search ...",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable { search(text) }
            )
        },
        modifier = modifier
            .padding(vertical = 0.dp)
            .height(50.dp)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(25.dp)
            )
    )
}