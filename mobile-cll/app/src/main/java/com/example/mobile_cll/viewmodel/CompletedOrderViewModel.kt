package com.example.mobile_cll.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_cll.model.DatabaseHelper
import com.example.mobile_cll.model.DatabaseOperations
import com.example.mobile_cll.model.entities.Delivery
import com.example.mobile_cll.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel responsible for handling completed orders.
 * Manages comments, images, doorstep deliveries, and signatures.
 */
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

    /**
     * Updates the comment state.
     * @param comment The comment to set.
     */
    fun updateComment(comment: String) {
        _commentState.value = comment
    }

    /**
     * Adds an image URI to the list.
     * @param uri The image URI.
     */
    fun addImage(uri: Uri) {
        _imageUris.value = _imageUris.value + uri
    }

    /**
     * Toggles doorstep delivery and updates the signature dialog visibility.
     * @param isChecked True if doorstep delivery is enabled.
     */
    fun toggleDoorstepDelivery(isChecked: Boolean) {
        _isDoorstepDelivery.value = isChecked
        _showSignatureDialog.value = isChecked
    }

    /**
     * Updates the signature bitmap.
     * @param bitmap The new signature bitmap.
     */
    fun updateSignature(bitmap: Bitmap?) {
        _signatureBitmap.value = bitmap
    }

    /**
     * Shows the signature dialog.
     */
    fun showSignatureDialog() {
        _showSignatureDialog.value = true
    }

    /**
     * Dismisses the signature dialog.
     */
    fun dismissSignatureDialog() {
        _showSignatureDialog.value = false
    }

    /**
     * Clears the saved signature bitmap.
     */
    fun clearSignature() {
        _signatureBitmap.value = null
    }

    private val dbOperations = DatabaseOperations(DatabaseHelper(context))

    /**
     * Saves delivery data for a given trip.
     * @param tripId The trip identifier.
     * @param deliveryStatusId The delivery status ID.
     */
    fun saveDeliveryForTrip(
        tripId: String,
        deliveryStatusId: Int
    ) {
        viewModelScope.launch {
            try {
                val userId = dbOperations.getDeliveryDriver()?.id ?: throw IllegalStateException("No driver found")
                val orders = dbOperations.getOrdersForTrip(tripId)
                if (orders.isEmpty()) {
                    Log.e("CompletedOrderViewModel", "No orders found for tripId: $tripId")
                    _saveStatus.value = "Error: No orders found"
                    return@launch
                }

                val photoByteArray = _imageUris.value.firstOrNull()?.let { uri ->
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }

                val signatureByteArray = _signatureBitmap.value?.let { bitmap ->
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.toByteArray()
                }

                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formattedDateTime = formatter.format(Date())

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

                    Log.d("CompletedOrderViewModel", "Saving data for orderId: ${order.id}")
                    val rowId = dbOperations.insertDelivery(delivery)
                    if (rowId == -1L) {
                        Log.e("CompletedOrderViewModel", "Failed to insert for orderId: ${order.id}")
                        allSuccess = false
                    }
                }

                if (allSuccess) {
                    tripRepository.updateTripFinished(tripId, true)
                    val updatedTrip = tripRepository.getTrip(tripId)
                    Log.d("CompletedOrderViewModel", "Trip updated: $updatedTrip")
                    _saveStatus.value = "Success"
                } else {
                    _saveStatus.value = "Error: Save failed"
                }

                // Reset state after saving
                _commentState.value = ""
                _imageUris.value = emptyList()
                _isDoorstepDelivery.value = false
                _signatureBitmap.value = null

            } catch (e: Exception) {
                Log.e("CompletedOrderViewModel", "Error saving: ${e.message}")
                _saveStatus.value = "Error: ${e.message}"
            }
        }
    }
}
