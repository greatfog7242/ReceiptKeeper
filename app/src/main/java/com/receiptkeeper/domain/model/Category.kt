package com.receiptkeeper.domain.model

import java.time.Instant

/**
 * Domain model for Category
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val createdAt: Instant = Instant.now()
)
