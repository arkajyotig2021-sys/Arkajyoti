package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = WellbeingBlueLight,
    onPrimary = WellbeingOnBlueLight,
    primaryContainer = WellbeingBlueContainerLight,
    onPrimaryContainer = WellbeingOnBlueContainerLight,
    secondary = WellbeingTealLight,
    secondaryContainer = WellbeingTealContainerLight,
    onSecondaryContainer = WellbeingOnTealContainerLight,
    background = WellbeingBackgroundLight,
    onBackground = WellbeingOnSurfaceLight,
    surface = WellbeingSurfaceLight,
    onSurface = WellbeingOnSurfaceLight,
    surfaceVariant = WellbeingSurfaceVariantLight,
    onSurfaceVariant = WellbeingOnSurfaceVariantLight,
    surfaceContainerLow = WellbeingSurfaceLight,
    surfaceContainer = ColorTokens.LightContainer,
    surfaceContainerHigh = WellbeingSurfaceVariantLight,
    outlineVariant = WellbeingOutlineLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = WellbeingBlueDark,
    onPrimary = WellbeingOnBlueDark,
    primaryContainer = WellbeingBlueContainerDark,
    onPrimaryContainer = WellbeingOnBlueContainerDark,
    secondary = WellbeingTealDark,
    secondaryContainer = WellbeingTealContainerDark,
    onSecondaryContainer = WellbeingOnTealContainerDark,
    background = WellbeingBackgroundDark,
    onBackground = WellbeingOnSurfaceDark,
    surface = WellbeingSurfaceDark,
    onSurface = WellbeingOnSurfaceDark,
    surfaceVariant = WellbeingSurfaceVariantDark,
    onSurfaceVariant = WellbeingOnSurfaceVariantDark,
    surfaceContainerLow = WellbeingSurfaceDark,
    surfaceContainer = ColorTokens.DarkContainer,
    surfaceContainerHigh = WellbeingSurfaceVariantDark,
    outlineVariant = WellbeingOutlineDark,
)

private object ColorTokens {
    val LightContainer = androidx.compose.ui.graphics.Color(0xFFEEF2FA)
    val DarkContainer = androidx.compose.ui.graphics.Color(0xFF20242C)
}

@Composable
fun ScreenWellbeingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
