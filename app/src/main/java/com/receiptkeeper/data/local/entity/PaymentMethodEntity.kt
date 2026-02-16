package com.receiptkeeper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for payment methods
 */
@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String, // e.g., "Chase Visa", "Cash", "Amex"

    val type: PaymentType, // Cash, CreditCard, DebitCard, Other

    val lastFourDigits: String? = null, // Last 4 digits for cards

    val createdAt: Instant = Instant.now()
)

enum class PaymentType {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    OTHER
}
