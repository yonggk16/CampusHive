package com.example.campushive.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object CampusTypography {
    val Display = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.8).sp,
        lineHeight = 38.sp
    )
    val Title = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp,
        lineHeight = 34.sp
    )
    val ScreenTitle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.5).sp,
        lineHeight = 28.sp
    )
    val Section = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    )
    val Body = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val Caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
    val Eyebrow = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 0.8.sp
    )
    val Tag = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp
    )
}

val AppTypography = Typography(
    headlineLarge = CampusTypography.Display,
    headlineMedium = CampusTypography.ScreenTitle,
    titleLarge = CampusTypography.Title,
    titleMedium = CampusTypography.Section,
    bodyLarge = CampusTypography.Body,
    bodyMedium = CampusTypography.Body.copy(fontSize = 13.sp),
    labelMedium = CampusTypography.Caption,
    labelSmall = CampusTypography.Tag
)
