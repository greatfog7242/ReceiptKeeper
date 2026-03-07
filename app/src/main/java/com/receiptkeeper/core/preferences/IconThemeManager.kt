package com.receiptkeeper.core.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
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
 * Composable function to get the current icon theme
 */
@Composable
fun rememberIconTheme(): State<IconTheme> {
    val themeManager: IconThemeManager = hiltViewModel()
    return themeManager.iconTheme.collectAsState(initial = IconTheme.COLORFUL)
}