package grapes.microservices.views.OrderHistory

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.models.repository.OrderRepository
import grapes.microservices.viewmodels.OrderHistoryViewModel
import grapes.microservices.viewmodels.OrderHistoryViewModelFactory
import grapes.microservices.views.components.OrderComponents.FilterDialog
import grapes.microservices.views.components.OrderComponents.OrderHistoryContent
import grapes.microservices.views.components.OrderComponents.OrderHistoryTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(navController: NavController) {
    val repository = OrderRepository(RetrofitClient.orderApiService)
    val viewModel: OrderHistoryViewModel = viewModel(factory = OrderHistoryViewModelFactory(repository))

    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val hasMorePages by viewModel.hasMorePages.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            OrderHistoryTopBar(
                onBackClick = { navController.navigateUp() },
                onFilterClick = { showFilterDialog = true }
            )
        },
        modifier = Modifier.padding(16.dp)
    ) { paddingValues ->
        OrderHistoryContent(
            orders = orders,
            isLoading = isLoading,
            error = error,
            currentPage = currentPage,
            hasMorePages = hasMorePages,
            onPreviousPage = { viewModel.previousPage() },
            onNextPage = { viewModel.nextPage() },
            paddingValues = paddingValues
        )

        if (showFilterDialog) {
            FilterDialog(
                onDismiss = { showFilterDialog = false },
                onApply = { code, date ->
                    viewModel.applyFilters(code, date)
                    showFilterDialog = false
                }
            )
        }
    }
}