package com.example.mobile_cll.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobile_cll.model.Order
import com.example.mobile_cll.model.Trip

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
    val totalQuantity = orders.filter { it.tripId == trip.id }.sumOf { it.quantity }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { navController.navigate("trip_details/${trip.id}") }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = trip.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Dist: ${trip.distance}", fontSize = 14.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = trip.address, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Qty: $totalQuantity",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAD7E)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF4CAD7E), shape = RoundedCornerShape(4.dp))
                    .padding(8.dp)
                    .align(Alignment.End)
            ) {
                Text(text = trip.id, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
