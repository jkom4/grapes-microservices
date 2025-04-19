package grapes.microservices.views.CartScreen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import grapes.microservices.R
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.viewmodel.ArticleViewModel
import grapes.microservices.viewmodel.ArticleViewModelFactory
import grapes.microservices.viewmodel.CartScreenState
import grapes.microservices.viewmodel.PaymentState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController
) {
    // Obtenir le Context pour initialiser CartManager et ouvrir l'URL
    val context = LocalContext.current

    // Initialiser les dépendances
    val repository = remember { ArticleRepository(RetrofitClient.articleApiService) }
    val apiService = remember { RetrofitClient.articleApiService }

    // Créer la factory avec CartManager
    val viewModelFactory = remember {
        ArticleViewModelFactory.createFactory(
            context = context,
            repository = repository,
            apiService = apiService
        )
    }

    // Initialiser le ViewModel
    val viewModel: ArticleViewModel = viewModel(factory = viewModelFactory)
    val cartState = viewModel.cartScreenState.collectAsState()
    val paymentState = viewModel.paymentState.collectAsState()

    // Form fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Retrieve the error message string in the composable scope
    val errorRequiredFieldsMessage = stringResource(R.string.error_required_fields)

    // Animation state
    var showPaymentConfirmation by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (showPaymentConfirmation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (showPaymentConfirmation) 1f else 0f,
        animationSpec = tween(durationMillis = 600)
    )

    // Shipping Fee
    val shippingFee = 5f

    // Charger le panier au démarrage
    LaunchedEffect(Unit) {
        viewModel.fetchCart()
    }

    // Gérer le succès du paiement
    LaunchedEffect(paymentState.value) {
        if (paymentState.value is PaymentState.Success) {
            showPaymentConfirmation = true
            delay(2000)
            navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
            viewModel.resetPaymentState()
            showPaymentConfirmation = false
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        top = paddingValues.calculateTopPadding(),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
                    )
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                item {
                    Text(
                        text = stringResource(R.string.cart_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                when (val state = cartState.value) {
                    is CartScreenState.Loading -> {
                        item {
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
                    }
                    is CartScreenState.Success -> {
                        val cart = state.cart
                        if (cart.items.isEmpty()) {
                            item {
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
                            }
                        } else {
                            // Cart Items
                            item {
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
                                                    onClick = {
                                                        viewModel.removeFromCart(item.id)
                                                    },
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
                                    }
                                }
                            }

                            // Cost Summary
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.cart_summary),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Divider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = stringResource(R.string.cart_subtotal),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "%.2f €".format(cart.totalPrice),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = stringResource(R.string.cart_shipping),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "%.2f €".format(shippingFee),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = stringResource(R.string.cart_total),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "%.2f €".format(cart.totalPrice + shippingFee),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            // Payment Form
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.background
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.payment_info),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Divider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )

                                        val fields = listOf(
                                            Triple(R.string.field_name, fullName, { newValue: String -> fullName = newValue }),
                                            Triple(R.string.field_email, email, { newValue: String -> email = newValue }),
                                            Triple(R.string.field_phone, phoneNumber, { newValue: String -> phoneNumber = newValue }),
                                            Triple(R.string.field_address, address, { newValue: String -> address = newValue }),
                                            Triple(R.string.field_card_number, cardNumber, { newValue: String -> cardNumber = newValue }),
                                            Triple(R.string.field_expiry, expiryDate, { newValue: String -> expiryDate = newValue }),
                                            Triple(R.string.field_cvc, cvc, { newValue: String -> cvc = newValue })
                                        )

                                        fields.forEach { (labelResId, value, onChange) ->
                                            OutlinedTextField(
                                                value = value,
                                                onValueChange = onChange,
                                                label = { Text(stringResource(labelResId), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                                    cursorColor = MaterialTheme.colorScheme.primary,
                                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                keyboardOptions = when (labelResId) {
                                                    R.string.field_phone, R.string.field_zip, R.string.field_card_number, R.string.field_cvc -> KeyboardOptions(keyboardType = KeyboardType.Number)
                                                    R.string.field_email -> KeyboardOptions(keyboardType = KeyboardType.Email)
                                                    else -> KeyboardOptions.Default
                                                },
                                                singleLine = true
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                        ) {
                                            Checkbox(
                                                checked = termsAccepted,
                                                onCheckedChange = { checked -> termsAccepted = checked },
                                                colors = CheckboxDefaults.colors(
                                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                                                    checkedColor = MaterialTheme.colorScheme.primary,
                                                    uncheckedColor = MaterialTheme.colorScheme.outline
                                                )
                                            )
                                            Text(
                                                text = stringResource(R.string.terms_conditions),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Pay Button and Payment Status
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Form validation for Stripe button
                                    val formIsValid = fullName.isNotBlank() && phoneNumber.isNotBlank() &&
                                            address.isNotBlank() && cardNumber.isNotBlank() &&
                                            expiryDate.isNotBlank() && cvc.isNotBlank()

                                    Button(
                                        onClick = {
                                            // Validate required fields
                                            if (fullName.isBlank() || phoneNumber.isBlank() || address.isBlank() ||
                                                cardNumber.isBlank() || expiryDate.isBlank() || cvc.isBlank()
                                            ) {
                                                errorMessage = errorRequiredFieldsMessage
                                            } else {
                                                errorMessage = ""
                                                viewModel.payAndClearCart(
                                                    address = address,
                                                    phoneNumber = phoneNumber,
                                                    customerName = fullName,
                                                    country = "",
                                                    postalCode = ""
                                                )
                                            }
                                        },
                                        enabled = termsAccepted && cart.items.isNotEmpty(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 2.dp,
                                            pressedElevation = 4.dp
                                        )
                                    ) {
                                        Text(
                                            text = stringResource(R.string.pay_button),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }

                                    // Stripe Button
                                    Button(
                                        onClick = {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse("https://buy.stripe.com/test_fZe3cr0INeIK50c6oo"))
                                            )
                                        },
                                        enabled = termsAccepted && cart.items.isNotEmpty() && formIsValid,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary,
                                            contentColor = MaterialTheme.colorScheme.onSecondary,
                                            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                            disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 2.dp,
                                            pressedElevation = 4.dp
                                        )
                                    ) {
                                        Text(
                                            text = "Stripe",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }

                                    if (errorMessage.isNotEmpty()) {
                                        Text(
                                            text = errorMessage,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp)
                                        )
                                    }

                                    when (val payment = paymentState.value) {
                                        is PaymentState.Loading -> {
                                            LinearProgressIndicator(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        is PaymentState.Error -> {
                                            Text(
                                                text = "${stringResource(R.string.error_prefix)}: ${payment.message}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp)
                                            )
                                        }
                                        else -> {
                                            // Idle: no message
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is CartScreenState.Error -> {
                        item {
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
                                    text = "${stringResource(R.string.error_prefix)}: ${state.message}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Payment Confirmation Animation
            if (showPaymentConfirmation) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .size(250.dp)
                            .scale(animatedScale)
                            .alpha(animatedAlpha),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = stringResource(R.string.payment_success),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.payment_success),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.payment_thanks),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}