package com.receiptkeeper.features.receipts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.data.repository.BookRepository
import com.receiptkeeper.data.repository.CategoryRepository
import com.receiptkeeper.data.repository.PaymentMethodRepository
import com.receiptkeeper.data.repository.ReceiptRepository
import com.receiptkeeper.data.repository.VendorRepository
import com.receiptkeeper.domain.model.Book
import com.receiptkeeper.domain.model.Category
import com.receiptkeeper.domain.model.PaymentMethod
import com.receiptkeeper.domain.model.Receipt
import com.receiptkeeper.domain.model.Vendor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptDetailUiState(
    val receipt: Receipt? = null,
    val vendor: Vendor? = null,
    val category: Category? = null,
    val paymentMethod: PaymentMethod? = null,
    val book: Book? = null,
    val allVendors: List<Vendor> = emptyList(),
    val allCategories: List<Category> = emptyList(),
    val allPaymentMethods: List<PaymentMethod> = emptyList(),
    val allBooks: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val receiptRepository: ReceiptRepository,
    private val bookRepository: BookRepository,
    private val vendorRepository: VendorRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val receiptId: Long = checkNotNull(savedStateHandle["receiptId"])

    private val _uiState = MutableStateFlow(ReceiptDetailUiState())
    val uiState: StateFlow<ReceiptDetailUiState> = _uiState.asStateFlow()

    init {
        loadReceipt()
    }

    private fun loadReceipt() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                combine(
                    receiptRepository.getReceiptById(receiptId),
                    bookRepository.getAllBooks(),
                    vendorRepository.getAllVendors(),
                    categoryRepository.getAllCategories(),
                    paymentMethodRepository.getAllPaymentMethods()
                ) { receipt, books, vendors, categories, paymentMethods ->
                    val vendor = receipt?.vendorId?.let { vendorId ->
                        vendors.find { it.id == vendorId }
                    }
                    val category = receipt?.categoryId?.let { categoryId ->
                        categories.find { it.id == categoryId }
                    }
                    val paymentMethod = receipt?.paymentMethodId?.let { pmId ->
                        paymentMethods.find { it.id == pmId }
                    }
                    val book = receipt?.bookId?.let { bookId ->
                        books.find { it.id == bookId }
                    }
                    ReceiptDetailUiState(
                        receipt = receipt,
                        vendor = vendor,
                        category = category,
                        paymentMethod = paymentMethod,
                        book = book,
                        allVendors = vendors,
                        allCategories = categories,
                        allPaymentMethods = paymentMethods,
                        allBooks = books,
                        isLoading = false,
                        error = null
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load receipt"
                    )
                }
            }
        }
    }

    fun deleteReceipt(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.value.receipt?.let { receipt ->
                try {
                    receiptRepository.deleteReceipt(receipt)
                    onDeleted()
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(error = e.message ?: "Failed to delete receipt")
                    }
                }
            }
        }
    }
}
