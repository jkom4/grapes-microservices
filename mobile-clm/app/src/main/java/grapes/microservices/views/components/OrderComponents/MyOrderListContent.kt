package grapes.microservices.views.components.OrderComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import grapes.microservices.R
import grapes.microservices.models.data.Order

@Composable
fun OrderListContent(
    orders: List<Order>,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        when {
            isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            error != null -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.error_message),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            orders.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_orders),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            else -> {
                items(orders) { order ->
                    OrderItem(order)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}