package com.receiptkeeper.domain.model

/**
 * Domain model for Book with receipt count
 */
data class BookWithReceiptCount(
    val book: Book,
    val receiptCount: Int
)