package com.receiptkeeper.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Domain model for Receipt
 *
 * [totalAmount] is always the USD-equivalent amount used for calculations.
 * [originalAmount] is the amount in the original [currency] (e.g. CNY).
 * When [currency] == "USD", [originalAmount] == [totalAmount].
 */
data class Receipt(
    val id: Long = 0,
    val bookId: Long,
    val vendorId: Long? = null,
    val categoryId: Long? = null,
    val paymentMethodId: Long? = null,
    val totalAmount: Double,
    val currency: String = "USD",
    val originalAmount: Double = totalAmount,
    val transactionDate: LocalDate,
    val notes: String? = null,
    val imageUri: String? = null,
    val extractedText: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val tsrToken: ByteArray? = null
)
