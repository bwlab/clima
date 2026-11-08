package it.bwlab.clima.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Modalità tema: Sistema / Chiaro / Scuro (impostazione dell'app).
 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val LightColors = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = AccentBlueSoft,
    onPrimaryContainer = Color(0xFF0B3D9C),
    secondary = AccentOrange,
    onSecondary = Color.White,
    secondaryContainer = AccentOrangeSoft,
    onSecondaryContainer = Color(0xFF93300A),
    background = BgLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineLight,
    error = Color(0xFFC62828),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF143A7B),
    onPrimaryContainer = AccentBlueSoft,
    secondary = AccentOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF7A3108),
    onSecondaryContainer = AccentOrangeSoft,
    background = BgDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

/**
 * Tema dell'app: dynamic color disattivato di proposito, i colori delle
 * modalità (blu/arancio) devono restare riconoscibili.
 */
@Composable
fun ClimaTheme(theme: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (theme) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = ClimaTypography,
        content = content
    )
}
