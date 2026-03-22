package com.receiptkeeper.features.settings

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.core.preferences.PreferencesManager
import com.receiptkeeper.data.repository.BookRepository
import com.receiptkeeper.domain.model.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bouncycastle.tsp.TimeStampRequest
import org.bouncycastle.tsp.TimeStampResponse
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipFile
import javax.inject.Inject

data class EvidencePackage(
    val path: String,
    val fileName: String,
    val sizeKb: Long,
    val lastModified: Long
)

data class VerificationResult(
    val packageName: String,
    val imageHashOk: Boolean?,   // null = no image in package
    val canonicalHashOk: Boolean,
    val tsqMatchesHash: Boolean,
    val tsrMatchesTsq: Boolean,
    val certifiedAt: String?,
    val error: String?
) {
    val allPassed: Boolean get() =
        (imageHashOk == null || imageHashOk) &&
        canonicalHashOk && tsqMatchesHash && tsrMatchesTsq
}

@HiltViewModel
class TimestampSettingsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val books: StateFlow<List<Book>> = bookRepository.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedBookId: StateFlow<Long?> = preferencesManager.timestampBookId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _evidencePackages = MutableStateFlow<List<EvidencePackage>>(emptyList())
    val evidencePackages: StateFlow<List<EvidencePackage>> = _evidencePackages.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    private val _verificationResult = MutableStateFlow<VerificationResult?>(null)
    val verificationResult: StateFlow<VerificationResult?> = _verificationResult.asStateFlow()

    private val _packageToDelete = MutableStateFlow<EvidencePackage?>(null)
    val packageToDelete: StateFlow<EvidencePackage?> = _packageToDelete.asStateFlow()

    fun setTimestampBook(bookId: Long?) {
        viewModelScope.launch { preferencesManager.updateTimestampBookId(bookId) }
    }

    fun loadEvidencePackages() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "雪松堡收据"
                )
                val packages = if (dir.exists()) {
                    dir.listFiles { f -> f.isFile && f.name.endsWith(".zip") }
                        ?.map { f ->
                            EvidencePackage(
                                path = f.absolutePath,
                                fileName = f.name,
                                sizeKb = f.length() / 1024,
                                lastModified = f.lastModified()
                            )
                        }
                        ?.sortedByDescending { it.lastModified }
                        ?: emptyList()
                } else emptyList()
                _evidencePackages.value = packages
            }
        }
    }

    fun showDeleteConfirmation(pkg: EvidencePackage) {
        _packageToDelete.value = pkg
    }

    fun hideDeleteConfirmation() {
        _packageToDelete.value = null
    }

    fun confirmDelete() {
        val pkg = _packageToDelete.value ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { File(pkg.path).delete() }
            _packageToDelete.value = null
            loadEvidencePackages()
        }
    }

    fun verifyEvidencePackage(pkg: EvidencePackage) {
        viewModelScope.launch {
            _isVerifying.value = true
            _verificationResult.value = null
            val result = withContext(Dispatchers.IO) { runVerification(pkg) }
            _isVerifying.value = false
            _verificationResult.value = result
        }
    }

    fun dismissVerificationResult() {
        _verificationResult.value = null
    }

    private fun runVerification(pkg: EvidencePackage): VerificationResult {
        return try {
            ZipFile(pkg.path).use { zip ->
                val manifestBytes = zip.getInputStream(zip.getEntry("manifest.json")).readBytes()
                val tsqEntry = zip.getEntry("timestamp_query.tsq")
                val tsrEntry = zip.getEntry("timestamp_proof.tsr")
                val imageEntry = zip.getEntry("receipt_image.jpg")

                if (tsqEntry == null || tsrEntry == null) {
                    return VerificationResult(
                        packageName = pkg.fileName,
                        imageHashOk = null,
                        canonicalHashOk = false,
                        tsqMatchesHash = false,
                        tsrMatchesTsq = false,
                        certifiedAt = null,
                        error = "Missing timestamp_query.tsq or timestamp_proof.tsr in package"
                    )
                }

                val tsqBytes = zip.getInputStream(tsqEntry).readBytes()
                val tsrBytes = zip.getInputStream(tsrEntry).readBytes()
                val imageBytes = imageEntry?.let { zip.getInputStream(it).readBytes() }

                // --- 3rd-party reconstruction ---
                // Trust nothing pre-computed. Rebuild everything from raw package contents.

                val manifest = JSONObject(String(manifestBytes))
                val data = manifest.getJSONObject("data")
                val hashes = manifest.getJSONObject("hashes")
                val expectedImageHash = hashes.getString("image_sha256")

                // 1. Independently compute image SHA-256 from the image file in the ZIP.
                //    Compare it to what the manifest recorded.
                val computedImageHash = imageBytes?.let {
                    MessageDigest.getInstance("SHA-256").digest(it)
                        .joinToString("") { b -> "%02x".format(b) }
                }
                val imageHashOk = if (computedImageHash != null && expectedImageHash != "no-image") {
                    computedImageHash == expectedImageHash
                } else null

                // 2. Reconstruct the canonical string from raw data fields in manifest.json,
                //    applying the same fallback rules used at export time.
                //    Use the independently computed image hash (or "no-image").
                val imageHashForCanonical = computedImageHash ?: "no-image"
                val vendorField   = data.getString("vendor")  .ifEmpty { "no-vendor" }
                val categoryField = data.getString("category").ifEmpty { "no-category" }
                val paymentField  = data.getString("payment_method").ifEmpty { "no-payment" }
                val bookField     = data.getString("book")    .ifEmpty { "no-book" }
                val notesField    = data.getString("notes")
                val reconstructedCanonical =
                    "${data.getString("receipt_id")}|${data.getString("amount")}" +
                    "|${data.getString("date_on_receipt")}|${data.getString("updated_at_utc")}" +
                    "|${vendorField}|${categoryField}|${paymentField}|${bookField}" +
                    "|${notesField}|${imageHashForCanonical}"

                val reconstructedDigest = MessageDigest.getInstance("SHA-256")
                    .digest(reconstructedCanonical.toByteArray())
                val reconstructedHash = reconstructedDigest.joinToString("") { "%02x".format(it) }

                // canonicalHashOk: reconstructed hash matches what the manifest recorded
                val canonicalHashOk = reconstructedHash == hashes.getString("manifest_data_sha256")

                // 3. TSQ message imprint must equal the reconstructed hash
                val tsq = TimeStampRequest(tsqBytes)
                val tsqHash = tsq.messageImprintDigest.joinToString("") { "%02x".format(it) }
                val tsqMatchesHash = tsqHash == reconstructedHash

                // 4. TSR validates against a fresh TSQ built from the reconstructed hash
                val freshTsq = org.bouncycastle.tsp.TimeStampRequestGenerator()
                    .generate(org.bouncycastle.tsp.TSPAlgorithms.SHA256, reconstructedDigest)
                val tsr = TimeStampResponse(tsrBytes)
                var certifiedAt: String? = null
                var tsrError: String? = null
                val tsrMatchesTsq = try {
                    tsr.validate(freshTsq)
                    val genTime = tsr.timeStampToken.timeStampInfo.genTime
                    certifiedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US).format(genTime)
                    true
                } catch (e: Exception) {
                    tsrError = e.message
                    false
                }

                VerificationResult(
                    packageName = pkg.fileName,
                    imageHashOk = imageHashOk,
                    canonicalHashOk = canonicalHashOk,
                    tsqMatchesHash = tsqMatchesHash,
                    tsrMatchesTsq = tsrMatchesTsq,
                    certifiedAt = certifiedAt,
                    error = tsrError
                )
            }
        } catch (e: Exception) {
            VerificationResult(
                packageName = pkg.fileName,
                imageHashOk = null,
                canonicalHashOk = false,
                tsqMatchesHash = false,
                tsrMatchesTsq = false,
                certifiedAt = null,
                error = e.message ?: "Verification failed"
            )
        }
    }
}
