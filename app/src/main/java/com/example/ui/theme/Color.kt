package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val WellbeingBlueLight = Color(0xFF1A73E8)
val WellbeingOnBlueLight = Color(0xFFFFFFFF)
val WellbeingBlueContainerLight = Color(0xFFD2E3FC)
val WellbeingOnBlueContainerLight = Color(0xFF041E49)

val WellbeingTealLight = Color(0xFF00796B)
val WellbeingTealContainerLight = Color(0xFFC4EED0)
val WellbeingOnTealContainerLight = Color(0xFF072711)

val WellbeingBackgroundLight = Color(0xFFF6F8FC)
val WellbeingSurfaceLight = Color(0xFFFFFFFF)
val WellbeingSurfaceVariantLight = Color(0xFFE9EEF6)
val WellbeingOnSurfaceLight = Color(0xFF1B1C1E)
val WellbeingOnSurfaceVariantLight = Color(0xFF5E6368)
val WellbeingOutlineLight = Color(0xFFDADCE0)

val WellbeingBlueDark = Color(0xFF8AB4F8)
val WellbeingOnBlueDark = Color(0xFF062E6F)
val WellbeingBlueContainerDark = Color(0xFF0842A0)
val WellbeingOnBlueContainerDark = Color(0xFFD2E3FC)

val WellbeingTealDark = Color(0xFF81C995)
val WellbeingTealContainerDark = Color(0xFF0F5223)
val WellbeingOnTealContainerDark = Color(0xFFC4EED0)

val WellbeingBackgroundDark = Color(0xFF111318)
val WellbeingSurfaceDark = Color(0xFF1B1E24)
val WellbeingSurfaceVariantDark = Color(0xFF282C34)
val WellbeingOnSurfaceDark = Color(0xFFE3E5E8)
val WellbeingOnSurfaceVariantDark = Color(0xFFA8AEB8)
val WellbeingOutlineDark = Color(0xFF3C4043)

/**
 * Harmonious Pixel Digital Wellbeing chart and app segment colors.
 */
val WellbeingChartPalette = listOf(
    Color(0xFF1A73E8), // Google Blue
    Color(0xFF1E8E3E), // Sage Green
    Color(0xFFE37400), // Warm Amber
    Color(0xFFD93025), // Coral Red
    Color(0xFF8430CE), // Soft Lavender
    Color(0xFF007B83), // Deep Cyan
    Color(0xFF3949AB), // Indigo
    Color(0xFFC2185B), // Berry
)

val WellbeingChartPaletteDark = listOf(
    Color(0xFF8AB4F8),
    Color(0xFF81C995),
    Color(0xFFFDD663),
    Color(0xFFF28B82),
    Color(0xFFC58AF9),
    Color(0xFF78D9EC),
    Color(0xFF9FA8DA),
    Color(0xFFF48FB1),
)

fun chartColorForIndex(index: Int, darkTheme: Boolean): Color {
    val palette = if (darkTheme) WellbeingChartPaletteDark else WellbeingChartPalette
    val safeIndex = ((index % palette.size) + palette.size) % palette.size
    return palette[safeIndex]
}
