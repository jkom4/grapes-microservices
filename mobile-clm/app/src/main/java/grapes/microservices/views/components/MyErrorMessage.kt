package grapes.microservices.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import grapes.microservices.R
import grapes.microservices.ui.theme.White

@Composable
fun MyErrorMessage(message: String,
                   onRefreshPressed: () -> Unit = {}) {
    Column(modifier = Modifier
        .background(color = MaterialTheme.colorScheme.tertiary, shape = MaterialTheme.shapes.large)
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,) {
        Text(message, color = Color.Red, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRefreshPressed) {
            Text(stringResource(R.string.refresh), color = White)
        }
    }
}