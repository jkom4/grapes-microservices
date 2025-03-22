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
    Log.d("EmailSentScreen", "Trip ID: $tripId, Trip Name: $tripName, Trip Address: $tripAddress")

    val shouldNavigate = viewModel.shouldNavigate.value
    val context = LocalContext.current

    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            val intent = Intent(context, MapsActivity::class.java).apply {
                putExtra("tripId", tripId)
                putExtra("tripName", tripName)
                putExtra("tripAddress", tripAddress)
            }
            context.startActivity(intent)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.handleNavigationAfterDelay()
    }

    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Success Icon",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Successful",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "An email has been sent to the customer",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
    )
}