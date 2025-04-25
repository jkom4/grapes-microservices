package grapes.microservices.views.components.ArticleDetailsComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import grapes.microservices.R
import grapes.microservices.models.data.Article

@Composable
fun ArticleInfo(
    article: Article,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    // Display article name
    Text(
        text = article.name,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = Color(0xFF9B00D8)
    )

    // Display article image
    AsyncImage(
        model = article.picture,
        contentDescription = "${stringResource(R.string.cart_image_description)} ${article.name}",
        modifier = Modifier
            .size(180.dp)
            .padding(vertical = 16.dp)
    )

    // Favorite button aligned to the end
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 32.dp),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(R.string.favorite_description),
                tint = Color(0xFF9B00D8)
            )
        }
    }
}