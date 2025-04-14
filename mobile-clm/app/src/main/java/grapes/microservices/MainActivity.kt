package grapes.microservices

import android.os.Bundle
import android.util.Log // Ajoutez l'import de Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import grapes.microservices.ui.theme.MobileCLMTheme
import grapes.microservices.views.components.MyNavigation
import java.net.NetworkInterface
import java.net.InetAddress
import java.util.Collections

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupérer l'adresse IP et afficher dans les logs
        val ipAddress = getDeviceIpAddress()
        Log.d("IP_Address", "Adresse IP de l'appareil : $ipAddress")

        enableEdgeToEdge()
        setContent {
            MobileCLMTheme(false) {
                MyNavigation()
            }
        }
    }

    // Méthode pour récupérer uniquement l'IPv4
    private fun getDeviceIpAddress(): String? {
        try {
            // Obtenez les interfaces réseau de l'appareil
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())

            // Parcourez toutes les interfaces réseau
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)

                // Parcourez les adresses de chaque interface réseau
                for (address in addresses) {
                    // Vérifiez que l'adresse est IPv4 (pas une adresse de boucle ou une adresse IPv6)
                    if (address is InetAddress && !address.isLoopbackAddress && address.hostAddress.indexOf(":") == -1) {
                        return address.hostAddress // Retourne l'adresse IPv4
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
