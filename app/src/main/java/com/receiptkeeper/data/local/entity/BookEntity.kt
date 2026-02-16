package com.receiptkeeper.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for organizing receipts into books/folders
 */
@Entity(
    tableName = "books",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val description: String? = null,

    val createdAt: Instant = Instant.now(),

    val updatedAt: Instant = Instant.now()
)
