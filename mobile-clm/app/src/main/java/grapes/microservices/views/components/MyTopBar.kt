package grapes.microservices.views.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import grapes.microservices.R
import grapes.microservices.ui.theme.White

@Composable
fun MyTopBar(
    modifier: Modifier = Modifier,
    navController: NavController // Ajout du NavController
) {
    // horizontale Bar
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Partie gauche : logo + texte
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.logo_grapes), // Ton logo
                contentDescription = "Grapes Logo",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Grapes",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            MyIcon(
                imageVector = Icons.Default.Notifications,
                description = "Notifications"
            )

            Spacer(modifier = Modifier.width(8.dp))

            MyIcon(
                imageVector = Icons.Default.ShoppingCart,
                description = "Shopping cart",
                onClick = { navController.navigate("cart") } // Navigation vers CartScreen
            )

            Spacer(modifier = Modifier.width(8.dp))

            MyIcon(
                imageVector = Icons.Default.AccountBox,
                description = "Account"
            )
        }
    }
}

@Composable
fun MyIcon(
    imageVector: ImageVector,
    description: String,
    onClick: () -> Unit = {}
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.primary)
            .size(35.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = description,
            tint = White
        )
    }
}