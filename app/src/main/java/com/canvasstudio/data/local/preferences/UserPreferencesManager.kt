package com.canvasstudio.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

open class UserPreferencesManager(private val context: Context? = null) {

    private object PreferencesKeys {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
        val GRID_ENABLED_KEY = booleanPreferencesKey("grid_enabled")
        val MODULE_TEXT_ENABLED = booleanPreferencesKey("module_text_enabled")
        val MODULE_IMAGE_ENABLED = booleanPreferencesKey("module_image_enabled")
        val MODULE_CHART_ENABLED = booleanPreferencesKey("module_chart_enabled")
        val BRAND_TITLE = stringPreferencesKey("brand_title")
        val CANVAS_WIDTH = intPreferencesKey("canvas_width")
        val CANVAS_HEIGHT = intPreferencesKey("canvas_height")
        val IS_LOCKED = booleanPreferencesKey("is_locked")
    }

    open val isLockedFlow: Flow<Boolean> by lazy {
        context?.dataStore?.data
            ?.catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            ?.map { it[PreferencesKeys.IS_LOCKED] ?: false }
            ?: kotlinx.coroutines.flow.flowOf(false)
    }

    open val gridEnabledFlow: Flow<Boolean> by lazy {
        context?.dataStore?.data
            ?.catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            ?.map { it[PreferencesKeys.GRID_ENABLED_KEY] ?: true }
            ?: kotlinx.coroutines.flow.flowOf(true)
    }

    open val brandTitleFlow: Flow<String> by lazy {
        context?.dataStore?.data
            ?.catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            ?.map { it[PreferencesKeys.BRAND_TITLE] ?: "Canvas Studio" }
            ?: kotlinx.coroutines.flow.flowOf("Canvas Studio")
    }

    open val canvasDimensionsFlow: Flow<Pair<Int, Int>> by lazy {
        context?.dataStore?.data
            ?.catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            ?.map { preferences ->
                Pair(
                    preferences[PreferencesKeys.CANVAS_WIDTH] ?: 2000,
                    preferences[PreferencesKeys.CANVAS_HEIGHT] ?: 2000
                )
            }
            ?: kotlinx.coroutines.flow.flowOf(Pair(2000, 2000))
    }

    open suspend fun setBrandTitle(title: String) {
        context?.dataStore?.edit { it[PreferencesKeys.BRAND_TITLE] = title }
    }

    open suspend fun setCanvasDimensions(width: Int, height: Int) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.CANVAS_WIDTH] = width
            preferences[PreferencesKeys.CANVAS_HEIGHT] = height
        }
    }

    open val darkModeFlow: Flow<Boolean> by lazy {
        context?.dataStore?.data
            ?.catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            ?.map { preferences ->
                preferences[PreferencesKeys.DARK_MODE_KEY] ?: false
            }
            ?: kotlinx.coroutines.flow.flowOf(false)
    }

    open val modulesStateFlow: Flow<Map<String, Boolean>> by lazy {
        context?.dataStore?.data
            ?.catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            ?.map { preferences ->
                mapOf(
                    "text" to (preferences[PreferencesKeys.MODULE_TEXT_ENABLED] ?: true),
                    "image" to (preferences[PreferencesKeys.MODULE_IMAGE_ENABLED] ?: true),
                    "chart" to (preferences[PreferencesKeys.MODULE_CHART_ENABLED] ?: true)
                )
            }
            ?: kotlinx.coroutines.flow.flowOf(mapOf("text" to true, "image" to true, "chart" to true))
    }

    open suspend fun setDarkMode(enabled: Boolean) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE_KEY] = enabled
        }
    }

    open suspend fun setGridEnabled(enabled: Boolean) {
        context?.dataStore?.edit { it[PreferencesKeys.GRID_ENABLED_KEY] = enabled }
    }

    open suspend fun setLocked(enabled: Boolean) {
        context?.dataStore?.edit { it[PreferencesKeys.IS_LOCKED] = enabled }
    }

    open suspend fun setModuleEnabled(moduleType: String, enabled: Boolean) {
        context?.dataStore?.edit { preferences ->
            when (moduleType) {
                "text" -> preferences[PreferencesKeys.MODULE_TEXT_ENABLED] = enabled
                "image" -> preferences[PreferencesKeys.MODULE_IMAGE_ENABLED] = enabled
                "chart" -> preferences[PreferencesKeys.MODULE_CHART_ENABLED] = enabled
            }
        }
    }
}