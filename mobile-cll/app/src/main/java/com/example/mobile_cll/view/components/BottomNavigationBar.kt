package com.example.mobile_cll.view.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.mobile_cll.MapsActivity

@Composable
fun BottomNavigationBar(navController: NavController?, context: Context) {
    BottomAppBar(containerColor = Color(0xFF4CAD7E)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController?.navigate("home") }) {
                Icon(Icons.Filled.Home, contentDescription = "Home", tint = Color.White)
            }
            IconButton(onClick = {
                // Use Intent to navigate to MapsActivity
                val intent = Intent(context, MapsActivity::class.java)
                context.startActivity(intent)
            }) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Map", tint = Color.White)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Account", tint = Color.White)
            }
        }
    }
}
