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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.navigation.NavController
import com.example.mobile_cll.viewmodel.EmailSentViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.example.mobile_cll.MapsActivity

@Composable
fun EmailSentScreen(
    navController: NavController,
    tripId: String,
    tripName: String,
    tripAddress: String,
    viewModel: EmailSentViewModel = viewModel()
) {
    // Log the details of the trip received for debugging
    Log.d("EmailSentScreen", "Trip ID: $tripId, Trip Name: $tripName, Trip Address: $tripAddress")

    val shouldNavigate = viewModel.shouldNavigate.value  // Observe navigation state
    val context = LocalContext.current

    // LaunchedEffect to navigate to the MapsActivity after a delay if navigation flag is true
    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            // Create an Intent to start MapsActivity, passing necessary trip information
            val intent = Intent(context, MapsActivity::class.java).apply {
                putExtra("tripId", tripId)
                putExtra("tripName", tripName)
                putExtra("tripAddress", tripAddress)
                putExtra("cameFromEmail", true)
            }
            // Start the MapsActivity with the intent
            context.startActivity(intent)
        }
    }

    // LaunchedEffect to trigger the navigation after a delay in the view model
    LaunchedEffect(Unit) {
        viewModel.handleNavigationAfterDelay()
    }

    // Scaffold used to structure the layout
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

                // Display secondary message indicating email sent to the customer
                Text(
                    text = "An email has been sent to the customer",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
    )
}
