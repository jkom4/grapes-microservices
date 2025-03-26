package com.example.mobile_cll.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * TopSectionScan is a Composable function that displays the top section of the screen,
 * including a back button and a title. It uses a Column and Row to position its contents.
 *
 * @param navController A NavController used to navigate back to the home screen.
 */
@Composable
fun TopSectionScan(navController: NavController?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4CAD7E))
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
                Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Scan",
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Bottom)
            )
        }
    }
}
