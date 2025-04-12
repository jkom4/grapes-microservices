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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
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
    title: String, // The title to display above the article list
    articles: List<Article>, // List of articles to display
    orientation: Orientation = Vertical, // Orientation for the scroll (Vertical or Horizontal)
    navController: NavController, // NavController to handle navigation when an item is clicked
    modifier: Modifier = Modifier // Modifier to apply any extra customization to the layout
) {
    // A state for the scroll, used to remember the scroll position
    val scrollState = rememberScrollState()

    // A column to hold the title and list of articles
    Column(
        modifier = modifier.fillMaxWidth() // Ensure the column takes up the full width
    ) {
        // Display the title for the list
        Text(
            text = title, // Title of the section (e.g., "All Articles")
            style = MaterialTheme.typography.bodyLarge, // Style of the title
            fontWeight = FontWeight.Bold, // Make the title bold
            modifier = Modifier
                .fillMaxWidth() // Make sure the title spans the entire width
                .padding(horizontal = 16.dp, vertical = 8.dp) // Add padding around the title
        )

        // Check if there are articles available to display
        if (articles.isEmpty()) {
            // Display a message if no articles are available
            Text(
                text = "No articles available", // Message indicating no data
                style = MaterialTheme.typography.bodyMedium, // Medium style for the message
                modifier = Modifier.padding(16.dp) // Padding around the message
            )
        } else {
            // Horizontal scroll layout for articles
            Row(
                modifier = Modifier
                    .fillMaxWidth() // Take up full width
                    .horizontalScroll(scrollState) // Allow horizontal scrolling
                    .padding(horizontal = 16.dp) // Padding on the left and right for the articles
            ) {
                // Loop through the list of articles and display each one
                articles.forEach { item ->
                    MyArticleCard(
                        article = item, // Passing the current article to the card
                        navController = navController, // Navigation controller for handling clicks
                        modifier = Modifier
                            .padding(end = 12.dp) // Add some spacing between cards
                            .width(180.dp) // Set the width for each article card for consistent sizing
                    )
                }
            }
        }
    }
}
