package com.receiptkeeper.core.di

import android.content.Context
import androidx.room.Room
import com.receiptkeeper.core.database.ReceiptDatabase
import com.receiptkeeper.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Hilt module for providing Room database and DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideReceiptDatabase(
        @ApplicationContext context: Context,
        categoryDaoProvider: Provider<CategoryDao>
    ): ReceiptDatabase {
        return Room.databaseBuilder(
            context,
            ReceiptDatabase::class.java,
            ReceiptDatabase.DATABASE_NAME
        )
            .addCallback(ReceiptDatabase.createCallback(categoryDaoProvider))
            .fallbackToDestructiveMigration() // For development only - remove in production
            .build()
    }

    @Provides
    @Singleton
    fun provideBookDao(database: ReceiptDatabase): BookDao {
        return database.bookDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: ReceiptDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideVendorDao(database: ReceiptDatabase): VendorDao {
        return database.vendorDao()
    }

    @Provides
    @Singleton
    fun providePaymentMethodDao(database: ReceiptDatabase): PaymentMethodDao {
        return database.paymentMethodDao()
    }

    @Provides
    @Singleton
    fun provideSpendingGoalDao(database: ReceiptDatabase): SpendingGoalDao {
        return database.spendingGoalDao()
    }

    @Provides
    @Singleton
    fun provideReceiptDao(database: ReceiptDatabase): ReceiptDao {
        return database.receiptDao()
    }
}
