package grapes.microservices.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import grapes.microservices.models.network.OrderApiService

class DeliveryTrackingViewModelFactory(
    private val orderApiService: OrderApiService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryTrackingViewModel::class.java)) {
            return DeliveryTrackingViewModel(orderApiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}