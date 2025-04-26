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
                val orderId = tripId.toIntOrNull() ?: throw IllegalArgumentException("Invalid tripId: $tripId")

                val signatureBase64 = if (_isDoorstepDelivery.value) {
                    _signatureBitmap.value?.let { bitmap ->
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream)
                        val bytes = stream.toByteArray()
                        Base64.encodeToString(bytes, Base64.NO_WRAP)
                    }
                } else {
                    null
                }

                // 1. PATCH /cll/deliveries/feedback/{orderId}
                val feedback = RetrofitClient.DeliveryFeedback(
                    orderId = orderId,
                    comment = _commentState.value,
                    doorstep = _isDoorstepDelivery.value,
                    signature = signatureBase64,
                    deliveryStatusId = 2
                )
                try {
                    apiService.updateDeliveryFeedback(orderId, feedback)
                } catch (e: Exception) {
                    throw e
                }

                // 2. PATCH /cll/deliveries/update-status/{orderId}
                try {
                    apiService.updateDeliveryStatus(orderId.toString(), "Delivered")
                } catch (e: Exception) {
                    throw e
                }

                // 3. PATCH /cll/trips/{tripId}/finish
                try {
                    apiService.finishTrip(tripId)
                } catch (e: Exception) {
                    throw e
                }

                _saveStatus.value = "Success"
                _commentState.value = ""
                _imageUris.value = emptyList()
                _isDoorstepDelivery.value = false
                _signatureBitmap.value = null
                _showSignatureDialog.value = false

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
                (context as? Activity)?.finish()

            } catch (e: Exception) {
                _saveStatus.value = "Erreur: ${e.message}"
            }
        }
    }
}