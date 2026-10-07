package com.example.campushive.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun CampusHiveTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = AppTypography, content = content)
}