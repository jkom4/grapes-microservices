package com.example.mobile_cll.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobile_cll.viewmodel.EmailSentViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle

/**
 * Composable screen that displays a success message after an email is sent.
 * This screen will navigate back to the home screen after a delay.
 */
@Composable
fun EmailSentScreen(navController: NavController, viewModel: EmailSentViewModel = viewModel()) {

    // Launches an effect to handle navigation after a delay.
    LaunchedEffect(Unit) {
        viewModel.handleNavigationAfterDelay {
            navController.navigate("home") // Navigate back to the home screen after 5 seconds.
        }
    }

    // Scaffold to provide a standard screen layout.
    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Success icon indicating the email was sent successfully
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Success Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp)) // Space between icon and text

                // Text showing the success message
                Text(
                    text = "Successful",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )

                Spacer(modifier = Modifier.height(8.dp)) // Space between lines of text

                // Text showing additional information
                Text(
                    text = "An email has been sent to the customer",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    )
}
