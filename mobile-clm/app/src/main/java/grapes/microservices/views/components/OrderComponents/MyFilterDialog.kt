package grapes.microservices.views.components.OrderComponents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import grapes.microservices.R

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApply: (String?, String?) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.filter_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it
                        codeError = it.isNotEmpty() && it.toIntOrNull() == null
                    },
                    label = { Text(stringResource(R.string.filter_code)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = codeError,
                    supportingText = {
                        if (codeError) {
                            Text("Please enter a valid number")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                        dateError = it.isNotEmpty() && !it.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))
                    },
                    label = { Text(stringResource(R.string.filter_date)) },
                    placeholder = { Text("yyyy-MM-dd") },
                    isError = dateError,
                    supportingText = {
                        if (dateError) {
                            Text("Please enter a valid date (yyyy-MM-dd)")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel_button))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!codeError && !dateError) {
                                onApply(
                                    code.takeIf { it.isNotBlank() },
                                    date.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = !codeError && !dateError
                    ) {
                        Text(stringResource(R.string.apply_button))
                    }
                }
            }
        }
    }
}