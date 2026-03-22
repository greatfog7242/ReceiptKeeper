package com.receiptkeeper.core.services

import com.receiptkeeper.core.di.IoDispatcher
import com.receiptkeeper.core.preferences.PreferencesManager
import com.receiptkeeper.data.repository.ReceiptRepository
import com.receiptkeeper.domain.model.Receipt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import android.util.Log
import org.bouncycastle.tsp.TSPAlgorithms
import org.bouncycastle.tsp.TimeStampRequestGenerator
import org.bouncycastle.tsp.TimeStampResponse
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RfcTimestampService @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val receiptRepository: ReceiptRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun stampIfEnabled(receipt: Receipt, receiptId: Long) {
        withContext(ioDispatcher) {
            val bookId = preferencesManager.timestampBookId.first()
            Log.d("RfcTimestamp", "stampIfEnabled: receiptId=$receiptId bookId=${receipt.bookId} selectedBook=$bookId")
            if (bookId == null) { Log.d("RfcTimestamp", "No book selected, skipping"); return@withContext }
            if (receipt.bookId != bookId) { Log.d("RfcTimestamp", "Receipt book ${receipt.bookId} != selected $bookId, skipping"); return@withContext }
            try {
                val input = "${receiptId}|${receipt.totalAmount}|${receipt.transactionDate}|${receipt.updatedAt}"
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
