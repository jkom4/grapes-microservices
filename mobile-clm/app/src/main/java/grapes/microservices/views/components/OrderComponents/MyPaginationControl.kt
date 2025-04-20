package grapes.microservices.views.components.OrderComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import grapes.microservices.R

@Composable
fun PaginationControls(
    currentPage: Int,
    hasMorePages: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onPreviousPage,
            enabled = currentPage > 0
        ) {
            Text(stringResource(R.string.previous_page))
        }
        Text(
            text = stringResource(R.string.page_number, currentPage + 1),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Button(
            onClick = onNextPage,
            enabled = hasMorePages
        ) {
            Text(stringResource(R.string.next_page))
        }
    }
}