package com.example.mobile_cll.views.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobile_cll.models.entities.Order
import com.example.mobile_cll.models.entities.Trip

/**
 * Composable displaying a trip card with:
 * - Trip details
 * - Total quantity of items in the trip
 * - Navigation to the trip details screen
 *
 * @param trip The trip object containing details like name, address, and distance.
 * @param orders The list of orders associated with trips, used to calculate the total quantity.
 * @param navController The navigation controller to navigate to the trip details screen.
 */
@Composable
fun TripCard(trip: Trip, orders: List<Order>, navController: NavController) {
    val orderSize = orders.size

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                val encodedName = Uri.encode(trip.name)
                val encodedDistance = Uri.encode(trip.distance)
                val encodedAddress = Uri.encode(trip.address)

                navController.navigate("trip_details/${trip.id}/$encodedName/$encodedDistance/$encodedAddress")
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = trip.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Dist: ${trip.distance}", fontSize = 14.sp, color = MaterialTheme.colorScheme.tertiary)
            }
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = trip.address, fontSize = 14.sp, color = MaterialTheme.colorScheme.tertiary)
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Qty: $orderSize",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp))
                    .padding(8.dp)
                    .align(Alignment.End)
            ) {
                Text(text = trip.id, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

