package com.receiptkeeper.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Domain model for Receipt
 */
data class Receipt(
    val id: Long = 0,
    val bookId: Long,
    val vendorId: Long? = null,
    val categoryId: Long? = null,
    val paymentMethodId: Long? = null,
    val totalAmount: Double,
    val transactionDate: LocalDate,
    val notes: String? = null,
    val imageUri: String? = null,
    val extractedText: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
