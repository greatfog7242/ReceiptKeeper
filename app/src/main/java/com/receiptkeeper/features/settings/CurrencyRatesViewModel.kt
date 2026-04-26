package com.receiptkeeper.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.core.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CurrencyRatesUiState(
    val cnyToUsdRate: Double = PreferencesManager.DEFAULT_CNY_TO_USD_RATE,
    val isSaved: Boolean = false
)

@HiltViewModel
class CurrencyRatesViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurrencyRatesUiState())
    val uiState: StateFlow<CurrencyRatesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.cnyToUsdRate.collect { rate ->
                _uiState.update { it.copy(cnyToUsdRate = rate) }
            }
        }
    }

    fun saveRates(cnyToUsd: Double) {
        viewModelScope.launch {
            preferencesManager.updateCnyToUsdRate(cnyToUsd)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun clearSaved() {
        _uiState.update { it.copy(isSaved = false) }
    }
}
