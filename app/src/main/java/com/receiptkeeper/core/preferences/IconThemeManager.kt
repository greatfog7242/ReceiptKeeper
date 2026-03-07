package com.receiptkeeper.core.preferences

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconThemeManager @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    val iconTheme: Flow<IconTheme> = preferencesManager.iconTheme

    suspend fun updateIconTheme(theme: IconTheme) {
        preferencesManager.updateIconTheme(theme)
    }
}

/**
 * CompositionLocal for icon theme
 */
val LocalIconTheme = staticCompositionLocalOf { IconTheme.COLORFUL }

/**
 * Composable function to get the current icon theme
 */
@Composable
fun rememberIconTheme(iconThemeManager: IconThemeManager): State<IconTheme> {
    return iconThemeManager.iconTheme.collectAsState(initial = IconTheme.COLORFUL)
}