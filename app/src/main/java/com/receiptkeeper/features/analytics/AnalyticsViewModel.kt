package com.receiptkeeper.features.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.core.preferences.PreferencesManager
import com.receiptkeeper.data.local.entity.CategorySpending
import com.receiptkeeper.data.local.entity.VendorSpending
import com.receiptkeeper.data.local.entity.DailySpending
import com.receiptkeeper.data.repository.*
import com.receiptkeeper.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * ViewModel for analytics screen
 * Manages date range selection and spending calculations
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val spendingGoalRepository: SpendingGoalRepository,
    private val vendorRepository: VendorRepository,
    private val bookRepository: BookRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    // Date range - default to current month
    private val _startDate = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
    private val _endDate = MutableStateFlow(LocalDate.now())
    // Book selection - null means "All Books"
    private val _selectedBookId = MutableStateFlow<Long?>(null)

    val startDate: StateFlow<LocalDate> = _startDate.asStateFlow()
    val endDate: StateFlow<LocalDate> = _endDate.asStateFlow()
    val selectedBookId: StateFlow<Long?> = _selectedBookId.asStateFlow()

    // Receipts for selected date range and book
    val receipts: StateFlow<List<Receipt>> = combine(
        _startDate,
        _endDate,
        _selectedBookId
    ) { start, end, bookId ->
        Triple(start, end, bookId)
    }.flatMapLatest { (start, end, bookId) ->
        analyticsRepository.getReceiptsByDateRange(start, end, bookId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total spending for selected date range and book
    val totalSpending: StateFlow<Double> = combine(
        _startDate,
        _endDate,
        _selectedBookId
    ) { start, end, bookId ->
        Triple(start, end, bookId)
    }.flatMapLatest { (start, end, bookId) ->
        analyticsRepository.getTotalSpendingByDateRange(start, end, bookId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Category spending breakdown for selected date range and book
    val categoryBreakdown: StateFlow<List<CategorySpending>> = combine(
        _startDate,
        _endDate,
        _selectedBookId
    ) { start, end, bookId ->
        Triple(start, end, bookId)
    }.flatMapLatest { (start, end, bookId) ->
        analyticsRepository.getCategorySpendingBreakdown(start, end, bookId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Vendor spending breakdown for selected date range and book
    val vendorBreakdown: StateFlow<List<VendorSpending>> = combine(
        _startDate,
        _endDate,
        _selectedBookId
    ) { start, end, bookId ->
        Triple(start, end, bookId)
    }.flatMapLatest { (start, end, bookId) ->
        analyticsRepository.getVendorSpendingBreakdown(start, end, bookId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All categories for reference
    val categories: StateFlow<List<Category>> = analyticsRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All spending goals
    val spendingGoals: StateFlow<List<SpendingGoal>> = spendingGoalRepository.getAllSpendingGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Data for CSV export
    val vendors: StateFlow<List<Vendor>> = vendorRepository.getAllVendors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val books: StateFlow<List<Book>> = bookRepository.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentMethods: StateFlow<List<PaymentMethod>> = paymentMethodRepository.getAllPaymentMethods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Treemap threshold from preferences
    val treemapThreshold: StateFlow<Double> = preferencesManager.treemapThreshold
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5.0)

    // Treemap target aspect ratio from preferences
    val treemapAspectRatio: StateFlow<Double> = preferencesManager.treemapAspectRatio
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0)

    // Daily accumulated spending for trend chart
    val dailyAccumulatedSpending: StateFlow<List<DailySpending>> = combine(
        _startDate,
        _endDate,
        _selectedBookId
    ) { start, end, bookId ->
        Triple(start, end, bookId)
    }.flatMapLatest { (start, end, bookId) ->
        analyticsRepository.getDailyAccumulatedSpending(start, end, bookId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    /**
     * Set selected book for filtering analytics
     */
    fun setSelectedBook(bookId: Long?) {
        _selectedBookId.value = bookId
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
