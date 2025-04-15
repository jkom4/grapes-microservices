package grapes.microservices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import grapes.microservices.ui.theme.MobileCLMTheme
import grapes.microservices.views.components.MyNavigation

class MainActivity : ComponentActivity() {
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

