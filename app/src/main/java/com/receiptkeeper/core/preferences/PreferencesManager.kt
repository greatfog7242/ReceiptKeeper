package com.receiptkeeper.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
}