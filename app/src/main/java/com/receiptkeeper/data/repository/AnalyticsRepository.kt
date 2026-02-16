package com.receiptkeeper.data.repository

import com.receiptkeeper.data.local.dao.CategoryDao
import com.receiptkeeper.data.local.dao.ReceiptDao
import com.receiptkeeper.data.local.entity.CategorySpending
import com.receiptkeeper.data.mapper.toDomain
import com.receiptkeeper.domain.model.Category
import com.receiptkeeper.domain.model.Receipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for analytics and reporting operations
 */
@Singleton
class AnalyticsRepository @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val categoryDao: CategoryDao
) {

    /**
     * Get receipts within a date range
     */
    fun getReceiptsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Receipt>> {
        return receiptDao.getReceiptsByDateRange(startDate, endDate)
            .map { entities -> entities.map { it.toDomain() } }
    }

    /**
     * Get total spending for a date range
     */
    fun getTotalSpendingByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Double> {
        return receiptDao.getTotalSpendingByDateRange(startDate, endDate)
            .map { it ?: 0.0 }
    }

    /**
     * Get spending breakdown by category for a date range
     */
    fun getCategorySpendingBreakdown(startDate: LocalDate, endDate: LocalDate): Flow<List<CategorySpending>> {
        return receiptDao.getCategorySpendingBreakdown(startDate, endDate)
    }

    /**
     * Get spending for a specific category in a date range
     */
    fun getTotalSpendingByCategoryAndDateRange(
        categoryId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Double> {
        return receiptDao.getTotalSpendingByCategoryAndDateRange(categoryId, startDate, endDate)
            .map { it ?: 0.0 }
    }

    /**
     * Get all categories for reference
     */
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
            .map { entities -> entities.map { it.toDomain() } }
    }
}
