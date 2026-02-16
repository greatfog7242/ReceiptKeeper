package com.receiptkeeper.data.repository

import com.receiptkeeper.data.local.dao.ReceiptDao
import com.receiptkeeper.data.mapper.toDomain
import com.receiptkeeper.data.mapper.toEntity
import com.receiptkeeper.domain.model.Receipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Receipt operations
 * Core business logic for receipt management
 */
@Singleton
class ReceiptRepository @Inject constructor(
    private val receiptDao: ReceiptDao
) {
    fun getAllReceipts(): Flow<List<Receipt>> {
        return receiptDao.getAllReceipts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getReceiptById(receiptId: Long): Flow<Receipt?> {
        return receiptDao.getReceiptById(receiptId).map { it?.toDomain() }
    }

    fun getReceiptsByBook(bookId: Long): Flow<List<Receipt>> {
        return receiptDao.getReceiptsByBook(bookId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getReceiptsByCategory(categoryId: Long): Flow<List<Receipt>> {
        return receiptDao.getReceiptsByCategory(categoryId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getReceiptsByVendor(vendorId: Long): Flow<List<Receipt>> {
        return receiptDao.getReceiptsByVendor(vendorId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getReceiptsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Receipt>> {
        return receiptDao.getReceiptsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getReceiptsByCategoryAndDateRange(
        categoryId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Receipt>> {
        return receiptDao.getReceiptsByCategoryAndDateRange(categoryId, startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getTotalSpending(): Flow<Double> {
        return receiptDao.getTotalSpending().map { it ?: 0.0 }
    }

    fun getTotalSpendingByBook(bookId: Long): Flow<Double> {
        return receiptDao.getTotalSpendingByBook(bookId).map { it ?: 0.0 }
    }

    fun getTotalSpendingByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Double> {
        return receiptDao.getTotalSpendingByDateRange(startDate, endDate).map { it ?: 0.0 }
    }

    fun getTotalSpendingByCategoryAndDateRange(
        categoryId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Double> {
        return receiptDao.getTotalSpendingByCategoryAndDateRange(categoryId, startDate, endDate).map { it ?: 0.0 }
    }

    fun getCategorySpendingBreakdown(startDate: LocalDate, endDate: LocalDate): Flow<Map<Long, Double>> {
        return receiptDao.getCategorySpendingBreakdown(startDate, endDate).map { list ->
            list.associate { it.categoryId to it.total }
        }
    }

    suspend fun insertReceipt(receipt: Receipt): Long {
        return receiptDao.insertReceipt(receipt.toEntity())
    }

    suspend fun updateReceipt(receipt: Receipt) {
        receiptDao.updateReceipt(receipt.copy(updatedAt = Instant.now()).toEntity())
    }

    suspend fun deleteReceipt(receipt: Receipt) {
        receiptDao.deleteReceipt(receipt.toEntity())
    }

    suspend fun deleteReceiptById(receiptId: Long) {
        receiptDao.deleteReceiptById(receiptId)
    }

    suspend fun deleteReceiptsByBook(bookId: Long) {
        receiptDao.deleteReceiptsByBook(bookId)
    }
}
