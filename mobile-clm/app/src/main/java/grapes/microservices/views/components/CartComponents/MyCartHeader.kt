package grapes.microservices.views.components.CartComponents

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import grapes.microservices.R

@Composable
fun CartHeader() {
    Text(
        text = stringResource(R.string.cart_title),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}