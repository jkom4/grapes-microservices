package grapes.microservices.views.ArticleDetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import grapes.microservices.viewmodel.ArticleState
import grapes.microservices.viewmodel.ArticleViewModel
import grapes.microservices.viewmodel.ArticleViewModelFactory
import grapes.microservices.viewmodel.CartState

@Composable
fun ArticleDetailScreen(
    articleId: Int,
    navController: NavController
) {
    val repository = ArticleRepository(RetrofitClient.articleApiService)
    val viewModel: ArticleViewModel = viewModel(
        factory = ArticleViewModelFactory(repository, RetrofitClient.articleApiService)
    )
    val articleState = viewModel.articleState.collectAsState()
    val cartState = viewModel.cartState.collectAsState()

    // États pour la sélection de quantité
    var quantity by remember { mutableStateOf("") }
    var isUnitSelected by remember { mutableStateOf(true) }

    LaunchedEffect(articleId) {
        viewModel.fetchArticleById(articleId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (val state = articleState.value) {
            is ArticleState.Loading -> {
                Text(text = "Chargement...", style = MaterialTheme.typography.bodyLarge)
            }
            is ArticleState.Success -> {
                val article = state.article
                AsyncImage(
                    model = article.picture,
                    contentDescription = "Image de ${article.name}",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = article.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Prix: %.2f €/kg | %.2f €/unité".format(article.priceKg, article.priceUnit),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Description: ${article.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Origine: ${article.origin}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Stock: ${article.stockKg} kg | ${article.stockUnit} unités",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Sélection du type de quantité
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isUnitSelected,
                        onClick = { isUnitSelected = true }
                    )
                    Text("Unités")
                    RadioButton(
                        selected = !isUnitSelected,
                        onClick = { isUnitSelected = false }
                    )
                    Text("Kilogrammes")
                }

                // Champ de saisie de la quantité
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantité") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Affichage de l'état du panier
                when (val cart = cartState.value) {
                    is CartState.Loading -> {
                        Text(
                            text = "Ajout en cours...",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    is CartState.Success -> {
                        Text(
                            text = "Article ajouté au panier !",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        // Réinitialiser l'état après un court délai pour éviter un clignotement
                        LaunchedEffect(cart) {
                            kotlinx.coroutines.delay(2000)
                            viewModel.resetCartState()
                        }
                    }
                    is CartState.Error -> {
                        Text(
                            text = cart.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    is CartState.Idle -> {
                        // Rien à afficher
                    }
                }

                // Bouton Ajouter au panier
                Button(
                    onClick = {
                        val quantityValue = quantity.toFloatOrNull() ?: 0f
                        if (quantityValue > 0) {
                            viewModel.addToCart(
                                articleId = articleId,
                                quantityKg = if (!isUnitSelected) quantityValue else 0f,
                                quantityUnit = if (isUnitSelected) quantityValue else 0f
                            )
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp),
                    enabled = quantity.toFloatOrNull()?.let { it > 0 } ?: false
                ) {
                    Text("Ajouter au panier")
                }
            }
            is ArticleState.Error -> {
                Text(
                    text = "Erreur : ${state.message}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}