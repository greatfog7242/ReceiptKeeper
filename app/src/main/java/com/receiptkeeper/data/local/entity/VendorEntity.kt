package com.receiptkeeper.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for store/merchant names
 */
@Entity(
    tableName = "vendors",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class VendorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val iconName: String = "Store", // Material icon name

    val createdAt: Instant = Instant.now()
)
