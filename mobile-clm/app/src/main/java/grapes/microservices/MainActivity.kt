package grapes.microservices

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import grapes.microservices.models.utils.LocaleHelper
import grapes.microservices.ui.theme.MobileCLMTheme
import grapes.microservices.views.components.MyNavigation

class MainActivity : ComponentActivity() {
    private companion object {
        private const val TAG = "MainActivity"
    }

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
                MyNavigation(deepLinkUri = intent?.data)
            }
        }
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            Log.d(TAG, "Received deep link: $uri")
            if (uri.scheme == "grapes" && uri.host == "home") {
            } else {
                Log.w(TAG, "Unhandled deep link: $uri")
            }
        }
    }
}