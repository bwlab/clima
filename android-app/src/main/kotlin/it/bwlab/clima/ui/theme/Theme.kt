package it.bwlab.clima.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    tertiary = AccentBlue,
    onTertiary = Color.White,
    background = BgLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    inverseSurface = OnSurfaceLight,
    inverseOnSurface = SurfaceLight,
    inversePrimary = AccentBlueSoft,
    outline = OutlineLight,
    outlineVariant = Color(0xFFD9DDE7),
    scrim = Color(0x99000000),
    error = Color(0xFFC62828),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
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
    tertiary = AccentBlue,
    onTertiary = Color.White,
    background = BgDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    inverseSurface = OnSurfaceDark,
    inverseOnSurface = SurfaceDark,
    inversePrimary = Color(0xFF143A7B),
    outline = OutlineDark,
    outlineVariant = Color(0xFF2A3140),
    scrim = Color(0x99000000),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * Tema dell'app: dynamic color disattivato di proposito.
 * Light: surface bianca, surfaceVariant grigia, testo scuro — niente schermo vuoto.
 */
@Composable
fun ClimaTheme(theme: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (theme) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (dark) DarkColors else LightColors
    val view = LocalView.current
    val context = LocalContext.current

    // Non chiamare AppCompatDelegate.setDefaultNightMode da qui:
    // ricrea l'Activity e può lasciare uno Surface vuoto (schermo bianco).

    if (!view.isInEditMode) {
        SideEffect {
            val window = context.findActivity()?.window ?: return@SideEffect
            val bg = colors.background.toArgb()
            window.statusBarColor = bg
            window.navigationBarColor = bg
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = ClimaTypography
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.onBackground) {
            content()
        }
    }
}
