package grapes.microservices

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import grapes.microservices.models.utils.LocaleHelper
import grapes.microservices.ui.theme.MobileCLMTheme
import grapes.microservices.views.components.MyNavigation

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", "en") ?: "en"
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileCLMTheme(false) {
                MyNavigation()
            }
        }
    }
}