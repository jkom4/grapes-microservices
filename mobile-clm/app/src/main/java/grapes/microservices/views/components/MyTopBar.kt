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
import grapes.microservices.R
import grapes.microservices.ui.theme.White

@Composable
fun MyTopBar(
    modifier: Modifier
) {
    // horizontale Bar
    Row(
        modifier = modifier
            .fillMaxWidth(),
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

        // Partie droite : cloche, panier, avatar
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icône de notification (cloche)
            MyIcon(Icons.Default.Notifications, "Notifications")

            Spacer(modifier = Modifier.width(8.dp))

            // Icône de panier
            MyIcon(Icons.Default.ShoppingCart, "Shopping cart")

            Spacer(modifier = Modifier.width(8.dp))

            // Avatar (image de profil)
            MyIcon(Icons.Default.AccountBox, "Account")

//            // TODO Remplacez par une image de profil réelle
//            Image(
//                painter = painterResource(id = R.drawable.ic_avatar), // Ton image de profil
//                contentDescription = "User Avatar",
//                modifier = Modifier
//                    .size(36.dp)
//                    .clip(CircleShape)
//            )
        }
    }
}

@Composable
fun MyIcon(imageVector: ImageVector, description : String) {
    IconButton(onClick = { },
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