package grapes.microservices

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import grapes.microservices.ui.theme.MobileCLMTheme
import grapes.microservices.view.components.Navigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileCLMTheme {
                Navigation()
            }
        }
    }
}