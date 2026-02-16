package com.receiptkeeper.domain.model

import java.time.Instant

/**
 * Domain model for Book
 */
data class Book(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
