package com.example.campushive.ui.theme

import android.annotation.SuppressLint
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val Colors = lightColorScheme(
    primary = Charcoal,
    onPrimary = CardWhite,
    secondary = Honey,
    onSecondary = Charcoal,
    background = Cream,
    onBackground = Charcoal,
    surface = CardWhite,
    onSurface = Charcoal,
    outline = Hairline,
)

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun CampusHiveTheme(content: @Composable () -> Unit) {
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
        MaterialTheme(colorScheme = Colors, typography = AppTypography, content = content)
    }
}