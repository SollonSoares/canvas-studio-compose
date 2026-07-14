package com.canvasstudio.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
        val MODULE_TEXT_ENABLED = booleanPreferencesKey("module_text_enabled")
        val MODULE_IMAGE_ENABLED = booleanPreferencesKey("module_image_enabled")
        val MODULE_CHART_ENABLED = booleanPreferencesKey("module_chart_enabled")
        val BRAND_TITLE = stringPreferencesKey("brand_title")
    }

    val brandTitleFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it[PreferencesKeys.BRAND_TITLE] ?: "Canvas Studio" }

    suspend fun setBrandTitle(title: String) {
        context.dataStore.edit { it[PreferencesKeys.BRAND_TITLE] = title }
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DARK_MODE_KEY] ?: false
        }

    val modulesStateFlow: Flow<Map<String, Boolean>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapOf(
                "text" to (preferences[PreferencesKeys.MODULE_TEXT_ENABLED] ?: true),
                "image" to (preferences[PreferencesKeys.MODULE_IMAGE_ENABLED] ?: true),
                "chart" to (preferences[PreferencesKeys.MODULE_CHART_ENABLED] ?: true)
            )
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setModuleEnabled(moduleType: String, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            when (moduleType) {
                "text" -> preferences[PreferencesKeys.MODULE_TEXT_ENABLED] = enabled
                "image" -> preferences[PreferencesKeys.MODULE_IMAGE_ENABLED] = enabled
                "chart" -> preferences[PreferencesKeys.MODULE_CHART_ENABLED] = enabled
            }
        }
    }
}