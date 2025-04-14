package grapes.microservices.views.Settings

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import grapes.microservices.R
import grapes.microservices.models.utils.LocaleHelper

@Composable
fun SettingsScreen(context: Context) {
    val languages = listOf("English" to "en", "Français" to "fr")
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var selected by remember { mutableStateOf(prefs.getString("lang", "en") ?: "en") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(
                R.string.settings),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    stringResource(R.string.settings_language),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                languages.forEach { (label, code) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = selected == code,
                            onClick = {
                                selected = code
                                prefs.edit().putString("lang", code).apply()
                                LocaleHelper.setLocale(context, code)
                                (context as? Activity)?.recreate()
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = label, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
