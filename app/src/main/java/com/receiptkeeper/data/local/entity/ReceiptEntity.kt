package com.receiptkeeper.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/**
 * Room entity for receipts (main entity)
 * Contains foreign keys to books, vendors, categories, and payment methods
 */
@Entity(
    tableName = "receipts",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE // Delete receipt when book is deleted
        ),
        ForeignKey(
            entity = VendorEntity::class,
            parentColumns = ["id"],
            childColumns = ["vendorId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = PaymentMethodEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentMethodId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["bookId"]),
        Index(value = ["vendorId"]),
        Index(value = ["categoryId"]),
        Index(value = ["paymentMethodId"]),
        Index(value = ["transactionDate"]) // For date range queries
    ]
)
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val bookId: Long, // Required: every receipt belongs to a book

    val vendorId: Long? = null,

    val categoryId: Long? = null,

    val paymentMethodId: Long? = null,

    val totalAmount: Double,

    val transactionDate: LocalDate,

    val notes: String? = null,

    val imageUri: String? = null, // File path to saved image

    val extractedText: String? = null, // Raw OCR text for debugging

    val createdAt: Instant = Instant.now(),

    val updatedAt: Instant = Instant.now()
)
