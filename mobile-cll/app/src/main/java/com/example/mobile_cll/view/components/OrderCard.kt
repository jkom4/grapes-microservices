package com.example.mobile_cll.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile_cll.model.Order
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner

/**
 * Composable displaying an order card with:
 * - ID of the order
 * - Product description
 * - Quantity of the product
 * - A button that triggers the scanning action
 *
 * @param order The order object that contains the order's details such as ID, product description, and quantity.
 * @param onScanClick A lambda function that is triggered when the scan button is clicked, passing the order ID as a parameter.
 */

@Composable
fun OrderCard(order: Order, onScanClick: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .heightIn(min = 100.dp, max = 120.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order ID: ${order.id}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = { onScanClick(order.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = "Scan",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Scan",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text("Product: ${order.productDescription}", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary )
            Text("Quantity: ${order.quantity}", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}
