package com.example.mobile_cll.views.components.various

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.mobile_cll.MapsActivity

@Composable
fun BottomNavigationBar(navController: NavController) {
    val context = LocalContext.current

    BottomAppBar(containerColor = MaterialTheme.colorScheme.primary) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                navController.graph.forEach { destination ->
                }

                try {
                    // Force navigate to "home" and clear back stack
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                } catch (e: IllegalArgumentException) {
                    // Fallback: Pop back stack or navigate to start destination
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        // Navigate to the graph's start destination as a fallback
                        navController.graph.startDestinationRoute?.let { startRoute ->
                            navController.navigate(startRoute) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            }) {
                Icon(Icons.Filled.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = {
                // Navigate to MapActivity using an Intent
                val intent = Intent(context, MapsActivity::class.java)
                context.startActivity(intent)
            }) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Map", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}