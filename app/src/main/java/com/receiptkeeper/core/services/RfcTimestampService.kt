package com.receiptkeeper.core.services

import com.receiptkeeper.core.di.IoDispatcher
import com.receiptkeeper.core.preferences.PreferencesManager
import com.receiptkeeper.data.repository.ReceiptRepository
import com.receiptkeeper.domain.model.Receipt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
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
            val bookId = preferencesManager.timestampBookId.first() ?: return@withContext
            if (receipt.bookId != bookId) return@withContext
            try {
                val input = "${receiptId}|${receipt.totalAmount}|${receipt.transactionDate}|${receipt.updatedAt}"
                val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
                val gen = TimeStampRequestGenerator()
                val request = gen.generate(TSPAlgorithms.SHA256, digest)
                val url = URL("https://timestamp.digicert.com")
                val conn = url.openConnection() as HttpURLConnection
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/timestamp-query")
                conn.outputStream.write(request.encoded)
                val responseBytes = conn.inputStream.readBytes()
                // Validate we got a valid TSR before storing
                TimeStampResponse(responseBytes)
                receiptRepository.updateTsrToken(receiptId, responseBytes)
            } catch (_: Exception) {
                // Silent — do not surface network failures to the user
            }
        }
    }
}
