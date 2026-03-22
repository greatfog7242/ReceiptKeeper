package com.receiptkeeper.features.receipts

import android.content.Context
import android.os.Environment
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit
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
    val exportError: String? = null,
    val exportSuccess: String? = null
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

    fun clearExportSuccess() { _uiState.update { it.copy(exportSuccess = null) } }
    fun clearExportError() { _uiState.update { it.copy(exportError = null) } }

    fun exportProofPackage() {
        val state = _uiState.value
        val receipt = state.receipt ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }
            try {
                withContext(Dispatchers.IO) {
                    // Step 1: read image bytes (if present)
                    val imageBytes = receipt.imageUri?.let { uri ->
                        File(uri).takeIf { it.exists() }?.readBytes()
                    }
                    val imageSha256 = imageBytes?.let {
                        MessageDigest.getInstance("SHA-256").digest(it)
                            .joinToString("") { b -> "%02x".format(b) }
                    } ?: "no-image"

                    // Step 2: build manifest data fields — these are the source of truth.
                    // Storing them first ensures the canonical string is built from
                    // exactly the same values that the verifier will read from manifest.json.
                    val dataObj = JSONObject().apply {
                        put("receipt_id", receipt.id.toString())
                        put("vendor", state.vendor?.name ?: "")
                        put("category", state.category?.name ?: "")
                        put("payment_method", state.paymentMethod?.name ?: "")
                        put("book", state.book?.name ?: "")
                        put("amount", "%.2f".format(receipt.totalAmount))
                        put("date_on_receipt", receipt.transactionDate.toString())
                        put("updated_at_utc", receipt.updatedAt.truncatedTo(ChronoUnit.MILLIS).toString())
                        put("notes", receipt.notes ?: "")
                    }

                    // Step 3: construct canonical string by reading back from dataObj,
                    // applying the same fallback rules as the verifier does.
                    val vendorField  = dataObj.getString("vendor").ifEmpty { "no-vendor" }
                    val catField     = dataObj.getString("category").ifEmpty { "no-category" }
                    val payField     = dataObj.getString("payment_method").ifEmpty { "no-payment" }
                    val bookField    = dataObj.getString("book").ifEmpty { "no-book" }
                    val canonicalString =
                        "${dataObj.getString("receipt_id")}|${dataObj.getString("amount")}" +
                        "|${dataObj.getString("date_on_receipt")}|${dataObj.getString("updated_at_utc")}" +
                        "|${vendorField}|${catField}|${payField}|${bookField}" +
                        "|${dataObj.getString("notes")}|${imageSha256}"

                    // Step 4: hash the canonical string
                    val canonicalDigest = MessageDigest.getInstance("SHA-256")
                        .digest(canonicalString.toByteArray())
                    val manifestDataSha256 = canonicalDigest.joinToString("") { "%02x".format(it) }

                    // Step 5: generate TSQ and get a fresh TSR from TSA
                    val tsqBytes = TimeStampRequestGenerator()
                        .generate(TSPAlgorithms.SHA256, canonicalDigest).encoded
                    val conn = (URL("https://freetsa.org/tsr").openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15_000
                        readTimeout = 15_000
                        doOutput = true
                        setRequestProperty("Content-Type", "application/timestamp-query")
                    }
                    conn.outputStream.write(tsqBytes)
                    val responseCode = conn.responseCode
                    if (responseCode != 200) {
                        val err = conn.errorStream?.readBytes()?.toString(Charsets.UTF_8) ?: ""
                        throw Exception("TSA returned HTTP $responseCode: $err")
                    }
                    val tsrBytes = conn.inputStream.readBytes()
                    TimeStampResponse(tsrBytes) // validate parse

                    // Step 6: assemble manifest.json with hashes
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

                    // Step 7: write ZIP
                    val destDir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "雪松堡收据"
                    ).also { it.mkdirs() }
                    val zipFile = File(destDir, "Receipt_Evidence_${receipt.id}.zip")
                    ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
                        zip.putNextEntry(ZipEntry("manifest.json"))
                        zip.write(manifestJson.toByteArray())
                        zip.closeEntry()

                        zip.putNextEntry(ZipEntry("timestamp_proof.tsr"))
                        zip.write(tsrBytes)
                        zip.closeEntry()

                        zip.putNextEntry(ZipEntry("timestamp_query.tsq"))
                        zip.write(tsqBytes)
                        zip.closeEntry()

                        zip.putNextEntry(ZipEntry("README.txt"))
                        zip.write(buildReadme().toByteArray())
                        zip.closeEntry()

                        imageBytes?.let {
                            zip.putNextEntry(ZipEntry("receipt_image.jpg"))
                            zip.write(it)
                            zip.closeEntry()
                        }
                    }

                    zipFile.absolutePath
                }
                _uiState.update { it.copy(exportSuccess = "Saved to Downloads/雪松堡收据/${receipt.id}") }
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

This package contains four verifiable components:
  receipt_image.jpg    - The original receipt photo
  manifest.json        - Receipt metadata and hashes
  timestamp_query.tsq  - The exact query sent to the TSA (binary, RFC 3161)
  timestamp_proof.tsr  - The signed timestamp response from FreeTSA (binary, RFC 3161)

== Step 1: Verify Image Integrity ==

  sha256sum receipt_image.jpg

  -> Must match 'hashes.image_sha256' in manifest.json.
     If the receipt has no image, the value will be the literal string "no-image".

== Step 2: Independently Reconstruct the Canonical String ==

The canonical string is the exact UTF-8 string that was SHA-256 hashed and
submitted to the TSA. An auditor can rebuild it from scratch using the
following rules, without trusting manifest.json at all.

  FIELD ORDER (10 fields, zero-indexed):
    0  Receipt ID          integer (as printed in the app)
    1  Total Amount        Fixed two decimal places: String.format("%.2f", amount)
                           e.g. "12.50", "100.00" — always two digits after the point
    2  Receipt Date        ISO-8601 local date: YYYY-MM-DD
    3  Last-Updated UTC    ISO-8601 instant, e.g. "2026-03-22T00:02:10.123456789Z"
    4  Vendor Name         raw string; literal "no-vendor" if none assigned
    5  Category Name       raw string; literal "no-category" if none assigned
    6  Payment Method      raw string; literal "no-payment" if none assigned
    7  Book Name           raw string; literal "no-book" if none assigned
    8  Notes               raw string; empty string "" if no notes
    9  Image SHA-256       lowercase hex; literal "no-image" if no photo

  SEPARATOR: pipe character "|" with no surrounding spaces
  ENCODING:  UTF-8, no BOM
  TEMPLATE:
    {0}|{1}|{2}|{3}|{4}|{5}|{6}|{7}|{8}|{9}

  EXAMPLE:
    42|18.50|2026-03-20|2026-03-22T00:02:10.123Z|Walmart|Food|Visa|Expenses||a3f1...

  VERIFICATION:
    echo -n "<reconstructed_string>" | sha256sum
    -> Must match 'hashes.manifest_data_sha256' in manifest.json.
    -> Must also match the digest embedded in timestamp_query.tsq (see Step 4).

== Step 3: Verify the Timestamp Signature ==

The .tsr file is the TSA's cryptographic proof. Verify it against the .tsq
so you don't need to trust the manifest_data_sha256 value either:

  Download FreeTSA's CA cert:
    curl -O https://freetsa.org/files/tsa.crt

  Verify:
    openssl ts -verify -in timestamp_proof.tsr -queryfile timestamp_query.tsq -CAfile tsa.crt

  A successful result prints: "Verification: OK"

== Step 4: Inspect the TSQ Independently ==

To confirm the query itself contains the expected digest:

  openssl ts -query -in timestamp_query.tsq -text

  -> "Message Imprint" must show SHA-256 and match 'hashes.manifest_data_sha256'.

== Summary: Full Audit Chain ==

  Receipt fields  ->  reconstruct canonical string  (Step 2)
  canonical string  ->  SHA-256 digest              (Step 2)
  digest  ->  embedded in timestamp_query.tsq       (Step 4)
  timestamp_query.tsq + timestamp_proof.tsr  ->  TSA signature valid  (Step 3)

If all four steps pass independently, the receipt data is proven to have
existed in its current form at the time recorded in the TSR.

== Notes ==
- The timestamp certifies the state of the receipt at the moment it was saved.
- If the receipt was edited after the initial save, the canonical string
  reconstructed from the current UI values will differ from what was timestamped.
  The TSR is still valid — it proves the original save-time data.
    """.trimIndent()
}
