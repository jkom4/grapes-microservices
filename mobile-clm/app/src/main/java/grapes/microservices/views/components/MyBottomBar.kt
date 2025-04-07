package grapes.microservices.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import grapes.microservices.Screen

@Composable
fun MyBottomBar(
    modifier: Modifier,
    navController: NavHostController,
) {
    Row(
        modifier = modifier
            .background(color= MaterialTheme.colorScheme.primaryContainer)
            .padding(20.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val currentNav = navController.currentDestination?.route
        // home
        MyIcon(Icons.Default.Home, "Shopping cart",
            modifier = Modifier.padding(end = 8.dp),
            onClick = {navController.navigate(Screen.Home.route)},
            isSelected = (currentNav == Screen.Home.route))

        // cart
        MyIcon(Icons.Default.ShoppingCart, "Shopping cart",
            modifier = Modifier.padding(end = 8.dp),
            onClick = {navController.navigate(Screen.Cart.route)},
            isSelected = (currentNav == Screen.Cart.route))

        // profile
        MyIcon(Icons.Default.Person, "Shopping cart",
            modifier = Modifier.padding(end = 8.dp),
            onClick = {navController.navigate(Screen.Profile.route)},
            isSelected = (currentNav == Screen.Profile.route))

        // settings
        MyIcon(Icons.Default.Settings, "Notifications",
            modifier = Modifier.padding(end = 8.dp),
            onClick = {navController.navigate(Screen.Settings.route)},
            isSelected = (currentNav == Screen.Settings.route))
    }
}

@Composable
private fun MyIcon(imageVector: ImageVector, description : String,
                   modifier: Modifier = Modifier, onClick : () -> Unit = {},
                   isSelected: Boolean) {
    IconButton(onClick = onClick,
        modifier = modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .size(35.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.secondary
                else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = description,
            tint = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.onBackground,

        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMyBottomBar() {
    MyBottomBar(
        modifier = Modifier
            .fillMaxWidth(),
        navController = rememberNavController(),
    )
}