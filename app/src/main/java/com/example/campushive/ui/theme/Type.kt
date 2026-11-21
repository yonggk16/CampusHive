package com.example.campushive.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.campushive.R

val AlloFontFamily = FontFamily(
    Font(R.font.allo_regular, FontWeight.Normal),
    Font(R.font.allo_medium, FontWeight.Medium),
    Font(R.font.allo_semibold, FontWeight.SemiBold),
    Font(R.font.allo_bold, FontWeight.Bold),
    Font(R.font.allo_extrabold, FontWeight.ExtraBold)
)

object CampusTypography {
    val Display = TextStyle(
        fontFamily = AlloFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.8).sp,
        lineHeight = 38.sp
    )
    val Title = TextStyle(
        fontFamily = AlloFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp,
        lineHeight = 34.sp
    )
    val ScreenTitle = TextStyle(
        fontFamily = AlloFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.5).sp,
        lineHeight = 28.sp
    )
    val Section = TextStyle(
        fontFamily = AlloFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    )
    val Body = TextStyle(
        fontFamily = AlloFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val Caption = TextStyle(
        fontFamily = AlloFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
    val Eyebrow = TextStyle(
        fontFamily = AlloFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.8.sp
    )
    val Tag = TextStyle(
        fontFamily = AlloFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp
    )
}

private val DefaultTypography = Typography()

val AppTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = AlloFontFamily),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = AlloFontFamily),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = AlloFontFamily),
    headlineLarge = CampusTypography.Display,
    headlineMedium = CampusTypography.ScreenTitle,
    headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = AlloFontFamily),
    titleLarge = CampusTypography.Title,
    titleMedium = CampusTypography.Section,
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = AlloFontFamily),
    bodyLarge = CampusTypography.Body,
    bodyMedium = CampusTypography.Body.copy(fontSize = 13.sp),
    bodySmall = DefaultTypography.bodySmall.copy(fontFamily = AlloFontFamily),
    labelLarge = DefaultTypography.labelLarge.copy(fontFamily = AlloFontFamily),
    labelMedium = CampusTypography.Caption,
    labelSmall = CampusTypography.Tag
)
