package com.receiptkeeper.data.local.dao

import androidx.room.*
import com.receiptkeeper.data.local.entity.CategorySpending
import com.receiptkeeper.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * DAO for Receipt operations with complex queries
 */
@Dao
interface ReceiptDao {

    @Query("SELECT * FROM receipts ORDER BY transactionDate DESC, createdAt DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE id = :receiptId")
    fun getReceiptById(receiptId: Long): Flow<ReceiptEntity?>

    @Query("SELECT * FROM receipts WHERE bookId = :bookId ORDER BY transactionDate DESC")
    fun getReceiptsByBook(bookId: Long): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE categoryId = :categoryId ORDER BY transactionDate DESC")
    fun getReceiptsByCategory(categoryId: Long): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE vendorId = :vendorId ORDER BY transactionDate DESC")
    fun getReceiptsByVendor(vendorId: Long): Flow<List<ReceiptEntity>>

    @Query("""
        SELECT * FROM receipts
        WHERE transactionDate BETWEEN :startDate AND :endDate
        ORDER BY transactionDate DESC
    """)
    fun getReceiptsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<ReceiptEntity>>

    @Query("""
        SELECT * FROM receipts
        WHERE categoryId = :categoryId
        AND transactionDate BETWEEN :startDate AND :endDate
        ORDER BY transactionDate DESC
    """)
    fun getReceiptsByCategoryAndDateRange(
        categoryId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<ReceiptEntity>>

    @Query("SELECT SUM(totalAmount) FROM receipts")
    fun getTotalSpending(): Flow<Double?>

    @Query("SELECT SUM(totalAmount) FROM receipts WHERE bookId = :bookId")
    fun getTotalSpendingByBook(bookId: Long): Flow<Double?>

    @Query("""
        SELECT SUM(totalAmount) FROM receipts
        WHERE transactionDate BETWEEN :startDate AND :endDate
    """)
    fun getTotalSpendingByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<Double?>

    @Query("""
        SELECT SUM(totalAmount) FROM receipts
        WHERE categoryId = :categoryId
        AND transactionDate BETWEEN :startDate AND :endDate
    """)
    fun getTotalSpendingByCategoryAndDateRange(
        categoryId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<Double?>

    @Query("""
        SELECT categoryId, SUM(totalAmount) as total
        FROM receipts
        WHERE transactionDate BETWEEN :startDate AND :endDate
        AND categoryId IS NOT NULL
        GROUP BY categoryId
    """)
    fun getCategorySpendingBreakdown(startDate: LocalDate, endDate: LocalDate): Flow<List<CategorySpending>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE id = :receiptId")
    suspend fun deleteReceiptById(receiptId: Long)

    @Query("DELETE FROM receipts WHERE bookId = :bookId")
    suspend fun deleteReceiptsByBook(bookId: Long)
}
