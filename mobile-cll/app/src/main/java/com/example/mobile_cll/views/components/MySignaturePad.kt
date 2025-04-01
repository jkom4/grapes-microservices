package com.example.mobile_cll.views.components

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.gcacace.signaturepad.views.SignaturePad

/**
 * A Composable component for capturing user signatures.
 *
 * @param modifier The modifier for customizing the layout.
 * @param onSignatureChange Callback invoked when the signature changes.
 */
@Composable
fun SignaturePadComponent(
    modifier: Modifier = Modifier,
    onSignatureChange: (Bitmap?) -> Unit
) {
    var signaturePadRef by remember { mutableStateOf<SignaturePad?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            SignaturePad(ctx, null).apply {
                setMinWidth(3f) // Minimum stroke width
                setMaxWidth(10f) // Maximum stroke width
                setVelocityFilterWeight(0.9f) // Smoothens the stroke based on velocity

                setOnSignedListener(object : SignaturePad.OnSignedListener {
                    override fun onStartSigning() {}
                    override fun onSigned() { onSignatureChange(signatureBitmap) }
                    override fun onClear() { onSignatureChange(null) }
                })

                signaturePadRef = this
            }
        }
    )

    // Clears the signature pad when the component is first composed
    LaunchedEffect(Unit) {
        signaturePadRef?.clear()
    }
}
