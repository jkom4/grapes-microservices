package com.example.mobile_cll.view.components

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

@Composable
fun BottomNavigationBar(navController: NavController?) {
    BottomAppBar(containerColor = Color(0xFF4CAD7E)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController?.navigate("home") }) {
                Icon(Icons.Filled.Home, contentDescription = "Home", tint = Color.White)
            }
            IconButton(onClick = { navController?.navigate("map") }) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Map", tint = Color.White)
            }
            IconButton(onClick = { /* Action Language */ }) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Account", tint = Color.White)
            }
        }
    }
}

