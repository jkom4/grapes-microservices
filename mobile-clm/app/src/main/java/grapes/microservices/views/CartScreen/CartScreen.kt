package grapes.microservices.views.CartScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.ArticleRepository
import grapes.microservices.viewmodel.ArticleViewModel
import grapes.microservices.viewmodel.ArticleViewModelFactory
import grapes.microservices.viewmodel.PaymentState
import grapes.microservices.views.components.CartComponents.CartHeader
import grapes.microservices.views.components.CartComponents.CartItemsList
import grapes.microservices.views.components.CartComponents.CartSummary
import grapes.microservices.views.components.CartComponents.PaymentConfirmation
import grapes.microservices.views.components.CartComponents.PaymentForm
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController
) {
    // Get the Context to initialize dependencies and open URLs
    val context = LocalContext.current

    // Initialize dependencies
    val repository = remember { ArticleRepository(RetrofitClient.articleApiService) }
    val articleApiService = remember { RetrofitClient.articleApiService }
    val paymentApiService = remember { RetrofitClient.paymentApiService }

    // Create the factory for ViewModel
    val viewModelFactory = remember {
        ArticleViewModelFactory.createFactory(
            context = context,
            repository = repository,
            articleApiService = articleApiService,
            paymentApiService = paymentApiService
        )
    }

    // Initialize the ViewModel
    val viewModel: ArticleViewModel = viewModel(factory = viewModelFactory)
    val cartState = viewModel.cartScreenState.collectAsState()
    val paymentState = viewModel.paymentState.collectAsState()

    // Load cart on startup
    LaunchedEffect(Unit) {
        viewModel.fetchCart()
    }

    // Handle payment success
    LaunchedEffect(paymentState.value) {
        if (paymentState.value is PaymentState.Success) {
            delay(2000)
            navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
            viewModel.resetPaymentState()
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
                item { CartHeader() }
                item { CartItemsList(viewModel = viewModel, cartState = cartState.value) }
                item { CartSummary(cartState = cartState.value) }
                item { PaymentForm(viewModel = viewModel, cartState = cartState.value) }
            }
            PaymentConfirmation(paymentState = paymentState.value)
        }
    }
}