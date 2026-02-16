package com.receiptkeeper.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.data.repository.VendorRepository
import com.receiptkeeper.domain.model.Vendor
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
class VendorsViewModel @Inject constructor(
    private val vendorRepository: VendorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VendorsUiState())
    val uiState: StateFlow<VendorsUiState> = _uiState.asStateFlow()

    init {
        loadVendors()
    }

    private fun loadVendors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            vendorRepository.getAllVendors()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load vendors"
                        )
                    }
                }
                .collect { vendors ->
                    _uiState.update {
                        it.copy(
                            vendors = vendors,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun createVendor(name: String) {
        viewModelScope.launch {
            try {
                val vendor = Vendor(name = name, createdAt = Instant.now())
                vendorRepository.insertVendor(vendor)
                _uiState.update { it.copy(showAddDialog = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to create vendor")
                }
            }
        }
    }

    fun updateVendor(vendor: Vendor) {
        viewModelScope.launch {
            try {
                vendorRepository.updateVendor(vendor)
                _uiState.update { it.copy(editingVendor = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update vendor")
                }
            }
        }
    }

    fun deleteVendor(vendor: Vendor) {
        viewModelScope.launch {
            try {
                vendorRepository.deleteVendor(vendor)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to delete vendor")
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingVendor = null) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun showEditDialog(vendor: Vendor) {
        _uiState.update { it.copy(editingVendor = vendor, showAddDialog = false) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(editingVendor = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class VendorsUiState(
    val vendors: List<Vendor> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val editingVendor: Vendor? = null
)
