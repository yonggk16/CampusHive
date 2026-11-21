package com.example.campushive.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF14171E),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF14171E),
    onSecondary = Color(0xFF14171E),
    background = Color(0xFFFAF8F2),
    onBackground = Color(0xFF14171E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF14171E),
    outline = Color(0xFFE2DFD4),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFAFAFA),
    onPrimary = Color(0xFF09090B),
    secondary = Color(0xFFF5C249),
    onSecondary = Color(0xFF09090B),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFAFAFA),
    surface = Color(0xFF141417),
    onSurface = Color(0xFFFAFAFA),
    outline = Color(0xFF27272C),
)

@Composable
fun CampusHiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    remember(context) {
        CampusThemeState.load(context)
        true
    }

    val resolvedDark = when (CampusThemeState.preference) {
        ThemePreference.SYSTEM -> darkTheme
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    CampusThemeState.isDark = resolvedDark

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !resolvedDark
            }
        }
    }

    val colorScheme = if (resolvedDark) DarkColors else LightColors
    val configuration = LocalConfiguration.current
    val currentDensity = LocalDensity.current

    // Standard baseline mobile design width = 390.dp
    val baseWidthDp = 390f
    val scaleFactor = (configuration.screenWidthDp / baseWidthDp).coerceIn(0.85f, 1.20f)

    val scaledDensity = remember(currentDensity.density, currentDensity.fontScale, scaleFactor) {
        Density(
            density = currentDensity.density * scaleFactor,
            fontScale = currentDensity.fontScale
        )
    }

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
    }
}
