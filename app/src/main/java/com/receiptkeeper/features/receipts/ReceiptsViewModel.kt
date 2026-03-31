package com.receiptkeeper.features.receipts

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.core.preferences.PreferencesManager
import com.receiptkeeper.core.util.ImageHandler
import com.receiptkeeper.data.repository.BookRepository
import com.receiptkeeper.data.repository.CategoryRepository
import com.receiptkeeper.core.services.RfcTimestampService
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ReceiptsViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val bookRepository: BookRepository,
    private val vendorRepository: VendorRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val imageHandler: ImageHandler,
    private val rfcTimestampService: RfcTimestampService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptsUiState())
    val uiState: StateFlow<ReceiptsUiState> = _uiState.asStateFlow()

    val defaultBookId: StateFlow<Long> = bookRepository.getAllBooksSortedByReceiptCount()
        .map { it.firstOrNull()?.book?.id ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _suggestedCategoryId = MutableStateFlow<Long?>(null)
    val suggestedCategoryId: StateFlow<Long?> = _suggestedCategoryId.asStateFlow()

    fun onVendorSelected(vendorName: String) {
        viewModelScope.launch {
            val vendor = vendorRepository.getVendorByName(vendorName)
            _suggestedCategoryId.value = vendor?.let {
                receiptRepository.getMostPopularCategoryForVendor(it.id)
            }
        }
    }

    fun clearSuggestedCategory() {
        _suggestedCategoryId.value = null
    }

    init {
        loadData()
        loadTimestampBookId()
    }

    private fun loadTimestampBookId() {
        viewModelScope.launch {
            preferencesManager.timestampBookId.collect { bookId ->
                _uiState.update { it.copy(timestampBookId = bookId) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Combine all data streams
            combine(
                receiptRepository.getAllReceipts(),
                bookRepository.getAllBooks(),
                vendorRepository.getAllVendors(),
                categoryRepository.getAllCategories(),
                paymentMethodRepository.getAllPaymentMethods()
            ) { receipts, books, vendors, categories, paymentMethods ->
                ReceiptsData(receipts, books, vendors, categories, paymentMethods)
            }
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load receipts"
                        )
                    }
                }
                .collect { data ->
                    _uiState.update { currentState ->
                        val newState = currentState.copy(
                            allReceipts = data.receipts,
                            books = data.books,
                            vendors = data.vendors,
                            categories = data.categories,
                            paymentMethods = data.paymentMethods,
                            isLoading = false,
                            error = null
                        )
                        applyFilters(newState)
                    }
                }
        }
    }

    fun createReceipt(
        bookId: Long,
        vendorName: String,
        categoryId: Long,
        paymentMethodId: Long?,
        totalAmount: Double,
        transactionDate: LocalDate,
        notes: String?,
        imageUri: Uri?,
        extractedText: String?
    ) {
        viewModelScope.launch {
            try {
                // Get or create vendor (returns vendor ID)
                val vendorId = vendorRepository.getOrCreateVendor(vendorName)

                // Save image to app storage if provided (Uri.EMPTY means no image)
                val savedImagePath = if (imageUri != null && imageUri != Uri.EMPTY) {
                    imageHandler.saveImage(imageUri)
                } else {
                    null
                }

                val receipt = Receipt(
                    bookId = bookId,
                    vendorId = vendorId,
                    categoryId = categoryId,
                    paymentMethodId = paymentMethodId,
                    totalAmount = totalAmount,
                    transactionDate = transactionDate,
                    notes = notes,
                    imageUri = savedImagePath,
                    extractedText = extractedText,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                val savedId = receiptRepository.insertReceipt(receipt)
                viewModelScope.launch { rfcTimestampService.stampIfEnabled(receipt, savedId) }
                _uiState.update { it.copy(showAddDialog = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to create receipt")
                }
            }
        }
    }

    fun updateReceipt(receipt: Receipt) {
        viewModelScope.launch {
            try {
                val updated = receipt.copy(updatedAt = Instant.now())
                receiptRepository.updateReceipt(updated)
                _uiState.update { it.copy(editingReceipt = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update receipt")
                }
            }
        }
    }

    fun updateReceiptFromDialog(
        receiptId: Long,
        bookId: Long,
        vendorName: String,
        categoryId: Long,
        paymentMethodId: Long?,
        totalAmount: Double,
        transactionDate: LocalDate,
        notes: String?,
        oldImageUri: String?,
        newImageUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                // Get or create vendor (returns vendor ID)
                val vendorId = vendorRepository.getOrCreateVendor(vendorName)

                // Handle image update (Uri.EMPTY means user clicked remove)
                val finalImageUri = when {
                    newImageUri == Uri.EMPTY -> {
                        // User wants to remove image
                        if (oldImageUri != null) {
                            imageHandler.deleteImage(oldImageUri)
                        }
                        null
                    }
                    newImageUri != null -> {
                        // User selected new image
                        if (oldImageUri != null) {
                            imageHandler.deleteImage(oldImageUri)
                        }
                        imageHandler.saveImage(newImageUri)
                    }
                    else -> {
                        // Keep existing image
                        oldImageUri
                    }
                }

                val original = _uiState.value.editingReceipt
                val updated = Receipt(
                    id = receiptId,
                    bookId = bookId,
                    vendorId = vendorId,
                    categoryId = categoryId,
                    paymentMethodId = paymentMethodId,
                    totalAmount = totalAmount,
                    transactionDate = transactionDate,
                    notes = notes,
                    imageUri = finalImageUri,
                    extractedText = original?.extractedText,
                    createdAt = original?.createdAt ?: Instant.now(),
                    updatedAt = Instant.now()
                )
                receiptRepository.updateReceipt(updated)
                viewModelScope.launch { rfcTimestampService.stampIfEnabled(updated, updated.id) }
                _uiState.update { it.copy(editingReceipt = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update receipt")
                }
            }
        }
    }

    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch {
            try {
                // Delete image file if exists
                if (receipt.imageUri != null) {
                    imageHandler.deleteImage(receipt.imageUri)
                }
                receiptRepository.deleteReceipt(receipt)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to delete receipt")
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingReceipt = null) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun showEditDialog(receipt: Receipt) {
        _uiState.update { it.copy(editingReceipt = receipt, showAddDialog = false) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(editingReceipt = null) }
    }

    fun setBookFilter(bookId: Long?) {
        _uiState.update { state -> applyFilters(state.copy(selectedBookFilter = bookId)) }
    }

    fun setVendorFilter(vendorId: Long?) {
        _uiState.update { state -> applyFilters(state.copy(selectedVendorFilter = vendorId)) }
    }

    fun setPaymentFilter(paymentMethodId: Long?) {
        _uiState.update { state -> applyFilters(state.copy(selectedPaymentFilter = paymentMethodId)) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state -> applyFilters(state.copy(searchQuery = query)) }
    }

    private fun applyFilters(state: ReceiptsUiState): ReceiptsUiState {
        var filtered = if (state.selectedBookFilter != null) {
            state.allReceipts.filter { it.bookId == state.selectedBookFilter }
        } else {
            state.allReceipts
        }
        if (state.selectedVendorFilter != null) {
            filtered = filtered.filter { it.vendorId == state.selectedVendorFilter }
        }
        if (state.selectedPaymentFilter != null) {
            filtered = filtered.filter { it.paymentMethodId == state.selectedPaymentFilter }
        }
        val query = state.searchQuery.trim().lowercase()
        val result = if (query.isEmpty()) {
            filtered
        } else {
            filtered.filter { receipt ->
                val vendor = state.vendors.find { it.id == receipt.vendorId }
                val category = state.categories.find { it.id == receipt.categoryId }
                val book = state.books.find { it.id == receipt.bookId }
                val paymentMethod = state.paymentMethods.find { it.id == receipt.paymentMethodId }
                listOfNotNull(
                    vendor?.name,
                    category?.name,
                    book?.name,
                    paymentMethod?.name,
                    receipt.notes,
                    receipt.extractedText,
                    receipt.totalAmount.toString(),
                    receipt.transactionDate.toString()
                ).any { it.lowercase().contains(query) }
            }
        }
        return state.copy(receipts = result, totalSpending = result.sumOf { it.totalAmount })
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ReceiptsUiState(
    val receipts: List<Receipt> = emptyList(),
    val allReceipts: List<Receipt> = emptyList(),
    val books: List<Book> = emptyList(),
    val vendors: List<Vendor> = emptyList(),
    val categories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val totalSpending: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val editingReceipt: Receipt? = null,
    val selectedBookFilter: Long? = null,
    val selectedVendorFilter: Long? = null,
    val selectedPaymentFilter: Long? = null,
    val searchQuery: String = "",
    val timestampBookId: Long? = null
)

private data class ReceiptsData(
    val receipts: List<Receipt>,
    val books: List<Book>,
    val vendors: List<Vendor>,
    val categories: List<Category>,
    val paymentMethods: List<PaymentMethod>
)
