package grapes.microservices.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import grapes.microservices.models.data.Article
import grapes.microservices.models.data.ArticleDetails
import grapes.microservices.models.data.ArticleMetricByUnit
import grapes.microservices.models.data.Opinion
import grapes.microservices.ui.theme.Black
import grapes.microservices.ui.theme.White

@Composable
fun MyArticleCardDetails(article: ArticleDetails, modifier: Modifier = Modifier) {

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
        Column(modifier = Modifier
            .fillMaxWidth()) {
            Row(modifier = Modifier.align(Alignment.End)) {
                Text(article.rating.toString())
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "star",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            MyImageFromUrl(
                modifier = Modifier.size(120.dp),
                imageUrl = article.picture,
            )
            Text(article.name, style = MaterialTheme.typography.bodyLarge)
            Text(article.metric.toString())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyArticleCardDetailsPreview() {
    MyArticleCardDetails(
        article = ArticleDetails(
            id = "1",
            name = "Test Article",
            category = "Test Category",
            family = "Test Family",
            description = "This is a test article",
            picture = "https://example.com/image.jpg",
            rating = 4.5f,
            metric = ArticleMetricByUnit(
                price = 4.5,
                stock = 5.0
            ),
            messages = listOf<Opinion>(),
        ),
        modifier = Modifier.fillMaxSize()
    )
}