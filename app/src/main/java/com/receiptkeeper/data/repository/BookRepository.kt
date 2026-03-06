package com.receiptkeeper.data.repository

import com.receiptkeeper.data.local.dao.BookDao
import com.receiptkeeper.data.local.dao.ReceiptDao
import com.receiptkeeper.data.mapper.toDomain
import com.receiptkeeper.data.mapper.toEntity
import com.receiptkeeper.domain.model.Book
import com.receiptkeeper.domain.model.BookWithReceiptCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Book operations
 */
@Singleton
class BookRepository @Inject constructor(
    private val bookDao: BookDao,
    private val receiptDao: ReceiptDao
) {
    fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getBookById(bookId: Long): Flow<Book?> {
        return bookDao.getBookById(bookId).map { it?.toDomain() }
    }

    suspend fun getBookByName(name: String): Book? {
        return bookDao.getBookByName(name)?.toDomain()
    }

    suspend fun insertBook(book: Book): Long {
        return bookDao.insertBook(book.toEntity())
    }

    suspend fun updateBook(book: Book) {
        bookDao.updateBook(book.copy(updatedAt = Instant.now()).toEntity())
    }

    suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book.toEntity())
    }

    suspend fun deleteBookById(bookId: Long) {
        bookDao.deleteBookById(bookId)
    }

    suspend fun updateBookDisplayOrder(bookId: Long, displayOrder: Int) {
        bookDao.updateBookDisplayOrder(bookId, displayOrder)
    }

    suspend fun updateBooksDisplayOrder(books: List<Book>) {
        books.forEachIndexed { index, book ->
            bookDao.updateBookDisplayOrder(book.id, index)
        }
    }

    fun getAllBooksWithReceiptCount(): Flow<List<BookWithReceiptCount>> {
        return bookDao.getAllBooks().map { bookEntities ->
            bookEntities.map { bookEntity ->
                val book = bookEntity.toDomain()
                BookWithReceiptCount(
                    book = book,
                    receiptCount = 0 // Will be populated by combine
                )
            }
        }
    }

    fun getAllBooksSortedByReceiptCount(): Flow<List<BookWithReceiptCount>> {
        return combine(
            bookDao.getAllBooks(),
            receiptDao.getAllReceipts()
        ) { bookEntities, receiptEntities ->
            // Create a map of bookId -> receipt count
            val receiptCountByBookId = receiptEntities
                .groupBy { it.bookId }
                .mapValues { (_, receipts) -> receipts.size }

            // Create BookWithReceiptCount objects
            bookEntities.map { bookEntity ->
                val book = bookEntity.toDomain()
                val receiptCount = receiptCountByBookId[book.id] ?: 0
                BookWithReceiptCount(book, receiptCount)
            }.sortedByDescending { it.receiptCount } // Sort by receipt count descending
        }
    }
}
