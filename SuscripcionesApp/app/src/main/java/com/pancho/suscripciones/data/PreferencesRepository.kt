package com.pancho.suscripciones.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "prefs")

enum class VisualStyle { LISTA, MAQUINA_ESCRIBIR, SUAVE }

class PreferencesRepository(private val context: Context) {
    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val visualStyleKey = stringPreferencesKey("visual_style")

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { it[darkThemeKey] ?: true }

    val visualStyle: Flow<VisualStyle> = context.dataStore.data.map { prefs ->
        prefs[visualStyleKey]?.let { runCatching { VisualStyle.valueOf(it) }.getOrNull() }
            ?: VisualStyle.LISTA
    }

    suspend fun setDarkTheme(value: Boolean) {
        context.dataStore.edit { it[darkThemeKey] = value }
    }

    suspend fun setVisualStyle(value: VisualStyle) {
        context.dataStore.edit { it[visualStyleKey] = value.name }
    }
}
