package com.example.mobile_cll.view

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobile_cll.viewmodel.EmailSentViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.ui.platform.LocalContext

/**
 * Composable screen that displays a success message after an email is sent.
 * This screen will navigate back to the home screen after a delay.
 */
@Composable
fun EmailSentScreen(
    navController: NavController,
    tripId: String,
    tripName: String,
    tripAddress: String,
    viewModel: EmailSentViewModel = viewModel()
) {

    val shouldNavigate = viewModel.shouldNavigate.value  // Observe navigation state
    val context = LocalContext.current


    // Scaffold to provide a standard screen layout.
    Scaffold(
        content = { paddingValues ->
            // Main content of the screen, centered vertically and horizontally
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Display success icon (check circle)
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Success Icon",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display success text
                Text(
                    text = "Successful",  // Main success message
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Text showing additional information
                Text(
                    text = "An email has been sent to the customer",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
    )
}
