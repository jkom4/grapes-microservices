package grapes.microservices.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import grapes.microservices.models.data.Article
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import coil3.compose.AsyncImage

@Composable
fun MyArticleCard(
    article: Article, // Article data to be displayed
    navController: NavController, // Navigation controller to handle navigation
    modifier: Modifier = Modifier // Modifier to apply to the card
) {
    // Card component to display article details
    Card(
        shape = RoundedCornerShape(12.dp), // Rounded corners for the card
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Card shadow
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface, // Background color of the card
            contentColor = MaterialTheme.colorScheme.onSurface // Text color on the card
        ),
        modifier = modifier
            .padding(8.dp) // Padding around the card
            .clickable { navController.navigate("article_detail/${article.id}") } // Navigate to article detail screen when clicked
    ) {
        // Column to arrange content vertically within the card
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .heightIn(min = 220.dp) // Minimum height for the card
        ) {
            // Box to hold the image and price label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp) // Set the height of the image box
            ) {
                // Asynchronously load and display the article's image
                AsyncImage(
                    model = article.picture, // Load the article's picture from its URL/path
                    contentDescription = "Image of ${article.name}", // Image description for accessibility
                    modifier = Modifier
                        .fillMaxSize() // Image takes up the full space of the box
                )

                // Display the price label for the article in a pink background
                Text(
                    text = "%.2f €/kg".format(article.priceKg), // Price formatted with 2 decimal points
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold, // Make the price text bold
                        color = MaterialTheme.colorScheme.onPrimary // Set text color to be readable on primary color
                    ),
                    modifier = Modifier
                        .padding(start = 8.dp, top = 16.dp) // Padding for position
                        .background(
                            color = MaterialTheme.colorScheme.secondary, // Background color of the price label
                            shape = RoundedCornerShape(8.dp) // Rounded corners for the price label
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp) // Padding inside the price label
                        .align(Alignment.TopStart) // Position at the top left corner
                )
            }

            // Row to display the article's title and the shopping cart icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp), // Padding around the row
                verticalAlignment = Alignment.CenterVertically, // Vertically align items in the center
            ) {
                // Display the title of the article
                Text(
                    text = article.name, // Article name
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold, // Make the title bold
                        color = MaterialTheme.colorScheme.onSurface // Text color based on surface theme
                    ),
                    modifier = Modifier.weight(1f) // Title takes up remaining space in the row
                )

                // Shopping cart icon as a button, with a pink circular background
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondary, // Background color of the button
                            shape = CircleShape // Round shape for the button
                        )
                        .clickable { /* Add to cart action goes here */ } // Handle cart addition
                        .size(40.dp) // Size of the button
                        .align(Alignment.CenterVertically) // Align the button vertically in the center
                ) {
                    // Shopping cart icon
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart, // Use shopping cart icon
                        contentDescription = "Add to cart", // Description for accessibility
                        tint = Color.White, // Set the icon color to white
                        modifier = Modifier
                            .size(24.dp) // Set the size of the icon
                            .align(Alignment.Center) // Align the icon in the center of the box
                    )
                }
            }
        }
    }
}
