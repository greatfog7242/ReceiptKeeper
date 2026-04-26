package com.receiptkeeper.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
    companion object {
        private val ICON_THEME_KEY = stringPreferencesKey("icon_theme")
        private val TREEMAP_THRESHOLD_KEY = doublePreferencesKey("treemap_threshold")
        private val TREEMAP_ASPECT_RATIO_KEY = doublePreferencesKey("treemap_aspect_ratio")
        private val TIMESTAMP_BOOK_ID_KEY = longPreferencesKey("timestamp_book_id")
        private val CNY_TO_USD_RATE_KEY = doublePreferencesKey("cny_to_usd_rate")

        /** Default CNY → USD rate (approximate; user can override in Settings). */
        const val DEFAULT_CNY_TO_USD_RATE = 0.1374
        val SUPPORTED_CURRENCIES = listOf("USD", "CNY")
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

    val timestampBookId: Flow<Long?> = context.dataStore.data
        .map { preferences -> preferences[TIMESTAMP_BOOK_ID_KEY] }

    suspend fun updateTimestampBookId(bookId: Long?) {
        context.dataStore.edit { preferences ->
            if (bookId == null) preferences.remove(TIMESTAMP_BOOK_ID_KEY)
            else preferences[TIMESTAMP_BOOK_ID_KEY] = bookId
        }
    }

    /** CNY → USD exchange rate. Defaults to [DEFAULT_CNY_TO_USD_RATE]. */
    val cnyToUsdRate: Flow<Double> = context.dataStore.data
        .map { preferences -> preferences[CNY_TO_USD_RATE_KEY] ?: DEFAULT_CNY_TO_USD_RATE }

    suspend fun updateCnyToUsdRate(rate: Double) {
        context.dataStore.edit { preferences ->
            preferences[CNY_TO_USD_RATE_KEY] = rate
        }
    }

}