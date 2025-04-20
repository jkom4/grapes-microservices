package com.example.mobile_cll.views

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mobile_cll.views.components.BottomNavigationBar
import com.example.mobile_cll.views.components.SignaturePadComponent
import com.example.mobile_cll.views.components.TopSection
import com.example.mobile_cll.viewmodels.CompletedOrderViewModel
import com.example.mobile_cll.viewmodels.CompletedOrderViewModelFactory

@Composable
fun CompletedOrderScreen(
    navController: NavController,
    tripId: String,
    viewModel: CompletedOrderViewModel = viewModel(factory = CompletedOrderViewModelFactory(LocalContext.current))
) {
    Log.d("CompletedOrderScreen", "tripId reçu au démarrage: $tripId")
    val context = LocalContext.current

    val commentState by viewModel.commentState.collectAsState()
    val imageUris by viewModel.imageUris.collectAsState()
    val isDoorstepDelivery by viewModel.isDoorstepDelivery.collectAsState()
    val signatureBitmap by viewModel.signatureBitmap.collectAsState()
    val showSignatureDialog by viewModel.showSignatureDialog.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.addImage(it) } }

    LaunchedEffect(saveStatus) {
        saveStatus?.let { status ->
            Log.d("CompletedOrderScreen", "saveStatus modifié: $status")
            if (status.startsWith("Erreur")) {
                Log.e("CompletedOrderScreen", "Erreur de soumission: $status")
                Toast.makeText(context, "Erreur: $status", Toast.LENGTH_LONG).show()
                viewModel.resetSaveStatus()
            }
        }
    }

    Scaffold(
        topBar = { TopSection(navController, "Valid Order") },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("CompletedOrder", fontSize = 22.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Ajouter un commentaire", fontSize = 16.sp, color = Color.Gray)
            TextField(
                value = commentState,
                onValueChange = { viewModel.updateComment(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(8.dp),
                placeholder = { Text("Entrez un commentaire...", color = Color.Gray) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Prendre des photos du produit", fontSize = 16.sp, color = Color.Gray)

            LazyRow {
                items(imageUris) { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAD7E))
            ) {
                Text("Ajouter des photos", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = isDoorstepDelivery,
                    onCheckedChange = { viewModel.toggleDoorstepDelivery(it) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Livraison à domicile", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.d("CompletedOrderScreen", "Soumission des données:")
                    Log.d("CompletedOrderScreen", "tripId: $tripId")
                    Log.d("CompletedOrderScreen", "commentaire: $commentState")
                    Log.d("CompletedOrderScreen", "imageUris: $imageUris")
                    Log.d("CompletedOrderScreen", "isDoorstepDelivery: $isDoorstepDelivery")
                    Log.d("CompletedOrderScreen", "signatureBitmap: ${signatureBitmap != null}")

                    viewModel.confirmDelivery(tripId)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAD7E))
            ) {
                Text("Soumettre", color = Color.White)
            }
        }

        if (showSignatureDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissSignatureDialog() },
                confirmButton = {
                    Row {
                        Button(
                            onClick = { viewModel.clearSignature() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
                        ) {
                            Text("Effacer")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.confirmDelivery(tripId) }
                        ) {
                            Text("Confirmer")
                        }
                    }
                },
                title = { Text("Signature") },
                text = {
                    Column {
                        SignaturePadComponent(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            onSignatureChange = { viewModel.updateSignature(it) }
                        )
                    }
                }
            )
        }
    }
}