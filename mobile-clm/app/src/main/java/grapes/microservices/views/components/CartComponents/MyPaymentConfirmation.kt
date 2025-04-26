package grapes.microservices.views.components.CartComponents

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import grapes.microservices.R
import grapes.microservices.viewmodel.PaymentState

@Composable
fun PaymentConfirmation(
    paymentState: PaymentState
) {
    val showPaymentConfirmation = paymentState is PaymentState.Success
    val animatedScale by animateFloatAsState(
        targetValue = if (showPaymentConfirmation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (showPaymentConfirmation) 1f else 0f,
        animationSpec = tween(durationMillis = 600)
    )

    if (showPaymentConfirmation) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .size(250.dp)
                    .scale(animatedScale)
                    .alpha(animatedAlpha),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.payment_success),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.payment_success),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.payment_thanks),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}