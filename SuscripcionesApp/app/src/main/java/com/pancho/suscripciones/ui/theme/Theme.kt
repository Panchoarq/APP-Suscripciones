package com.pancho.suscripciones.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class ExtraColors(
    val surface2: Color,
    val textMuted: Color,
    val accentSoft: Color,
    val danger: Color,
)

val LocalExtraColors = androidx.compose.runtime.staticCompositionLocalOf {
    ExtraColors(DarkSurface2, DarkTextMuted, DarkAccentSoft, DarkDanger)
}

@Composable
fun SuscripcionesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            background = DarkBg,
            surface = DarkSurface,
            onBackground = DarkText,
            onSurface = DarkText,
            primary = DarkAccent,
            outline = DarkBorder,
            error = DarkDanger,
        )
    } else {
        lightColorScheme(
            background = LightBg,
            surface = LightSurface,
            onBackground = LightText,
            onSurface = LightText,
            primary = LightAccent,
            outline = LightBorder,
            error = LightDanger,
        )
    }

    val extraColors = if (darkTheme) {
        ExtraColors(DarkSurface2, DarkTextMuted, DarkAccentSoft, DarkDanger)
    } else {
        ExtraColors(LightSurface2, LightTextMuted, LightAccentSoft, LightDanger)
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
