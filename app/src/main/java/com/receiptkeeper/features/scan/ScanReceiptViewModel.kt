package com.receiptkeeper.features.scan

import android.app.Application
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
import com.receiptkeeper.features.scan.ocr.ExtractedReceiptData
import com.receiptkeeper.features.scan.ocr.OcrProcessor
import com.receiptkeeper.features.scan.ocr.ReceiptParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for scan receipt screen
 * Manages camera capture, OCR processing, and receipt creation workflow
 */
@HiltViewModel
class ScanReceiptViewModel @Inject constructor(
    private val ocrProcessor: OcrProcessor,
    private val imageHandler: ImageHandler,
    private val receiptRepository: ReceiptRepository,
    private val bookRepository: BookRepository,
    private val vendorRepository: VendorRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanReceiptUiState())
    val uiState: StateFlow<ScanReceiptUiState> = _uiState.asStateFlow()

    // Load reference data for dropdowns
    val books: StateFlow<List<Book>> = bookRepository.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentMethods: StateFlow<List<PaymentMethod>> = paymentMethodRepository.getAllPaymentMethods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendors: StateFlow<List<com.receiptkeeper.domain.model.Vendor>> = vendorRepository.getAllVendors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Called when image is captured from camera
     */
    fun onImageCaptured(imageUri: Uri) {
        viewModelScope.launch {
            // Save captured image to app storage
            val savedPath = imageHandler.saveImage(imageUri)

            if (savedPath != null) {
                _uiState.update {
                    it.copy(
                        capturedImageUri = Uri.parse(savedPath),
                        isScanning = false,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        error = "Failed to save image",
                        isScanning = true
                    )
                }
            }
        }
    }

    /**
     * Process OCR on captured image
     */
    fun processOcr() {
        val imageUri = _uiState.value.capturedImageUri ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }

            val result = ocrProcessor.processImage(imageUri, application)

            if (result.success) {
                val extracted = ReceiptParser.parseReceipt(result.fullText)
                _uiState.update {
                    it.copy(
                        extractedData = extracted,
                        isProcessing = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = result.error ?: "OCR processing failed"
                    )
                }
            }
        }
    }

    /**
     * Update individual extracted field
     */
    fun updateVendor(value: String) {
        _uiState.update { state ->
            state.copy(
                extractedData = state.extractedData?.copy(vendor = value)
            )
        }
    }

    fun updateAmount(value: String) {
        val amount = value.toDoubleOrNull()
        _uiState.update { state ->
            state.copy(
                extractedData = state.extractedData?.copy(amount = amount)
            )
        }
    }

    fun updateDate(value: LocalDate?) {
        _uiState.update { state ->
            state.copy(
                extractedData = state.extractedData?.copy(date = value)
            )
        }
    }

    fun updateCardLast4(value: String) {
        _uiState.update { state ->
            state.copy(
                extractedData = state.extractedData?.copy(cardLast4 = value)
            )
        }
    }

    /**
     * Save receipt with extracted/edited data
     */
    fun saveReceipt(
        bookId: Long,
        categoryId: Long,
        paymentMethodId: Long?,
        notes: String,
        onSuccess: (Long) -> Unit
    ) {
        val state = _uiState.value
        val extracted = state.extractedData
        val imageUri = state.capturedImageUri

        if (extracted == null || imageUri == null) {
            _uiState.update { it.copy(error = "Missing receipt data") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isProcessing = true, error = null) }

                // Get or create vendor
                val vendorId = if (!extracted.vendor.isNullOrBlank()) {
                    vendorRepository.getOrCreateVendor(extracted.vendor)
                } else {
                    null
                }

                // Create receipt
                val receipt = com.receiptkeeper.domain.model.Receipt(
                    bookId = bookId,
                    vendorId = vendorId,
                    categoryId = categoryId,
                    paymentMethodId = paymentMethodId,
                    totalAmount = extracted.amount ?: 0.0,
                    transactionDate = extracted.date ?: LocalDate.now(),
                    notes = notes.ifBlank { null },
                    imageUri = imageUri.toString(),
                    extractedText = extracted.fullText
                )

                val receiptId = receiptRepository.insertReceipt(receipt)

                _uiState.update { it.copy(isProcessing = false) }
                onSuccess(receiptId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = "Failed to save receipt: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Retry camera capture
     */
    fun retryCapture() {
        _uiState.update { ScanReceiptUiState(isScanning = true) }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Skip OCR and go directly to manual entry
     */
    fun skipOcr() {
        val imageUri = _uiState.value.capturedImageUri ?: return

        _uiState.update {
            it.copy(
                extractedData = ExtractedReceiptData(
                    vendor = null,
                    date = LocalDate.now(),
                    amount = null,
                    cardLast4 = null,
                    fullText = ""
                )
            )
        }
    }
}

/**
 * UI state for scan receipt screen
 */
data class ScanReceiptUiState(
    val isScanning: Boolean = true,
    val isProcessing: Boolean = false,
    val capturedImageUri: Uri? = null,
    val extractedData: ExtractedReceiptData? = null,
    val error: String? = null
)
