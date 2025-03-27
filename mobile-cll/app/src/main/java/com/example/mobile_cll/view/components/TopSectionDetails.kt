package com.example.mobile_cll.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Composable displaying a top section with:
 * - A back button that navigates to the home screen
 * - A title indicating the order details screen
 *
 * @param navController The navigation controller used to navigate back to the home screen.
 */
@Composable
fun TopSectionDetails(navController: NavController?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(
                onClick = { navController?.navigate("home") },
                modifier = Modifier.align(Alignment.Bottom)
            ) {
                Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Order Details",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
    }
}
