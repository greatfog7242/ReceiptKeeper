package com.receiptkeeper.core.services

import android.util.Log
import com.receiptkeeper.core.di.IoDispatcher
import com.receiptkeeper.core.preferences.PreferencesManager
import com.receiptkeeper.data.repository.BookRepository
import com.receiptkeeper.data.repository.CategoryRepository
import com.receiptkeeper.data.repository.PaymentMethodRepository
import com.receiptkeeper.data.repository.ReceiptRepository
import com.receiptkeeper.data.repository.VendorRepository
import com.receiptkeeper.domain.model.Receipt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.bouncycastle.tsp.TSPAlgorithms
import org.bouncycastle.tsp.TimeStampRequestGenerator
import org.bouncycastle.tsp.TimeStampResponse
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RfcTimestampService @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val receiptRepository: ReceiptRepository,
    private val bookRepository: BookRepository,
    private val vendorRepository: VendorRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun stampIfEnabled(receipt: Receipt, receiptId: Long) {
        withContext(ioDispatcher) {
            val bookId = preferencesManager.timestampBookId.first()
            Log.d("RfcTimestamp", "stampIfEnabled: receiptId=$receiptId bookId=${receipt.bookId} selectedBook=$bookId")
            if (bookId == null) { Log.d("RfcTimestamp", "No book selected, skipping"); return@withContext }
            if (receipt.bookId != bookId) { Log.d("RfcTimestamp", "Receipt book ${receipt.bookId} != selected $bookId, skipping"); return@withContext }
            try {
                // Resolve human-readable names from IDs
                val vendorName = receipt.vendorId?.let {
                    vendorRepository.getVendorById(it).first()?.name
                } ?: "no-vendor"
                val categoryName = receipt.categoryId?.let {
                    categoryRepository.getCategoryById(it).first()?.name
                } ?: "no-category"
                val paymentMethodName = receipt.paymentMethodId?.let {
                    paymentMethodRepository.getPaymentMethodById(it).first()?.name
                } ?: "no-payment"
                val bookName = bookRepository.getBookById(receipt.bookId).first()?.name ?: "no-book"
                val notes = receipt.notes ?: ""

                // Hash the receipt image file if present
                val imageSha256 = receipt.imageUri?.let { uri ->
                    runCatching {
                        val bytes = File(uri).readBytes()
                        MessageDigest.getInstance("SHA-256").digest(bytes)
                            .joinToString("") { "%02x".format(it) }
                    }.getOrNull()
                } ?: "no-image"

                // Canonical hash input — all fields, UTC timestamps
                val input = "${receiptId}|${receipt.totalAmount}|${receipt.transactionDate}" +
                        "|${receipt.updatedAt}|${vendorName}|${categoryName}" +
                        "|${paymentMethodName}|${bookName}|${notes}|${imageSha256}"
                Log.d("RfcTimestamp", "Hash input: $input")

                val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
                val gen = TimeStampRequestGenerator()
                val request = gen.generate(TSPAlgorithms.SHA256, digest)
                val url = URL("https://freetsa.org/tsr")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/timestamp-query")
                conn.outputStream.write(request.encoded)
                val responseCode = conn.responseCode
                Log.d("RfcTimestamp", "TSA HTTP $responseCode")
                val responseBytes = if (responseCode == 200) {
                    conn.inputStream.readBytes()
                } else {
                    val err = conn.errorStream?.readBytes()?.toString(Charsets.UTF_8) ?: ""
                    Log.e("RfcTimestamp", "TSA error body: $err")
                    return@withContext
                }
                Log.d("RfcTimestamp", "TSA response ${responseBytes.size} bytes")
                TimeStampResponse(responseBytes)
                receiptRepository.updateTsrToken(receiptId, responseBytes)
                Log.d("RfcTimestamp", "TSR stored for receipt $receiptId")
            } catch (e: Exception) {
                Log.e("RfcTimestamp", "Stamp failed: ${e.javaClass.simpleName}: ${e.message}", e)
            }
        }
    }
}
