package com.pancho.suscripciones.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.ui.graphics.vector.ImageVector

/** Infiere el ícono de categoría por palabra clave en el nombre (case-insensitive). */
fun iconForCategory(name: String): ImageVector? {
    val n = name.lowercase()
    return when {
        listOf("streaming", "video", "tv").any { n.contains(it) } -> Icons.Filled.PlayCircle
        listOf("music", "música", "musica", "audio").any { n.contains(it) } -> Icons.Filled.MusicNote
        listOf("almacen", "nube", "cloud", "drive").any { n.contains(it) } -> Icons.Filled.Cloud
        listOf("compra", "super", "mercado", "tienda").any { n.contains(it) } -> Icons.Filled.ShoppingCart
        listOf("comida", "delivery", "food", "restaur").any { n.contains(it) } -> Icons.Filled.LocalShipping
        listOf("transporte", "auto", "car", "movilidad", "viaje").any { n.contains(it) } -> Icons.Filled.DirectionsCar
        else -> null
    }
}

/** Genera hasta 2 iniciales a partir de las primeras letras de las palabras del nombre. */
fun monoForName(name: String): String =
    name.trim().split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
