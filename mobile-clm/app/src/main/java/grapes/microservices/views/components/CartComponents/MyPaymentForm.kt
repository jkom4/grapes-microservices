package grapes.microservices.views.components.CartComponents

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import grapes.microservices.R
import grapes.microservices.viewmodel.ArticleViewModel
import grapes.microservices.viewmodel.CartScreenState
import grapes.microservices.viewmodel.PaymentState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentForm(
    viewModel: ArticleViewModel,
    cartState: CartScreenState
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val errorRequiredFieldsMessage = stringResource(R.string.error_required_fields)
    val paymentState = viewModel.paymentState.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.payment_info),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            val fields = listOf(
                Triple(R.string.field_name, fullName, { newValue: String -> fullName = newValue }),
                Triple(R.string.field_email, email, { newValue: String -> email = newValue }),
                Triple(R.string.field_phone, phoneNumber, { newValue: String -> phoneNumber = newValue }),
                Triple(R.string.field_address, address, { newValue: String -> address = newValue }),
            )

            fields.forEach { (labelResId, value, onChange) ->
                OutlinedTextField(
                    value = value,
                    onValueChange = onChange,
                    label = { Text(stringResource(labelResId), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    keyboardOptions = when (labelResId) {
                        R.string.field_phone -> KeyboardOptions(keyboardType = KeyboardType.Number)
                        R.string.field_email -> KeyboardOptions(keyboardType = KeyboardType.Email)
                        else -> KeyboardOptions.Default
                    },
                    singleLine = true
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { checked -> termsAccepted = checked },
                    colors = CheckboxDefaults.colors(
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
                Text(
                    text = stringResource(R.string.terms_conditions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Form validation for buttons
            val formIsValid = fullName.isNotBlank() && phoneNumber.isNotBlank() && address.isNotBlank()
            val isCartNotEmpty = cartState is CartScreenState.Success && cartState.cart.items.isNotEmpty()

            // Pay Now Button
            Button(
                onClick = {
                    if (fullName.isBlank() || phoneNumber.isBlank() || address.isBlank()) {
                        errorMessage = errorRequiredFieldsMessage
                    } else if (cartState is CartScreenState.Success && cartState.cart.items.isNotEmpty()) {
                        errorMessage = ""
                        // Appel direct de l'URL en dur
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("http://79.76.108.164:82/login"))
                        )
                    } else {
                        errorMessage = "Cart is empty or not loaded"
                    }
                },
                enabled = termsAccepted && isCartNotEmpty && formIsValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = stringResource(R.string.pay_button),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Stripe Button
            Button(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://buy.stripe.com/test_fZe3cr0INeIK50c6oo"))
                    )
                },
                enabled = termsAccepted && isCartNotEmpty && formIsValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = stringResource(R.string.pay_stripe),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            when (val payment = paymentState.value) {
                is PaymentState.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is PaymentState.Error -> {
                    Text(
                        text = "${stringResource(R.string.error_prefix)}: ${payment.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
                else -> {
                    // Idle or Success: no message
                }
            }
        }
    }
}