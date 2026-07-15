package com.pancho.suscripciones.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO fidelidad de diseño: reemplazar por las fuentes reales.
// 1. Descargar "Space Grotesk" y "Manrope" desde fonts.google.com
// 2. Copiar los .ttf a app/src/main/res/font/ (nombres en snake_case)
// 3. Reemplazar FontFamily.Default abajo por FontFamily(Font(R.font.xxx, FontWeight.xxx), ...)
val SpaceGrotesk = FontFamily.Default
val Manrope = FontFamily.Default

val AppTypography = Typography(
    headlineLarge = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 38.sp),
    titleLarge = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 21.sp),
    titleMedium = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 19.sp),
    bodyLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 14.5.sp),
    bodyMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp),
    labelSmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
    labelMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
)
