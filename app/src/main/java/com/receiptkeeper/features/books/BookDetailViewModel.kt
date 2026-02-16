package com.receiptkeeper.features.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.data.repository.*
import com.receiptkeeper.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * ViewModel for book detail screen
 * Shows receipts in a specific book with total spending
 */
@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val receiptRepository: ReceiptRepository,
    private val vendorRepository: VendorRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val _bookId = MutableStateFlow<Long?>(null)

    val book: StateFlow<Book?> = _bookId
        .filterNotNull()
        .flatMapLatest { bookId ->
            bookRepository.getBookById(bookId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val receipts: StateFlow<List<Receipt>> = _bookId
        .filterNotNull()
        .flatMapLatest { bookId ->
            receiptRepository.getReceiptsByBook(bookId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpending: StateFlow<Double> = _bookId
        .filterNotNull()
        .flatMapLatest { bookId ->
            receiptRepository.getTotalSpendingByBook(bookId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val vendors: StateFlow<List<Vendor>> = vendorRepository.getAllVendors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentMethods: StateFlow<List<PaymentMethod>> = paymentMethodRepository.getAllPaymentMethods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadBook(bookId: Long) {
        _bookId.value = bookId
    }
}
