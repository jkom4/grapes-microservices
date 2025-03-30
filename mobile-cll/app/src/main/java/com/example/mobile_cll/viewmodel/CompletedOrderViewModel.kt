package com.example.mobile_cll.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CompletedOrderViewModel : ViewModel() {

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

    fun clearSignature() {
        _signatureBitmap.value = null
    }

    fun dismissSignatureDialog() {
        _showSignatureDialog.value = false
    }

}
