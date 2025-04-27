package grapes.microservices.views.Settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import grapes.microservices.R
import grapes.microservices.models.network.RetrofitClient
import grapes.microservices.viewmodels.DeliveryTrackingViewModel
import grapes.microservices.viewmodels.DeliveryTrackingViewModelFactory
import grapes.microservices.views.components.DeliveryTrackingComponents.DeliveryTrackingTopBar
import kotlinx.coroutines.launch

@Composable
fun DeliveryTrackingScreen(navController: NavController) {
    val vm: DeliveryTrackingViewModel = viewModel(
        factory = DeliveryTrackingViewModelFactory(RetrofitClient.orderApiService)
    )
    val deliveryStatus by vm.deliveryStatus.collectAsState()
    var orderIdInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            DeliveryTrackingTopBar(
                onBackClick = { navController.navigateUp() },
            )
        },
        modifier = Modifier.padding(16.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = orderIdInput,
                        onValueChange = {
                            orderIdInput = it
                            errorMessage = null
                        },
                        label = { Text(stringResource(R.string.delivery_tracking_order_id_hint)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true,
                        isError = errorMessage != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            errorMessage?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    )

                    Button(
                        onClick = {
                            val orderId = orderIdInput.toIntOrNull()
                            if (orderId != null) {
                                coroutineScope.launch {
                                    vm.fetchDeliveryStatus(orderId)
                                }
                            } else {
                                errorMessage = "Tracking number not valid"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.delivery_tracking_search),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            when (deliveryStatus) {
                is DeliveryTrackingViewModel.DeliveryStatusState.Idle -> {
                }
                is DeliveryTrackingViewModel.DeliveryStatusState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.delivery_tracking_loading),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                is DeliveryTrackingViewModel.DeliveryStatusState.Success -> {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.delivery_tracking_status,
                                (deliveryStatus as DeliveryTrackingViewModel.DeliveryStatusState.Success).status
                            ),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is DeliveryTrackingViewModel.DeliveryStatusState.Error -> {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.delivery_tracking_error,
                                (deliveryStatus as DeliveryTrackingViewModel.DeliveryStatusState.Error).message
                            ),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}