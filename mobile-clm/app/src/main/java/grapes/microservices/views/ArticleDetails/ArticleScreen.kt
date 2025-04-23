package grapes.microservices.views.ArticleDetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.viewmodel.ArticleState
import grapes.microservices.viewmodel.ArticleViewModel
import grapes.microservices.viewmodel.ArticleViewModelFactory
import grapes.microservices.viewmodel.CartState
import androidx.compose.ui.res.stringResource
import grapes.microservices.R
import kotlinx.coroutines.delay

@Composable
fun ArticleDetailScreen(
    articleId: Int,
    navController: NavController
) {
    var showConfirmation by remember { mutableStateOf(false) }

    // Initialize the repository and ViewModel
    val repository = ArticleRepository(RetrofitClient.articleApiService)
    val viewModel: ArticleViewModel = viewModel(
        factory = ArticleViewModelFactory(repository, RetrofitClient.articleApiService)
    )
    val articleState = viewModel.articleState.collectAsState()
    val cartState = viewModel.cartState.collectAsState()

    // Manage quantity and selection state
    var quantity by remember { mutableStateOf("") }
    var isUnitSelected by remember { mutableStateOf(true) }
    var isFavorite by remember { mutableStateOf(false) }

    // Fetch article details by ID
    LaunchedEffect(articleId) {
        viewModel.fetchArticleById(articleId)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD6F4))
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back_button_description),
                    tint = Color(0xFF9B00D8)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.article_detail_title),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
        }

        when (val state = articleState.value) {
            is ArticleState.Loading -> Text(stringResource(R.string.loading))

            is ArticleState.Success -> {
                val article = state.article

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

                // Favorite button
                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.favorite_description),
                        tint = Color(0xFF9B00D8)
                    )
                }

                // Card for article details
                Card(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier
                        .padding(top = 16.dp),
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
                            verticalAlignment = Alignment.CenterVertically,
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
                            verticalAlignment = Alignment.CenterVertically
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
                        when (val cart = cartState.value) {
                            is CartState.Loading -> Text(
                                stringResource(R.string.adding_in_progress),
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            is CartState.Success -> {
                                LaunchedEffect(cart) {
                                    showConfirmation = true
                                    delay(1500)
                                    showConfirmation = false
                                    viewModel.resetCartState()
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

                            is CartState.Error -> Text(cart.message, color = Color.Red)

                            else -> {}
                        }

                        // Add to cart button
                        Button(
                            onClick = {
                                val qty = quantity.toFloatOrNull() ?: 0f
                                if (qty > 0) {
                                    viewModel.addToCart(
                                        articleId = articleId,
                                        quantityKg = if (!isUnitSelected) qty else 0f,
                                        quantityUnit = if (isUnitSelected) qty else 0f
                                    )
                                }
                            },
                            enabled = quantity.toFloatOrNull()?.let { it > 0 } ?: false,
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

            is ArticleState.Error -> Text(
                "${stringResource(R.string.error_prefix)} : ${state.message}",
                color = Color.Red
            )
        }
    }
}