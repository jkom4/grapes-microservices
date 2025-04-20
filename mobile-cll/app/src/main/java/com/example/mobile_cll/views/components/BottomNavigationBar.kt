package com.example.mobile_cll.views.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * Composable displaying a bottom navigation bar with:
 * - A home icon that navigates to the home screen
 * - A map icon that navigates to the map screen
 * - An account icon that navigates to the account screen
 *
 * @param navController The navigation controller that allows navigation between screens.
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    BottomAppBar(containerColor = MaterialTheme.colorScheme.primary) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigate("home") }) {
                Icon(Icons.Filled.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = { navController.navigate("map") }) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Map", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = { navController.navigate("account") }) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Account", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

