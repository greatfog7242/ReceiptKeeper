package com.receiptkeeper.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.receiptkeeper.data.local.dao.*
import com.receiptkeeper.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Provider

/**
 * Room Database for ReceiptKeeper
 * Version 1: Initial schema with all entities
 */
@Database(
    entities = [
        BookEntity::class,
        CategoryEntity::class,
        VendorEntity::class,
        PaymentMethodEntity::class,
        SpendingGoalEntity::class,
        ReceiptEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ReceiptDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun categoryDao(): CategoryDao
    abstract fun vendorDao(): VendorDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun spendingGoalDao(): SpendingGoalDao
    abstract fun receiptDao(): ReceiptDao

    companion object {
        const val DATABASE_NAME = "receipt_keeper_db"

        /**
         * Database callback to seed default categories on first creation
         */
        fun createCallback(categoryDaoProvider: Provider<CategoryDao>): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    // Seed default categories on database creation
                    CoroutineScope(Dispatchers.IO).launch {
                        val categoryDao = categoryDaoProvider.get()
                        seedDefaultCategories(categoryDao)
                    }
                }
            }
        }

        /**
         * Seeds 8 default categories with predefined colors
         */
        private suspend fun seedDefaultCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                CategoryEntity(
                    name = "Food",
                    colorHex = "#FF6B6B",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Grocery",
                    colorHex = "#4ECB71",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Hardware",
                    colorHex = "#FFA500",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Entertainment",
                    colorHex = "#E91E63",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Transportation",
                    colorHex = "#3498DB",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Utilities",
                    colorHex = "#9B59B6",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Healthcare",
                    colorHex = "#1ABC9C",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Other",
                    colorHex = "#95A5A6",
                    isDefault = true,
                    createdAt = Instant.now()
                )
            )

            categoryDao.insertCategories(defaultCategories)
        }
    }
}
