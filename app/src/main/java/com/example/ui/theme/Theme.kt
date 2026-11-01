package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val EditorialLightColorScheme = lightColorScheme(
    primary = LightPrimaryAccent,
    onPrimary = LightOnPrimaryAccent,
    primaryContainer = LightAccentContainer,
    onPrimaryContainer = LightOnAccentContainer,
    secondary = LightPrimaryAccent,
    onSecondary = LightOnPrimaryAccent,
    secondaryContainer = LightAccentContainer,
    onSecondaryContainer = LightOnAccentContainer,
    tertiary = WarningIndicator,
    onTertiary = LightSurface,
    tertiaryContainer = WarningContainerLight,
    onTertiaryContainer = OnWarningContainerLight,
    error = UrgentIndicator,
    onError = LightSurface,
    errorContainer = UrgentContainerLight,
    onErrorContainer = OnUrgentContainerLight,
    background = LightBackground,
    onBackground = LightPrimaryText,
    surface = LightSurface,
    onSurface = LightPrimaryText,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightSecondaryText,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceElevated,
    surfaceContainerHighest = LightAccentContainer,
    outline = LightSecondaryText,
    outlineVariant = LightDivider
)

private val EditorialDarkColorScheme = darkColorScheme(
    primary = DarkPrimaryAccent,
    onPrimary = DarkOnPrimaryAccent,
    primaryContainer = DarkAccentContainer,
    onPrimaryContainer = DarkOnAccentContainer,
    secondary = DarkPrimaryAccent,
    onSecondary = DarkOnPrimaryAccent,
    secondaryContainer = DarkAccentContainer,
    onSecondaryContainer = DarkOnAccentContainer,
    tertiary = WarningIndicatorDark,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkElevatedSurface,
    onTertiaryContainer = DarkPrimaryText,
    error = UrgentIndicatorDark,
    onError = DarkBackground,
    errorContainer = UrgentContainerDark,
    onErrorContainer = DarkPrimaryText,
    background = DarkBackground,
    onBackground = DarkPrimaryText,
    surface = DarkSurface,
    onSurface = DarkPrimaryText,
    surfaceVariant = DarkElevatedSurface,
    onSurfaceVariant = DarkSecondaryText,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkElevatedSurface,
    surfaceContainerHighest = DarkAccentContainer,
    outline = DarkSecondaryText,
    outlineVariant = DarkDivider
)

val EditorialShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun SurakshaChronicleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EditorialDarkColorScheme else EditorialLightColorScheme
    val scaledTypography = buildEditorialTypography(fontScale)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        shapes = EditorialShapes,
        content = content
    )
}
