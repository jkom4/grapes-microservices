package grapes.microservices.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Data class to represent each item in the Bottom Navigation
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun BottomBar(
    navController: NavController,
    currentRoute: String? // The current route to determine which item is selected
) {
    // List of items to display in the bottom navigation bar
    val items = listOf(
        BottomNavItem("home", Icons.Default.Home, "Home"),
        BottomNavItem("cart", Icons.Default.ShoppingCart, "Cart"),
        BottomNavItem("catalog", Icons.Default.Apps, "Catalog"),
        BottomNavItem("profile", Icons.Default.Person, "Profile")
    )

    // Card container for the bottom navigation bar
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp), // Setting the height of the bottom bar
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), // Rounded corners
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Shadow elevation
        colors = CardDefaults.cardColors(containerColor = Color.White) // Background color of the card
    ) {
        Column {
            // This row sets up the background for each item in the bottom bar (indicating selection)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.dp) // Invisible row, used to apply background
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    // Background color change based on selection
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (isSelected) Color(0xFFFFA8B2) else Color.White)
                    )
                }
            }

            // This row contains the actual icons and labels for the navigation items
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp), // Adding padding for spacing
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top // Align items to the top
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                // Navigate to the selected route if it's not already selected
                                if (!isSelected) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icon for each bottom navigation item
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) Color(0xFF9B00D8) else Color.Black, // Change icon color based on selection
                            modifier = Modifier.size(28.dp) // Set the size of the icon
                        )
                    }
                }
            }
        }
    }
}
