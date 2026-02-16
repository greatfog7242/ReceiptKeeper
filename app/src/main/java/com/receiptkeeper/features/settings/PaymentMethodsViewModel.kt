package com.receiptkeeper.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.data.local.entity.PaymentType
import com.receiptkeeper.data.repository.PaymentMethodRepository
import com.receiptkeeper.domain.model.PaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    init {
        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            paymentMethodRepository.getAllPaymentMethods()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load payment methods"
                        )
                    }
                }
                .collect { methods ->
                    _uiState.update {
                        it.copy(
                            paymentMethods = methods,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun createPaymentMethod(name: String, type: PaymentType, lastFourDigits: String?) {
        viewModelScope.launch {
            try {
                val method = PaymentMethod(
                    name = name,
                    type = type,
                    lastFourDigits = lastFourDigits?.takeIf { it.isNotBlank() },
                    createdAt = Instant.now()
                )
                paymentMethodRepository.insertPaymentMethod(method)
                _uiState.update { it.copy(showAddDialog = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to create payment method")
                }
            }
        }
    }

    fun updatePaymentMethod(method: PaymentMethod) {
        viewModelScope.launch {
            try {
                paymentMethodRepository.updatePaymentMethod(method)
                _uiState.update { it.copy(editingMethod = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update payment method")
                }
            }
        }
    }

    fun deletePaymentMethod(method: PaymentMethod) {
        viewModelScope.launch {
            try {
                paymentMethodRepository.deletePaymentMethod(method)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to delete payment method")
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingMethod = null) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun showEditDialog(method: PaymentMethod) {
        _uiState.update { it.copy(editingMethod = method, showAddDialog = false) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(editingMethod = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class PaymentMethodsUiState(
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val editingMethod: PaymentMethod? = null
)
