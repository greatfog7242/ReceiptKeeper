package com.receiptkeeper.data.repository

import com.receiptkeeper.data.local.dao.CategoryDao
import com.receiptkeeper.data.local.dao.ReceiptDao
import com.receiptkeeper.data.local.entity.CategorySpending
import com.receiptkeeper.data.local.entity.DailySpending
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

    /**
     * Get daily accumulated spending for trend analysis
     * Calculates accumulation and fills missing dates
     */
    fun getDailyAccumulatedSpending(
        startDate: LocalDate,
        endDate: LocalDate,
        bookId: Long? = null
    ): Flow<List<DailySpending>> {
        return receiptDao.getDailySpending(startDate, endDate, bookId)
            .map { dailySpendingList ->
                // Create a map of date -> daily spending for quick lookup
                val spendingMap = dailySpendingList.associateBy { it.date }

                // Generate all dates in range and calculate accumulated totals
                var currentDate = startDate
                var accumulatedTotal = 0.0
                val result = mutableListOf<DailySpending>()

                while (!currentDate.isAfter(endDate)) {
                    val dailySpending = spendingMap[currentDate]
                    val dailyTotal = dailySpending?.dailyTotal ?: 0.0
                    accumulatedTotal += dailyTotal

                    result.add(DailySpending(
                        date = currentDate,
                        dailyTotal = dailyTotal,
                        accumulatedTotal = accumulatedTotal
                    ))

                    currentDate = currentDate.plusDays(1)
                }

                result
            }
    }
}
