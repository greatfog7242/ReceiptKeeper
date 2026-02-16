package com.receiptkeeper.domain.model

import com.receiptkeeper.data.local.entity.PaymentType
import java.time.Instant

/**
 * Domain model for PaymentMethod
 */
data class PaymentMethod(
    val id: Long = 0,
    val name: String,
    val type: PaymentType,
    val lastFourDigits: String? = null,
    val createdAt: Instant = Instant.now()
)
