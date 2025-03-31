package com.example.mobile_cll.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_cll.model.DatabaseHelper
import com.example.mobile_cll.model.entities.Delivery
import com.example.mobile_cll.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompletedOrderViewModel(
    private val context: Context,
    private val tripRepository: TripRepository
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

    private val databaseHelper = DatabaseHelper(context)

    fun updateComment(comment: String) {
        _commentState.value = comment
    }

    fun addImage(uri: Uri) {
        _imageUris.value = _imageUris.value + uri
    }

    fun toggleDoorstepDelivery(isChecked: Boolean) {
        _isDoorstepDelivery.value = isChecked
        // Ouvrir le dialog si coché, le fermer si décoché
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

    fun saveDeliveryForTrip(
        tripId: String,
        deliveryStatusId: Int
    ) {
        viewModelScope.launch {
            try {
                val userId = databaseHelper.getDriver()?.id ?: throw IllegalStateException("No driver found")
                val orders = databaseHelper.getOrdersForTrip(tripId)
                if (orders.isEmpty()) {
                    Log.e("CompletedOrderViewModel", "Aucune commande trouvée pour tripId: $tripId")
                    _saveStatus.value = "Error: Aucune commande trouvée"
                    return@launch
                }

                val photoByteArray = _imageUris.value.firstOrNull()?.let { uri ->
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.readBytes()
                    }
                }

                val signatureByteArray = _signatureBitmap.value?.let { bitmap ->
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.toByteArray()
                }

                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val currentDateTime = Date()
                val formattedDateTime = formatter.format(currentDateTime)

                var allSuccess = true
                orders.forEach { order ->
                    val delivery = Delivery(
                        orderId = order.id,
                        userId = userId,
                        deliveryStatusId = deliveryStatusId,
                        deliveryDate = formattedDateTime,
                        deliveredAt = formattedDateTime,
                        comment = _commentState.value,
                        doorstep = _isDoorstepDelivery.value,
                        signature = signatureByteArray,
                        photo = photoByteArray
                    )

                    Log.d("CompletedOrderViewModel", "Données à insérer pour orderId: ${order.id}")
                    Log.d("CompletedOrderViewModel", "orderId: ${delivery.orderId}")
                    Log.d("CompletedOrderViewModel", "userId: ${delivery.userId}")
                    Log.d("CompletedOrderViewModel", "deliveryStatusId: ${delivery.deliveryStatusId}")
                    Log.d("CompletedOrderViewModel", "deliveryDate: ${delivery.deliveryDate}")
                    Log.d("CompletedOrderViewModel", "deliveredAt: ${delivery.deliveredAt}")
                    Log.d("CompletedOrderViewModel", "comment: ${delivery.comment}")
                    Log.d("CompletedOrderViewModel", "doorstep: ${delivery.doorstep}")
                    Log.d("CompletedOrderViewModel", "signature: ${delivery.signature?.size ?: "null"} bytes")
                    Log.d("CompletedOrderViewModel", "photo: ${delivery.photo?.size ?: "null"} bytes")

                    val rowId = databaseHelper.insertDelivery(delivery)
                    if (rowId == -1L) {
                        Log.e("CompletedOrderViewModel", "Échec de l'insertion pour orderId: ${order.id}")
                        allSuccess = false
                    }
                }

                if (allSuccess) {
                    tripRepository.updateTripFinished(tripId, true)
                    val updatedTrip = tripRepository.getTrip(tripId)
                    Log.d("CompletedOrderViewModel", "Trip après mise à jour : $updatedTrip")
                    _saveStatus.value = "Success"
                } else {
                    _saveStatus.value = "Error: Échec de l'enregistrement"
                }

                _commentState.value = ""
                _imageUris.value = emptyList()
                _isDoorstepDelivery.value = false
                _signatureBitmap.value = null

            } catch (e: Exception) {
                Log.e("CompletedOrderViewModel", "Erreur lors de l'enregistrement: ${e.message}")
                _saveStatus.value = "Error: ${e.message}"
            }
        }
    }
}