package com.example.mobile_cll.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GrayColor = Color(0xFF6E6E6E)

private val AppColorScheme = lightColorScheme(
    primary = Color(0xFF4CAD7E),
    secondary = Color(0xFF808080),
    tertiary = Color.Gray,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = Color.Red,
)


@Composable
fun MobileCLLTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
