package com.receiptkeeper.domain.model

import java.time.Instant

/**
 * Domain model for Vendor
 */
data class Vendor(
    val id: Long = 0,
    val name: String,
    val iconName: String = "Store",
    val createdAt: Instant = Instant.now()
)
