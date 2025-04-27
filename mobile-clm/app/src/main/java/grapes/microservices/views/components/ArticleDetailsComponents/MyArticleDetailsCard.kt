package grapes.microservices.views.components.ArticleDetailsComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import grapes.microservices.R
import grapes.microservices.models.data.Article
import grapes.microservices.viewmodel.ArticleViewModel
import grapes.microservices.viewmodel.CartState
import kotlinx.coroutines.delay

@Composable
fun ArticleDetailsCard(
    article: Article,
    cartState: CartState,
    viewModel: ArticleViewModel,
    navController: NavController
) {
    // Manage quantity and selection state
    var quantity by remember { mutableStateOf("") }
    var isUnitSelected by remember { mutableStateOf(true) }
    var showConfirmation by remember { mutableStateOf(false) }

    // Card for article details
    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Description section
            Text(
                text = stringResource(R.string.description),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF222222)
            )
            Text(
                text = article.description,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Origin
            Text(
                text = "${stringResource(R.string.origin)} : ${article.origin}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4A4A4A),
                modifier = Modifier.padding(top = 12.dp)
            )

            // Stock information
            Text(
                text = "${stringResource(R.string.stock)} : ${article.stockKg} ${stringResource(R.string.unit_kg)} / ${article.stockUnit} ${stringResource(R.string.unit_single)}",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Price per unit and per kilogram
            Text(
                text = stringResource(R.string.price_unit, article.priceUnit),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000),
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = stringResource(R.string.price_kg, article.priceKg),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000),
                modifier = Modifier.padding(top = 2.dp)
            )

            // Rating section
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                repeat(4) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107))
                }
                Icon(Icons.Outlined.StarBorder, contentDescription = null, tint = Color(0xFFFFC107))
                Text("(4.4)", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
            }

            // Unit selection (unit or kilogram)
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                RadioButton(selected = isUnitSelected, onClick = { isUnitSelected = true })
                Text(stringResource(R.string.units))
                RadioButton(selected = !isUnitSelected, onClick = { isUnitSelected = false })
                Text(stringResource(R.string.kilograms))
            }

            // Quantity input field
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text(stringResource(R.string.quantity)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            // Cart state handling
            when (cartState) {
                is CartState.Loading -> Text(
                    stringResource(R.string.adding_in_progress),
                    modifier = Modifier.padding(top = 8.dp)
                )

                is CartState.Success -> {
                    LaunchedEffect(cartState) {
                        showConfirmation = true
                        delay(1500)
                        showConfirmation = false
                        viewModel.resetPaymentState()
                        navController.popBackStack()
                    }

                    AnimatedVisibility(
                        visible = showConfirmation,
                        enter = fadeIn() + scaleIn(),
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.added_to_cart),
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                is CartState.Error -> Text(cartState.message, color = Color.Red)

                else -> {}
            }

            // Add to cart button
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    if (qty > 0) {
                        viewModel.addToCart(
                            articleId = article.id,
                            quantityKg = if (!isUnitSelected) qty.toFloat() else 0f,
                            quantityUnit = if (isUnitSelected) qty else 0
                        )
                    }
                },
                enabled = quantity.toIntOrNull()?.let { it > 0 } ?: false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF9B00D8)
                ),
                border = BorderStroke(2.dp, Color(0xFF9B00D8))
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = stringResource(R.string.add_to_cart))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_to_cart), fontWeight = FontWeight.Bold)
            }
        }
    }
}