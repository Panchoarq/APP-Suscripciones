package com.pancho.suscripciones.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Descarga el ícono de un servicio a partir de su dominio (favicon público de Google, sin API key)
 * y lo guarda en almacenamiento interno de la app. Una vez descargado, funciona sin internet.
 */
object LogoDownloader {
    private fun normalizeDomain(input: String): String? {
        val trimmed = input.trim().removePrefix("http://").removePrefix("https://").removePrefix("www.")
        val domain = trimmed.substringBefore("/").trim()
        return domain.takeIf { it.contains(".") }
    }

    suspend fun downloadFavicon(context: Context, websiteOrDomain: String): String? =
        withContext(Dispatchers.IO) {
            val domain = normalizeDomain(websiteOrDomain) ?: return@withContext null
            runCatching {
                val url = URL("https://www.google.com/s2/favicons?domain=$domain&sz=128")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.inputStream.use { input ->
                    val logosDir = File(context.filesDir, "logos").apply { mkdirs() }
                    val file = File(logosDir, "${UUID.randomUUID()}.png")
                    file.outputStream().use { output -> input.copyTo(output) }
                    file.absolutePath
                }
            }.getOrNull()
        }

    fun deleteLogo(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }
}
