package com.receiptkeeper.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for expense categories
 * 8 default categories are seeded on first database creation
 */
@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val colorHex: String, // e.g., "#FF6B6B"

    val isDefault: Boolean = false, // true for pre-seeded categories

    val createdAt: Instant = Instant.now()
)
