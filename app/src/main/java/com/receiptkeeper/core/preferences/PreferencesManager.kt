package com.receiptkeeper.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private companion object {
        val ICON_THEME_KEY = stringPreferencesKey("icon_theme")
        val TREEMAP_THRESHOLD_KEY = doublePreferencesKey("treemap_threshold")
        val TREEMAP_ASPECT_RATIO_KEY = doublePreferencesKey("treemap_aspect_ratio")
    }

    /**
     * Get the current icon theme
     */
    val iconTheme: Flow<IconTheme> = context.dataStore.data
        .map { preferences ->
            val themeString = preferences[ICON_THEME_KEY] ?: IconTheme.COLORFUL.name
            IconTheme.valueOf(themeString)
        }

    /**
     * Update the icon theme
     */
    suspend fun updateIconTheme(theme: IconTheme) {
        context.dataStore.edit { preferences ->
            preferences[ICON_THEME_KEY] = theme.name
        }
    }

    /**
     * Get the treemap threshold percentage
     */
    val treemapThreshold: Flow<Double> = context.dataStore.data
        .map { preferences ->
            preferences[TREEMAP_THRESHOLD_KEY] ?: 5.0 // Default: 5%
        }

    /**
     * Update the treemap threshold percentage
     */
    suspend fun updateTreemapThreshold(threshold: Double) {
        context.dataStore.edit { preferences ->
            preferences[TREEMAP_THRESHOLD_KEY] = threshold
        }
    }

    /**
     * Get the treemap target aspect ratio (width/height)
     */
    val treemapAspectRatio: Flow<Double> = context.dataStore.data
        .map { preferences ->
            preferences[TREEMAP_ASPECT_RATIO_KEY] ?: 1.0 // Default: squares (1.0)
        }

    /**
     * Update the treemap target aspect ratio
     */
    suspend fun updateTreemapAspectRatio(ratio: Double) {
        context.dataStore.edit { preferences ->
            preferences[TREEMAP_ASPECT_RATIO_KEY] = ratio
        }
    }
}