package com.example.mobile_cll.viewmodels.completedOrder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_cll.MainActivity
import com.example.mobile_cll.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CompletedOrderViewModel(
    private val context: Context
) : ViewModel() {

    private val _commentState = MutableStateFlow("")
    val commentState: StateFlow<String> = _commentState

    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris: StateFlow<List<Uri>> = _imageUris

    private val _isDoorstepDelivery = MutableStateFlow(false)
    val isDoorstepDelivery: StateFlow<Boolean> = _isDoorstepDelivery

    private val _signatureBitmap = MutableStateFlow<Bitmap?>(null)
    val signatureBitmap: StateFlow<Bitmap?> = _signatureBitmap

    private val _showSignatureDialog = MutableStateFlow(false)
    val showSignatureDialog: StateFlow<Boolean> = _showSignatureDialog

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus: StateFlow<String?> = _saveStatus

    private val apiService = RetrofitClient.getService(RetrofitClient.ApiService::class.java)

    fun updateComment(comment: String) {
        _commentState.value = comment
    }

    fun addImage(uri: Uri) {
        _imageUris.value = _imageUris.value + uri
    }

    fun toggleDoorstepDelivery(isChecked: Boolean) {
        _isDoorstepDelivery.value = isChecked
        _showSignatureDialog.value = isChecked
    }

    fun updateSignature(bitmap: Bitmap?) {
        _signatureBitmap.value = bitmap
    }

    fun showSignatureDialog() {
        _showSignatureDialog.value = true
    }

    fun dismissSignatureDialog() {
        _showSignatureDialog.value = false
    }

    fun clearSignature() {
        _signatureBitmap.value = null
    }

    fun resetSaveStatus() {
        _saveStatus.value = null
    }

    fun confirmDelivery(tripId: String) {
        viewModelScope.launch {
            try {
                Log.d("CompletedOrderViewModel", "Début de confirmDelivery pour tripId: $tripId")
                val orderId = tripId.toIntOrNull() ?: throw IllegalArgumentException("Invalid tripId: $tripId")

                // Préparer la signature
                val signatureBase64 = if (_isDoorstepDelivery.value) {
                    _signatureBitmap.value?.let { bitmap ->
                        Log.d("CompletedOrderViewModel", "Conversion de la signature en Base64")
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream)
                        val bytes = stream.toByteArray()
                        Base64.encodeToString(bytes, Base64.NO_WRAP) // Encoder en Base64
                    }
                } else {
                    null // Pas de signature si doorstep est false
                }

                // Log pour afficher la structure de la signature
                Log.d("CompletedOrderViewModel", "Signature Base64: $signatureBase64")

                // 1. PATCH /cll/deliveries/feedback/{orderId}
                val feedback = RetrofitClient.DeliveryFeedback(
                    orderId = orderId,
                    comment = _commentState.value,
                    doorstep = _isDoorstepDelivery.value,
                    signature = signatureBase64,
                    deliveryStatusId = 2
                )
                Log.d("CompletedOrderViewModel", "Mise à jour du feedback: $feedback")
                try {
                    apiService.updateDeliveryFeedback(orderId, feedback)
                    Log.d("CompletedOrderViewModel", "Feedback mis à jour avec succès pour orderId: $orderId")
                } catch (e: Exception) {
                    Log.e("CompletedOrderViewModel", "Erreur lors de la mise à jour du feedback: ${e.message}", e)
                    throw e
                }

                // 2. PATCH /cll/deliveries/update-status/{orderId}
                Log.d("CompletedOrderViewModel", "Mise à jour du statut à Delivered pour orderId: $orderId")
                try {
                    apiService.updateDeliveryStatus(orderId.toString(), "Delivered")
                    Log.d("CompletedOrderViewModel", "Statut mis à jour à Delivered avec succès")
                } catch (e: Exception) {
                    Log.e("CompletedOrderViewModel", "Erreur lors de la mise à jour du statut: ${e.message}", e)
                    throw e
                }

                // 3. PATCH /cll/trips/{tripId}/finish
                Log.d("CompletedOrderViewModel", "Finalisation du trip: $tripId")
                try {
                    apiService.finishTrip(tripId)
                    Log.d("CompletedOrderViewModel", "Trip terminé avec succès: $tripId")
                } catch (e: Exception) {
                    Log.e("CompletedOrderViewModel", "Erreur lors de la finalisation du trip: ${e.message}", e)
                    throw e
                }

                // Réinitialiser l'état
                _saveStatus.value = "Success"
                Log.d("CompletedOrderViewModel", "saveStatus défini à Success")
                _commentState.value = ""
                _imageUris.value = emptyList()
                _isDoorstepDelivery.value = false
                _signatureBitmap.value = null
                _showSignatureDialog.value = false

                Log.d("CompletedOrderViewModel", "Navigation vers MainActivity")
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
                (context as? Activity)?.finish()

            } catch (e: Exception) {
                Log.e("CompletedOrderViewModel", "Erreur lors de la confirmation de la livraison: ${e.message}", e)
                _saveStatus.value = "Erreur: ${e.message}"
            }
        }
    }
}