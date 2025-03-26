package com.example.mobile_cll.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable displaying a top section with:
 * - A greeting message to the user
 * - The number of trips dynamically updated based on the `tripCount`
 *
 * @param tripCount The number of trips to display in the message. It determines whether the message uses "trip" or "trips" based on the count.
 */
@Composable
fun TopSection(tripCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4CAD7E))
            .height(130.dp)
            .padding(horizontal = 35.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(35.dp))
        Text("Hello Mathys", fontSize = 18.sp, color = Color.White)
        Text(
            "$tripCount ${if (tripCount == 1) "trip" else "trips"} to do",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
