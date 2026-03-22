package com.receiptkeeper.features.receipts

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bouncycastle.tsp.TSPAlgorithms
import org.bouncycastle.tsp.TimeStampRequestGenerator
import org.bouncycastle.tsp.TimeStampResponse
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
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
    val error: String? = null,
    val tsrCertifiedAt: Instant? = null,
    val isExporting: Boolean = false,
    val exportError: String? = null
)

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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

    private val _shareEvent = MutableSharedFlow<Intent>()
    val shareEvent: SharedFlow<Intent> = _shareEvent.asSharedFlow()

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
                    val tsrCertifiedAt = receipt?.tsrToken?.let { bytes ->
                        runCatching {
                            val resp = TimeStampResponse(bytes)
                            resp.timeStampToken.timeStampInfo.genTime.toInstant()
                        }.getOrNull()
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
                        error = null,
                        tsrCertifiedAt = tsrCertifiedAt
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

    fun exportProofPackage() {
        val state = _uiState.value
        val receipt = state.receipt ?: return
        if (receipt.tsrToken == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            try {
                val intent = withContext(Dispatchers.IO) {
                    // Hash image file if present
                    val imageSha256 = receipt.imageUri?.let { uri ->
                        runCatching {
                            val bytes = File(uri).readBytes()
                            MessageDigest.getInstance("SHA-256").digest(bytes)
                                .joinToString("") { "%02x".format(it) }
                        }.getOrNull()
                    } ?: "no-image"

                    // Reconstruct canonical string — same field order and fallbacks as RfcTimestampService
                    val vendorName = state.vendor?.name ?: "no-vendor"
                    val categoryName = state.category?.name ?: "no-category"
                    val paymentMethodName = state.paymentMethod?.name ?: "no-payment"
                    val bookName = state.book?.name ?: "no-book"
                    val notes = receipt.notes ?: ""
                    val canonicalString = "${receipt.id}|${receipt.totalAmount}|${receipt.transactionDate}" +
                            "|${receipt.updatedAt}|${vendorName}|${categoryName}" +
                            "|${paymentMethodName}|${bookName}|${notes}|${imageSha256}"

                    val canonicalDigest = MessageDigest.getInstance("SHA-256")
                        .digest(canonicalString.toByteArray())
                    val manifestDataSha256 = canonicalDigest.joinToString("") { "%02x".format(it) }

                    // Regenerate the TSQ from the same digest (no extra storage needed)
                    val tsqBytes = TimeStampRequestGenerator()
                        .generate(TSPAlgorithms.SHA256, canonicalDigest).encoded

                    // Build manifest.json
                    val dataObj = JSONObject().apply {
                        put("receipt_id", receipt.id.toString())
                        put("vendor", state.vendor?.name ?: "")
                        put("category", state.category?.name ?: "")
                        put("payment_method", state.paymentMethod?.name ?: "")
                        put("book", state.book?.name ?: "")
                        put("amount", receipt.totalAmount.toString())
                        put("date_on_receipt", receipt.transactionDate.toString())
                        put("updated_at_utc", receipt.updatedAt.toString())
                        put("notes", receipt.notes ?: "")
                    }
                    val hashesObj = JSONObject().apply {
                        put("image_sha256", imageSha256)
                        put("canonical_string", canonicalString)
                        put("manifest_data_sha256", manifestDataSha256)
                    }
                    val manifestJson = JSONObject().apply {
                        put("version", "1.0")
                        put("data", dataObj)
                        put("hashes", hashesObj)
                    }.toString(2)

                    val readme = buildReadme()

                    // Write ZIP to cache dir
                    val zipFile = File(context.cacheDir, "Receipt_Evidence_${receipt.id}.zip")
                    ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
                        zip.putNextEntry(ZipEntry("manifest.json"))
                        zip.write(manifestJson.toByteArray())
                        zip.closeEntry()

                        zip.putNextEntry(ZipEntry("timestamp_proof.tsr"))
                        zip.write(receipt.tsrToken)
                        zip.closeEntry()

                        zip.putNextEntry(ZipEntry("timestamp_query.tsq"))
                        zip.write(tsqBytes)
                        zip.closeEntry()

                        zip.putNextEntry(ZipEntry("README.txt"))
                        zip.write(readme.toByteArray())
                        zip.closeEntry()

                        receipt.imageUri?.let { uri ->
                            val imageFile = File(uri)
                            if (imageFile.exists()) {
                                zip.putNextEntry(ZipEntry("receipt_image.jpg"))
                                zip.write(imageFile.readBytes())
                                zip.closeEntry()
                            }
                        }
                    }

                    val zipUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        zipFile
                    )

                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/zip"
                        putExtra(Intent.EXTRA_STREAM, zipUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
                _shareEvent.emit(intent)
            } catch (e: Exception) {
                _uiState.update { it.copy(exportError = e.message ?: "Export failed") }
            } finally {
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    private fun buildReadme(): String = """
RFC 3161 Evidence Package - Verification Guide
===============================================

This package contains three verifiable components:
  receipt_image.jpg    - The original receipt photo
  manifest.json        - Metadata and hashes
  timestamp_proof.tsr  - Binary RFC 3161 timestamp response from FreeTSA

== Step 1: Verify Image Integrity ==
sha256sum receipt_image.jpg
  -> Must match 'hashes.image_sha256' in manifest.json

== Step 2: Verify Timestamp Input ==
The field 'hashes.canonical_string' in manifest.json is the exact string
that was SHA-256 hashed and sent to FreeTSA. Verify it:
  echo -n "<canonical_string>" | sha256sum
  -> Must match 'hashes.manifest_data_sha256' in manifest.json

== Step 3: Verify the Timestamp Signature ==
Download FreeTSA's CA cert: https://freetsa.org/files/tsa.crt
Then run:
  openssl ts -verify -in timestamp_proof.tsr \
    -digest <manifest_data_sha256_hex> \
    -CAfile tsa.crt

A successful result prints: "Verification: OK"

== Notes ==
- The timestamp certifies the state of the receipt at the moment it was saved.
- If the receipt was edited after saving, the canonical_string will differ
  from a fresh reconstruction, but the TSR proof is still valid for the
  original save-time data.
    """.trimIndent()
}
