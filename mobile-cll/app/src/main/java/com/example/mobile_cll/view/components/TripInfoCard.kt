package com.example.mobile_cll.view.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobile_cll.model.Trip

/**
 * Composable displaying a trip info card with :
 * - Trip details
 * - A call button for the customer
 *
 * @param navController The navigation controller to manage navigation between screens.
 * @param tripId The unique identifier of the trip.
 * @param customerName The name of the customer associated with the trip.
 * @param address The address of the trip's destination.
 * @param orderId The unique identifier of the order associated with the trip.
 * @param trip The trip object containing details like name and distance.
 */
@Composable
fun TripInfoCard(navController: NavController?, tripId: String, customerName: String, address: String, orderId: String, trip: Trip) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .heightIn(min = 150.dp, max = 200.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = trip.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Dist: ${trip.distance}", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(text = trip.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(4.dp))

            }

            Box(
                modifier = Modifier
                    .padding(5.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            // Initiating a phone call using Intent.ACTION_DIAL
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:0648953161")
                            }
                            // Use the context to start the intent
                            context.startActivity(intent)
                                  },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Call",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Call customer",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
