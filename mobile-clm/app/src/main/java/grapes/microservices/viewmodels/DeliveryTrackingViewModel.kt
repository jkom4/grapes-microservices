package grapes.microservices.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import grapes.microservices.models.network.OrderApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "DeliveryTrackingViewModel"

class DeliveryTrackingViewModel(
    private val orderApiService: OrderApiService
) : ViewModel() {

    sealed class DeliveryStatusState {
        object Idle : DeliveryStatusState()
        object Loading : DeliveryStatusState()
        data class Success(val status: String) : DeliveryStatusState()
        data class Error(val message: String) : DeliveryStatusState()
    }

    private val _deliveryStatus = MutableStateFlow<DeliveryStatusState>(DeliveryStatusState.Idle)
    val deliveryStatus: StateFlow<DeliveryStatusState> = _deliveryStatus.asStateFlow()

    fun fetchDeliveryStatus(orderId: Int) {
        viewModelScope.launch {
            _deliveryStatus.value = DeliveryStatusState.Loading
            try {
                val status = orderApiService.getDeliveryStatus(orderId)
                _deliveryStatus.value = DeliveryStatusState.Success(status)
                Log.d(TAG, "Statut de livraison récupéré : $status")
            } catch (e: Exception) {
                _deliveryStatus.value = DeliveryStatusState.Error(e.message ?: "Échec de la récupération du statut de livraison")
                Log.e(TAG, "Erreur lors de la récupération du statut : ${e.message}")
            }
        }
    }
}