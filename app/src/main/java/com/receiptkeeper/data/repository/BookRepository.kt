package com.receiptkeeper.data.repository

import com.receiptkeeper.data.local.dao.BookDao
import com.receiptkeeper.data.mapper.toDomain
import com.receiptkeeper.data.mapper.toEntity
import com.receiptkeeper.domain.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Book operations
 */
@Singleton
class BookRepository @Inject constructor(
    private val bookDao: BookDao
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
}
