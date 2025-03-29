package grapes.microservices.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import grapes.microservices.viewmodel.HomeViewModel
import androidx.navigation.NavHostController
import grapes.microservices.R

@Composable
fun HomeScreen(navController: NavHostController?,homeVM: HomeViewModel) {
    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TopBar()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(null, homeVM = HomeViewModel())
}

@Composable
fun TopBar() {
    // Barre horizontale
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Partie gauche : logo + texte "Home"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.logo_grapes), // Ton logo
                contentDescription = "Grapes Logo",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Home",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        // Partie droite : cloche, panier, avatar
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icône de notification (cloche)
            IconButton(onClick = { /* TODO : action */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.Magenta // Exemple de couleur
                )
            }

            // Icône de panier
            IconButton(onClick = { /* TODO : action */ }) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Shopping Cart",
                    tint = Color.Magenta
                )
            }

            // Avatar (image de profil)
            IconButton(onClick = { /* TODO : action */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.Magenta // Exemple de couleur
                )
            }
        }
    }
}
