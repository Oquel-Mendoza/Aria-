package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalPremiumTheme = staticCompositionLocalOf { AriaThemes.MINIMAL_WHITE }

@Composable
fun AriaTheme(
    themeId: String,
    content: @Composable () -> Unit
) {
    val premiumTheme = AriaThemes.getThemeById(themeId)
    
    val colorScheme = if (premiumTheme.isDark) {
        darkColorScheme(
            primary = premiumTheme.primary,
            onPrimary = premiumTheme.onPrimary,
            secondary = premiumTheme.secondary,
            background = premiumTheme.background,
            surface = premiumTheme.surface,
            onBackground = premiumTheme.onBackground,
            onSurface = premiumTheme.onSurface,
            primaryContainer = premiumTheme.surface,
            onPrimaryContainer = premiumTheme.primary,
            surfaceVariant = premiumTheme.surface,
            onSurfaceVariant = premiumTheme.secondary
        )
    } else {
        lightColorScheme(
            primary = premiumTheme.primary,
            onPrimary = premiumTheme.onPrimary,
            secondary = premiumTheme.secondary,
            background = premiumTheme.background,
            surface = premiumTheme.surface,
            onBackground = premiumTheme.onBackground,
            onSurface = premiumTheme.onSurface,
            primaryContainer = premiumTheme.surface,
            onPrimaryContainer = premiumTheme.primary,
            surfaceVariant = premiumTheme.surface,
            onSurfaceVariant = premiumTheme.secondary
        )
    }

    CompositionLocalProvider(LocalPremiumTheme provides premiumTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
