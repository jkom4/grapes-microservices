package com.example.mobile_cll.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
fun TopSection(
    tripCount: Int,
    isFinished: Boolean = false,
) {
    val context = LocalContext.current


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .height(160.dp)
            .padding(horizontal = 35.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(35.dp))

        Text(
            text = "Hello Mathys",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )

        Text(
            text = "$tripCount trips ${if (isFinished) "done" else "to do"}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
