package grapes.microservices.views.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import grapes.microservices.R
import grapes.microservices.models.data.ArticleFilter
import grapes.microservices.ui.theme.White

@Composable
fun MyTopBar(
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // left side : logo + texte
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.logo_grapes), // Ton logo
                contentDescription = "Grapes Logo",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.grapes),
                style = MaterialTheme.typography.titleLarge,
            )
        }

        // Left side : notification, cart, profil
        Row(verticalAlignment = Alignment.CenterVertically) {
            // notification
            MyIcon(Icons.Default.Notifications, "Notifications",
                modifier = Modifier.padding(end = 8.dp))

            // cart
            MyIcon(Icons.Default.ShoppingCart, "Shopping cart",
                modifier = Modifier.padding(end = 8.dp))

            // profile
//            // TODO Replace with true profile image
            MyIcon(Icons.Default.AccountBox, "Account")
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
private fun MyIcon(imageVector: ImageVector, description : String, modifier: Modifier = Modifier) {
    IconButton(onClick = { },
        modifier = modifier
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