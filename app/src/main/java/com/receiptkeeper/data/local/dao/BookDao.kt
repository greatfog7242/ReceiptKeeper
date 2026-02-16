package com.receiptkeeper.data.local.dao

import androidx.room.*
import com.receiptkeeper.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Book operations
 */
@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookById(bookId: Long): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE name = :name")
    suspend fun getBookByName(name: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: Long)
}
