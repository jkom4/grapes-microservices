package grapes.microservices.views.components.CartComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import grapes.microservices.R
import grapes.microservices.models.data.Cart
import grapes.microservices.models.data.CartItem
import grapes.microservices.viewmodel.ArticleViewModel
import grapes.microservices.viewmodel.CartScreenState

@Composable
fun CartItemsList(
    viewModel: ArticleViewModel,
    cartState: CartScreenState
) {
    when (cartState) {
        is CartScreenState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        is CartScreenState.Success -> {
            val cart = cartState.cart
            if (cart.items.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = stringResource(R.string.cart_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cart.items, key = { item -> item.id }) { item ->
                            CartItemRow(item = item, onRemove = { viewModel.removeFromCart(item.id) })
                        }
                    }
                }
            }
        }
        is CartScreenState.Error -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "${stringResource(R.string.error_prefix)}: ${cartState.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.picturePath ?: "https://via.placeholder.com/100",
            contentDescription = "${stringResource(R.string.cart_image_description)} ${item.articleName}",
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.articleName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${stringResource(R.string.cart_quantity)}: ${if (item.quantityKg > 0) "${item.quantityKg} kg" else "${item.quantity} unit"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.cart_price, item.price),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.cart_item_total,
                    if (item.quantityKg > 0) item.quantityKg * item.price else item.quantity * item.price
                ),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "${stringResource(R.string.delete_item)} ${item.articleName}",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}