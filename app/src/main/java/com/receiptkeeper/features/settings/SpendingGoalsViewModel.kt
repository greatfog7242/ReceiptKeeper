package com.receiptkeeper.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.data.local.entity.GoalPeriod
import com.receiptkeeper.data.repository.CategoryRepository
import com.receiptkeeper.data.repository.SpendingGoalRepository
import com.receiptkeeper.domain.model.Category
import com.receiptkeeper.domain.model.SpendingGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * ViewModel for spending goals management
 */
@HiltViewModel
class SpendingGoalsViewModel @Inject constructor(
    private val spendingGoalRepository: SpendingGoalRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpendingGoalsUiState())
    val uiState: StateFlow<SpendingGoalsUiState> = _uiState.asStateFlow()

    val spendingGoals: StateFlow<List<SpendingGoal>> = spendingGoalRepository.getAllSpendingGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Create a new spending goal
     */
    fun createSpendingGoal(
        amount: Double,
        period: GoalPeriod,
        categoryId: Long?
    ) {
        viewModelScope.launch {
            try {
                val goal = SpendingGoal(
                    amount = amount,
                    period = period,
                    categoryId = categoryId,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                spendingGoalRepository.insertSpendingGoal(goal)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to create goal: ${e.message}") }
            }
        }
    }

    /**
     * Update an existing spending goal
     */
    fun updateSpendingGoal(goal: SpendingGoal) {
        viewModelScope.launch {
            try {
                spendingGoalRepository.updateSpendingGoal(goal)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update goal: ${e.message}") }
            }
        }
    }

    /**
     * Delete a spending goal
     */
    fun deleteSpendingGoal(goal: SpendingGoal) {
        viewModelScope.launch {
            try {
                spendingGoalRepository.deleteSpendingGoal(goal)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete goal: ${e.message}") }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for spending goals screen
 */
data class SpendingGoalsUiState(
    val error: String? = null
)
