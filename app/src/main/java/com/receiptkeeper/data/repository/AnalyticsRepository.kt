package com.receiptkeeper.data.repository

import com.receiptkeeper.data.local.dao.CategoryDao
import com.receiptkeeper.data.local.dao.ReceiptDao
import com.receiptkeeper.data.local.entity.CategorySpending
import com.receiptkeeper.data.local.entity.VendorSpending
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
     * Get receipts within a date range, optionally filtered by book
     */
    fun getReceiptsByDateRange(startDate: LocalDate, endDate: LocalDate, bookId: Long? = null): Flow<List<Receipt>> {
        return if (bookId != null) {
            receiptDao.getReceiptsByBookAndDateRange(bookId, startDate, endDate)
                .map { entities -> entities.map { it.toDomain() } }
        } else {
            receiptDao.getReceiptsByDateRange(startDate, endDate)
                .map { entities -> entities.map { it.toDomain() } }
        }
    }

    /**
     * Get total spending for a date range, optionally filtered by book
     */
    fun getTotalSpendingByDateRange(startDate: LocalDate, endDate: LocalDate, bookId: Long? = null): Flow<Double> {
        return if (bookId != null) {
            receiptDao.getTotalSpendingByBookAndDateRange(bookId, startDate, endDate)
                .map { it ?: 0.0 }
        } else {
            receiptDao.getTotalSpendingByDateRange(startDate, endDate)
                .map { it ?: 0.0 }
        }
    }

    /**
     * Get spending breakdown by category for a date range, optionally filtered by book
     */
    fun getCategorySpendingBreakdown(startDate: LocalDate, endDate: LocalDate, bookId: Long? = null): Flow<List<CategorySpending>> {
        return if (bookId != null) {
            receiptDao.getCategorySpendingBreakdownByBook(bookId, startDate, endDate)
        } else {
            receiptDao.getCategorySpendingBreakdown(startDate, endDate)
        }
    }

    /**
     * Get spending breakdown by vendor for a date range, optionally filtered by book
     */
    fun getVendorSpendingBreakdown(startDate: LocalDate, endDate: LocalDate, bookId: Long? = null): Flow<List<VendorSpending>> {
        return if (bookId != null) {
            receiptDao.getVendorSpendingBreakdownByBook(bookId, startDate, endDate)
        } else {
            receiptDao.getVendorSpendingBreakdown(startDate, endDate)
        }
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
