package com.example.mobile_cll.view

import android.content.Intent
import android.os.Handler
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
import android.app.Activity
import androidx.navigation.NavController
import com.example.mobile_cll.MainActivity

/**
 * Composable function representing the last screen of the application.
 * This screen displays a success message and automatically navigates back to MainActivity after 3 seconds.
 *
 * @param navController The navigation controller used for navigation within the app.
 * @param activity The activity instance used to start a new intent.
 */
@Composable
fun LastScreen(
    navController: NavController,
    activity: Activity // Pass the Activity to start the new Intent
) {
    // Start a side effect to navigate after 3 seconds
    LaunchedEffect(Unit) {
        // Wait for 3 seconds
        kotlinx.coroutines.delay(3000)
        // Create an Intent to go back to MainActivity
        val intent = Intent(activity, MainActivity::class.java)
        activity.startActivity(intent)
        activity.finish() // Optionally, finish the current activity (OrderActivity)
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

                Text(
                    text = "The order has been successfully delivered",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
    )
}
