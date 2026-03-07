package com.receiptkeeper.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.core.preferences.IconTheme
import com.receiptkeeper.core.preferences.IconThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IconThemeViewModel @Inject constructor(
    private val iconThemeManager: IconThemeManager
) : ViewModel() {

    val iconTheme: Flow<IconTheme> = iconThemeManager.iconTheme

    suspend fun updateIconTheme(theme: IconTheme) {
        iconThemeManager.updateIconTheme(theme)
    }

    fun updateIconThemeAsync(theme: IconTheme) {
        viewModelScope.launch {
            updateIconTheme(theme)
        }
    }
}