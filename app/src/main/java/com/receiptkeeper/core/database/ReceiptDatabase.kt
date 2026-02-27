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
import androidx.room.migration.Migration

/**
 * Room Database for ReceiptKeeper
 * Version 2: Added iconName to categories and vendors
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
    version = 2,
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
         * Migration from version 1 to 2: Add iconName column to categories and vendors
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add iconName column to categories table
                db.execSQL("ALTER TABLE categories ADD COLUMN iconName TEXT DEFAULT 'Category' NOT NULL")

                // Add iconName column to vendors table
                db.execSQL("ALTER TABLE vendors ADD COLUMN iconName TEXT DEFAULT 'Store' NOT NULL")
            }
        }

        /**
         * Get all migrations
         */
        val MIGRATIONS = arrayOf(MIGRATION_1_2)

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
         * Seeds 8 default categories with predefined colors and icons
         */
        private suspend fun seedDefaultCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                CategoryEntity(
                    name = "Food",
                    colorHex = "#FF6B6B",
                    iconName = "Restaurant",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Grocery",
                    colorHex = "#4ECB71",
                    iconName = "LocalGroceryStore",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Hardware",
                    colorHex = "#FFA500",
                    iconName = "Build",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Entertainment",
                    colorHex = "#E91E63",
                    iconName = "Movie",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Transportation",
                    colorHex = "#3498DB",
                    iconName = "DirectionsCar",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Utilities",
                    colorHex = "#9B59B6",
                    iconName = "Power",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Healthcare",
                    colorHex = "#1ABC9C",
                    iconName = "LocalHospital",
                    isDefault = true,
                    createdAt = Instant.now()
                ),
                CategoryEntity(
                    name = "Other",
                    colorHex = "#95A5A6",
                    iconName = "Category",
                    isDefault = true,
                    createdAt = Instant.now()
                )
            )

            categoryDao.insertCategories(defaultCategories)
        }
    }
}
