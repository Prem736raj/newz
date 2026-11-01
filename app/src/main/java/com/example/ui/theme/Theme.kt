package com.example.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import java.util.Locale

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

fun createLocalizedContext(baseContext: Context, language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.code)
    Locale.setDefault(locale)
    val config = Configuration(baseContext.resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return baseContext.createConfigurationContext(config)
}

@Composable
fun SurakshaChronicleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val localizedContext = remember(baseContext, appLanguage) {
        createLocalizedContext(baseContext, appLanguage)
    }
    val layoutDirection = if (appLanguage.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val colorScheme = if (darkTheme) EditorialDarkColorScheme else EditorialLightColorScheme
    val scaledTypography = remember(fontScale, appLanguage.script) {
        buildEditorialTypography(fontScale, appLanguage.script)
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedContext.resources.configuration,
        LocalLayoutDirection provides layoutDirection
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scaledTypography,
            shapes = EditorialShapes,
            content = content
        )
    }
}
