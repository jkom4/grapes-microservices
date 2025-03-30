package com.example.mobile_cll.view.components

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.gcacace.signaturepad.views.SignaturePad

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
                setMinWidth(3f)
                setMaxWidth(10f)
                setVelocityFilterWeight(0.9f)

                setOnSignedListener(object : SignaturePad.OnSignedListener {
                    override fun onStartSigning() {}
                    override fun onSigned() { onSignatureChange(signatureBitmap) }
                    override fun onClear() { onSignatureChange(null) }
                })

                signaturePadRef = this
            }
        }
    )

    LaunchedEffect(Unit) {
        signaturePadRef?.clear()
    }
}
