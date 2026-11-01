package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.LanguageScript

val PlayfairDisplayFontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal),
    Font(R.font.playfair_display, FontWeight.SemiBold),
    Font(R.font.playfair_display, FontWeight.Bold)
)

val SourceSans3FontFamily = FontFamily(
    Font(R.font.source_sans_3, FontWeight.Normal),
    Font(R.font.source_sans_3, FontWeight.Medium),
    Font(R.font.source_sans_3, FontWeight.SemiBold),
    Font(R.font.source_sans_3, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium)
)

val NotoSansDevanagariFontFamily = FontFamily(
    Font(R.font.noto_sans_devanagari, FontWeight.Normal),
    Font(R.font.noto_sans_devanagari, FontWeight.Medium),
    Font(R.font.noto_sans_devanagari, FontWeight.SemiBold),
    Font(R.font.noto_sans_devanagari, FontWeight.Bold)
)

val NotoSerifDevanagariFontFamily = FontFamily(
    Font(R.font.noto_serif_devanagari, FontWeight.Normal),
    Font(R.font.noto_serif_devanagari, FontWeight.SemiBold),
    Font(R.font.noto_serif_devanagari, FontWeight.Bold)
)

fun buildEditorialTypography(
    fontScale: Float = 1.0f,
    script: LanguageScript = LanguageScript.LATIN
): Typography {
    val scale = fontScale.coerceIn(0.85f, 1.6f)
    val isIndic = script.isIndicOrTallMatra
    // Increase line-height multiplier for Indic & Perso-Arabic scripts so matras and diacritics never clip
    val lineHeightBoost = if (isIndic) 1.15f else 1.0f
    // Include font padding for Indic scripts to prevent top/bottom matra clipping
    val platformStyle = PlatformTextStyle(includeFontPadding = isIndic)

    val displayFont = when (script) {
        LanguageScript.LATIN -> PlayfairDisplayFontFamily
        LanguageScript.DEVANAGARI -> NotoSerifDevanagariFontFamily
        else -> FontFamily.Serif
    }

    val bodyFont = when (script) {
        LanguageScript.LATIN -> SourceSans3FontFamily
        LanguageScript.DEVANAGARI -> NotoSansDevanagariFontFamily
        else -> FontFamily.SansSerif
    }

    return Typography(
        displayLarge = TextStyle(
            fontFamily = displayFont,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * scale).sp,
            lineHeight = (36 * scale * lineHeightBoost).sp,
            letterSpacing = if (isIndic) 0.sp else (-0.3).sp,
            platformStyle = platformStyle
        ),
        displayMedium = TextStyle(
            fontFamily = displayFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = (24 * scale).sp,
            lineHeight = (32 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        ),
        headlineLarge = TextStyle(
            fontFamily = displayFont,
            fontWeight = FontWeight.Bold,
            fontSize = (22 * scale).sp,
            lineHeight = (30 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        ),
        headlineMedium = TextStyle(
            fontFamily = displayFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20 * scale).sp,
            lineHeight = (28 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        ),
        headlineSmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = (19 * scale).sp,
            lineHeight = (27 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        ),
        titleLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18 * scale).sp,
            lineHeight = (26 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        ),
        titleMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * scale).sp,
            lineHeight = (23 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        ),
        titleSmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14 * scale).sp,
            lineHeight = (21 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * scale).sp,
            lineHeight = (25 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * scale).sp,
            lineHeight = (22 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        ),
        bodySmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = (12.5f * scale).sp,
            lineHeight = (19 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        ),
        labelLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13 * scale).sp,
            lineHeight = (19 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * scale).sp,
            lineHeight = (17 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * scale).sp,
            lineHeight = (16 * scale * lineHeightBoost).sp,
            platformStyle = platformStyle
        )
    )
}

val Typography = buildEditorialTypography(1.0f)
