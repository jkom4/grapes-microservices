package grapes.microservices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import grapes.microservices.ui.theme.MobileCLMTheme
import grapes.microservices.utils.LanguageUtils
import grapes.microservices.utils.MyNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val languageCode = LanguageUtils.getSavedLanguage(this)
        LanguageUtils.setLanguage(this, languageCode)
        enableEdgeToEdge()
        setContent {
            MobileCLMTheme(false) {
                MyNavigation()
            }
        }
    }
}

