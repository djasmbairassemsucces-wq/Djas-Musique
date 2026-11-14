package com.dmcreation.djasmusique.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.dmcreation.djasmusique.data.ThemeMode

val Gold = Color(0xFFD4AF37)
val GoldLight = Color(0xFFF1D77A)

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF1A1400),
    secondary = GoldLight,
    background = Color(0xFF0B0B0E),
    onBackground = Color(0xFFF2F2F2),
    surface = Color(0xFF15151A),
    onSurface = Color(0xFFF2F2F2),
    surfaceVariant = Color(0xFF22222A),
    onSurfaceVariant = Color(0xFFBDBDC7)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF8A6D00),
    onPrimary = Color.White,
    secondary = Color(0xFFB8860B),
    background = Color(0xFFFAF8F2),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFEFEBDD),
    onSurfaceVariant = Color(0xFF55524A)
)

@Composable
fun DjasTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
