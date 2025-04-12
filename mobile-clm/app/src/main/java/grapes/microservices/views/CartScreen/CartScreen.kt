package grapes.microservices.views.CartScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.viewmodel.ArticleViewModel
import grapes.microservices.viewmodel.ArticleViewModelFactory
import grapes.microservices.viewmodel.CartScreenState
import grapes.microservices.viewmodel.PaymentState

@Composable
fun CartScreen(
    navController: NavController
) {
    val repository = ArticleRepository(RetrofitClient.articleApiService)
    val viewModel: ArticleViewModel = viewModel(
        factory = ArticleViewModelFactory(repository, RetrofitClient.articleApiService)
    )
    val cartState = viewModel.cartScreenState.collectAsState()
    val paymentState = viewModel.paymentState.collectAsState()

    // États pour les champs du formulaire (conservés pour l'UX)
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    // Frais d'expédition
    val shippingFee = 5f

    LaunchedEffect(Unit) {
        viewModel.fetchCart(orderId = 1)
    }

    // Navigation après paiement réussi
    LaunchedEffect(paymentState.value) {
        if (paymentState.value is PaymentState.Success) {
            navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
            viewModel.resetPaymentState()
        }
    }

    // État de défilement
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Votre Panier",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = cartState.value) {
            is CartScreenState.Loading -> {
                Text(text = "Chargement du panier...", style = MaterialTheme.typography.bodyLarge)
            }
            is CartScreenState.Success -> {
                val cart = state.cart
                if (cart.items.isEmpty()) {
                    Text(
                        text = "Votre panier est vide.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    // Liste des articles
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(cart.items) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.picturePath ?: "https://via.placeholder.com/100",
                                    contentDescription = "Image de ${item.articleName}",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(end = 16.dp)
                                )
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.articleName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Quantité: ${if (item.quantityKg > 0) "${item.quantityKg} kg" else "${item.quantity} unités"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Prix unitaire: %.2f €".format(item.price),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Total: %.2f €".format(
                                            if (item.quantityKg > 0) item.quantityKg * item.price else item.quantity * item.price
                                        ),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.removeFromCart(item.id, orderId = 1)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Supprimer ${item.articleName}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    // Résumé des coûts
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(
                            text = "Sous-total: %.2f €".format(cart.totalPrice),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Frais d'expédition: %.2f €".format(shippingFee),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Total à payer: %.2f €".format(cart.totalPrice + shippingFee),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Formulaire de paiement (conservé pour l'UX)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Informations de paiement",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nom complet") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Adresse e-mail") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Numéro de téléphone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Adresse") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text("Pays") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Ville") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Département") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = zipCode,
                        onValueChange = { zipCode = it },
                        label = { Text("Code postal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Numéro de carte bancaire") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Date d'expiration (MM/AA)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = cvc,
                        onValueChange = { cvc = it },
                        label = { Text("CVC") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    // Case à cocher pour les conditions générales
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it }
                        )
                        Text(
                            text = "J'ai lu et j'accepte les conditions générales",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Bouton Payer maintenant
                    Button(
                        onClick = {
                            viewModel.payAndClearCart(orderId = 1)
                        },
                        enabled = termsAccepted &&
                                fullName.isNotBlank() &&
                                email.isNotBlank() &&
                                phoneNumber.isNotBlank() &&
                                address.isNotBlank() &&
                                country.isNotBlank() &&
                                city.isNotBlank() &&
                                department.isNotBlank() &&
                                zipCode.isNotBlank() &&
                                cardNumber.isNotBlank() &&
                                expiryDate.isNotBlank() &&
                                cvc.isNotBlank() &&
                                cartState.value is CartScreenState.Success &&
                                (cartState.value as CartScreenState.Success).cart.items.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Payer maintenant")
                    }

                    // Affichage de l'état du paiement
                    when (val payment = paymentState.value) {
                        is PaymentState.Loading -> {
                            Text(
                                text = "Traitement du paiement...",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        is PaymentState.Error -> {
                            Text(
                                text = payment.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        else -> {
                            // Ne rien afficher pour Idle ou Success
                        }
                    }
                }
            }
            is CartScreenState.Error -> {
                Text(
                    text = "Erreur : ${state.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}