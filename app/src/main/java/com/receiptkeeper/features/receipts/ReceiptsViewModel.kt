package com.receiptkeeper.features.receipts

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.core.util.ImageHandler
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
    private val imageHandler: ImageHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptsUiState())
    val uiState: StateFlow<ReceiptsUiState> = _uiState.asStateFlow()

    init {
        loadData()
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
                    val filteredReceipts = if (_uiState.value.selectedBookFilter != null) {
                        data.receipts.filter { it.bookId == _uiState.value.selectedBookFilter }
                    } else {
                        data.receipts
                    }

                    val totalSpending = filteredReceipts.sumOf { it.totalAmount }

                    _uiState.update {
                        it.copy(
                            receipts = filteredReceipts,
                            allReceipts = data.receipts,
                            books = data.books,
                            vendors = data.vendors,
                            categories = data.categories,
                            paymentMethods = data.paymentMethods,
                            totalSpending = totalSpending,
                            isLoading = false,
                            error = null
                        )
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
        imageUri: Uri?
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
                    extractedText = null,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                receiptRepository.insertReceipt(receipt)
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
                    extractedText = null,
                    createdAt = Instant.now(), // Will be ignored by update
                    updatedAt = Instant.now()
                )
                receiptRepository.updateReceipt(updated)
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
        viewModelScope.launch {
            _uiState.update { state ->
                val filteredReceipts = if (bookId != null) {
                    state.allReceipts.filter { it.bookId == bookId }
                } else {
                    state.allReceipts
                }

                val totalSpending = filteredReceipts.sumOf { it.totalAmount }

                state.copy(
                    selectedBookFilter = bookId,
                    receipts = filteredReceipts,
                    totalSpending = totalSpending
                )
            }
        }
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
    val selectedBookFilter: Long? = null
)

private data class ReceiptsData(
    val receipts: List<Receipt>,
    val books: List<Book>,
    val vendors: List<Vendor>,
    val categories: List<Category>,
    val paymentMethods: List<PaymentMethod>
)
