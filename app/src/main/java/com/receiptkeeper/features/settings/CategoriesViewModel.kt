package com.receiptkeeper.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.data.repository.CategoryRepository
import com.receiptkeeper.domain.model.Category
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
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            categoryRepository.getAllCategories()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load categories"
                        )
                    }
                }
                .collect { categories ->
                    _uiState.update {
                        it.copy(
                            categories = categories,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun createCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            try {
                val category = Category(
                    name = name,
                    colorHex = colorHex,
                    isDefault = false,
                    createdAt = Instant.now()
                )
                categoryRepository.insertCategory(category)
                _uiState.update { it.copy(showAddDialog = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to create category")
                }
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryRepository.updateCategory(category)
                _uiState.update { it.copy(editingCategory = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update category")
                }
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                // Only allow deleting non-default categories
                if (!category.isDefault) {
                    categoryRepository.deleteCategory(category)
                } else {
                    _uiState.update {
                        it.copy(error = "Cannot delete default categories")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to delete category")
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingCategory = null) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun showEditDialog(category: Category) {
        _uiState.update { it.copy(editingCategory = category, showAddDialog = false) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(editingCategory = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CategoriesUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val editingCategory: Category? = null
)
