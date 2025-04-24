package grapes.microservices.views.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart // Remplace avec l'icône que tu veux utiliser
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import grapes.microservices.R

@Composable
fun PromoBox(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE1BEE7), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.7f)) {
            Text(text = stringResource(R.string.promo_title), color = Color(0xFF6A1B9A), fontWeight = FontWeight.Bold)
            Text(text = stringResource(R.string.promo_subtitle), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = { navController.navigate("catalog") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA68C8))) {
                Text(text = stringResource(R.string.promo_button))
            }
        }
        Icon(
            imageVector = Icons.Filled.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterEnd).size(40.dp),
            tint = Color(0xFF6A1B9A)
        )
    }
}
