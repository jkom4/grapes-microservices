package grapes.microservices.views.components.OrderComponents

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun formatDateString(dateString: String, locale: Locale): String {
    return try {
        // Parse the ISO date string
        val dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        // Define the formatter based on locale
        val pattern = when (locale.language) {
            "fr" -> "d MMMM yyyy 'à' HH:mm"
            else -> "d MMMM yyyy 'at' HH:mm"
        }
        val formatter = DateTimeFormatter.ofPattern(pattern, locale)
        // Format the date
        dateTime.format(formatter)
    } catch (e: Exception) {
        // Fallback to original string if parsing fails
        dateString
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun formatOrderDate(dateString: String): String {
    // Get the current locale from the configuration
    val locale = LocalContext.current.resources.configuration.locales[0]
    // Call the non-composable utility function
    return formatDateString(dateString, locale)
}