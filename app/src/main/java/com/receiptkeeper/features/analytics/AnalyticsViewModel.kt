package com.receiptkeeper.features.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.data.local.entity.CategorySpending
import com.receiptkeeper.data.repository.AnalyticsRepository
import com.receiptkeeper.data.repository.SpendingGoalRepository
import com.receiptkeeper.domain.model.Category
import com.receiptkeeper.domain.model.Receipt
import com.receiptkeeper.domain.model.SpendingGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for analytics screen
 * Manages date range selection and spending calculations
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val spendingGoalRepository: SpendingGoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    // Date range - default to current month
    private val _startDate = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    private val _endDate = MutableStateFlow(LocalDate.now())

    val startDate: StateFlow<LocalDate> = _startDate.asStateFlow()
    val endDate: StateFlow<LocalDate> = _endDate.asStateFlow()

    // Receipts for selected date range
    val receipts: StateFlow<List<Receipt>> = combine(
        _startDate,
        _endDate
    ) { start, end ->
        start to end
    }.flatMapLatest { (start, end) ->
        analyticsRepository.getReceiptsByDateRange(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total spending for selected date range
    val totalSpending: StateFlow<Double> = combine(
        _startDate,
        _endDate
    ) { start, end ->
        start to end
    }.flatMapLatest { (start, end) ->
        analyticsRepository.getTotalSpendingByDateRange(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Category spending breakdown
    val categoryBreakdown: StateFlow<List<CategorySpending>> = combine(
        _startDate,
        _endDate
    ) { start, end ->
        start to end
    }.flatMapLatest { (start, end) ->
        analyticsRepository.getCategorySpendingBreakdown(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All categories for reference
    val categories: StateFlow<List<Category>> = analyticsRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All spending goals
    val spendingGoals: StateFlow<List<SpendingGoal>> = spendingGoalRepository.getAllSpendingGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Update date range
     */
    fun setDateRange(start: LocalDate, end: LocalDate) {
        _startDate.value = start
        _endDate.value = end
    }

    /**
     * Set predefined date range (This Month, Last Month, etc.)
     */
    fun setPredefinedRange(range: DateRangePreset) {
        val now = LocalDate.now()
        when (range) {
            DateRangePreset.THIS_MONTH -> {
                _startDate.value = now.withDayOfMonth(1)
                _endDate.value = now
            }
            DateRangePreset.LAST_MONTH -> {
                val lastMonth = now.minusMonths(1)
                _startDate.value = lastMonth.withDayOfMonth(1)
                _endDate.value = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
            }
            DateRangePreset.LAST_30_DAYS -> {
                _startDate.value = now.minusDays(30)
                _endDate.value = now
            }
            DateRangePreset.LAST_90_DAYS -> {
                _startDate.value = now.minusDays(90)
                _endDate.value = now
            }
            DateRangePreset.THIS_YEAR -> {
                _startDate.value = now.withDayOfYear(1)
                _endDate.value = now
            }
            DateRangePreset.ALL_TIME -> {
                _startDate.value = LocalDate.of(2000, 1, 1)
                _endDate.value = now
            }
        }
    }
}

/**
 * UI state for analytics screen
 */
data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Predefined date range options
 */
enum class DateRangePreset(val label: String) {
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_30_DAYS("Last 30 Days"),
    LAST_90_DAYS("Last 90 Days"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time")
}
