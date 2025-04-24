package com.example.mobile_cll.views.components.various

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
 * A composable component representing the top section of a screen with a title and a back button.
 *
 * @param navController The navigation controller used to navigate back.
 * @param title The title displayed in the top section.
 */
@Composable
fun TopSection(
    navController: NavController?,
    title: String
) {
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
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .offset(y = 20.dp)
            ) {
                Icon(
                    Icons.Filled.ArrowBackIosNew,
                    contentDescription = "Back", // Accessibility description
                    tint = MaterialTheme.colorScheme.onPrimary // White icon color
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .offset(y = 10.dp)
            )
        }
    }
}
