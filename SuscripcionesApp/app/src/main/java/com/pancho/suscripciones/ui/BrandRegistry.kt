package com.pancho.suscripciones.ui

data class Brand(val colorHex: String, val domain: String)

/**
 * Coincidencia local por palabra clave (sin internet) para autocompletar color de marca
 * y dominio sugerido al escribir el nombre de una suscripción. Set curado de servicios comunes;
 * lo que no coincide sigue usando el selector manual de color.
 */
private val brands: List<Pair<String, Brand>> = listOf(
    "netflix" to Brand("#E50914", "netflix.com"),
    "disney" to Brand("#113CCF", "disneyplus.com"),
    "hbo" to Brand("#8B2AB5", "max.com"),
    "max" to Brand("#8B2AB5", "max.com"),
    "amazon prime" to Brand("#00A8E1", "primevideo.com"),
    "prime video" to Brand("#00A8E1", "primevideo.com"),
    "paramount" to Brand("#0064FF", "paramountplus.com"),
    "star+" to Brand("#0F0F14", "starplus.com"),
    "apple tv" to Brand("#000000", "tv.apple.com"),
    "crunchyroll" to Brand("#F47521", "crunchyroll.com"),
    "youtube" to Brand("#FF0000", "youtube.com"),
    "spotify" to Brand("#1ED760", "spotify.com"),
    "apple music" to Brand("#FA243C", "music.apple.com"),
    "deezer" to Brand("#A238FF", "deezer.com"),
    "tidal" to Brand("#000000", "tidal.com"),
    "google one" to Brand("#4285F4", "one.google.com"),
    "icloud" to Brand("#3693F3", "icloud.com"),
    "drive" to Brand("#4285F4", "drive.google.com"),
    "dropbox" to Brand("#0061FF", "dropbox.com"),
    "onedrive" to Brand("#0078D4", "onedrive.live.com"),
    "uber eats" to Brand("#06C167", "ubereats.com"),
    "uber" to Brand("#000000", "uber.com"),
    "rappi" to Brand("#FF441F", "rappi.com"),
    "pedidosya" to Brand("#FA0050", "pedidosya.cl"),
    "didi" to Brand("#FF7300", "didiglobal.com"),
    "cabify" to Brand("#6E31F7", "cabify.com"),
    "lider" to Brand("#E10600", "lider.cl"),
    "jumbo" to Brand("#EE1C25", "jumbo.cl"),
    "amazon" to Brand("#FF9900", "amazon.com"),
    "playstation" to Brand("#003791", "playstation.com"),
    "xbox" to Brand("#107C10", "xbox.com"),
    "nintendo" to Brand("#E60012", "nintendo.com"),
    "chatgpt" to Brand("#10A37F", "chat.openai.com"),
    "claude" to Brand("#D97757", "claude.ai"),
    "notion" to Brand("#000000", "notion.so"),
    "canva" to Brand("#00C4CC", "canva.com"),
)

/** Busca coincidencia por substring (case-insensitive) en el nombre ingresado. */
fun findBrand(name: String): Brand? {
    val n = name.trim().lowercase()
    if (n.isEmpty()) return null
    return brands.firstOrNull { (keyword, _) -> n.contains(keyword) }?.second
}
