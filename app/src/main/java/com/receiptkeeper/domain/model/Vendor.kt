package com.receiptkeeper.domain.model

import java.time.Instant

/**
 * Domain model for Vendor
 */
data class Vendor(
    val id: Long = 0,
    val name: String,
    val createdAt: Instant = Instant.now()
)
