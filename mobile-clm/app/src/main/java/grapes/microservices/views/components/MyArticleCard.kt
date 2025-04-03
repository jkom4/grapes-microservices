package grapes.microservices.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import grapes.microservices.models.data.Article

@Composable
fun MyArticleCard(article: Article, modifier: Modifier = Modifier) {

    Card(
        shape = RectangleShape,
        colors = CardColors(
            containerColor = Color.Transparent,
            contentColor = CardDefaults.cardColors().contentColor,
            disabledContainerColor = CardDefaults.cardColors().disabledContainerColor,
            disabledContentColor = CardDefaults.cardColors().disabledContentColor
        ),
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Column {
            MyImageFromUrl(
                modifier = Modifier.size(120.dp),
                imageUrl = article.picture,
            )
            Text(article.name, style = MaterialTheme.typography.bodyLarge)
            Text(article.metric.priceToString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}