package com.receiptkeeper.features.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.data.repository.BookRepository
import com.receiptkeeper.domain.model.Book
import com.receiptkeeper.domain.model.BookWithReceiptCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * ViewModel for Books screen
 */
@HiltViewModel
class BooksViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BooksUiState())
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            bookRepository.getAllBooksSortedByReceiptCount()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load books"
                        )
                    }
                }
                .collect { booksWithCount ->
                    _uiState.update {
                        it.copy(
                            books = booksWithCount.map { it.book },
                            booksWithReceiptCount = booksWithCount,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun createBook(name: String, description: String?) {
        viewModelScope.launch {
            try {
                val book = Book(
                    name = name,
                    description = description,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                bookRepository.insertBook(book)
                _uiState.update { it.copy(showAddDialog = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to create book")
                }
            }
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            try {
                bookRepository.updateBook(book.copy(updatedAt = Instant.now()))
                _uiState.update { it.copy(editingBook = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to update book")
                }
            }
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            try {
                bookRepository.deleteBook(book)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to delete book")
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingBook = null) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun showEditDialog(book: Book) {
        _uiState.update { it.copy(editingBook = book, showAddDialog = false) }
    }

    fun hideEditDialog() {
        _uiState.update { it.copy(editingBook = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun reorderBooks(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            try {
                val currentBooks = _uiState.value.books.toMutableList()
                if (fromIndex < 0 || fromIndex >= currentBooks.size || 
                    toIndex < 0 || toIndex >= currentBooks.size) {
                    return@launch
                }

                // Reorder the list
                val bookToMove = currentBooks.removeAt(fromIndex)
                currentBooks.add(toIndex, bookToMove)

                // Update displayOrder for all books
                val booksWithNewOrder = currentBooks.mapIndexed { index, book ->
                    book.copy(displayOrder = index)
                }

                // Update in repository
                bookRepository.updateBooksDisplayOrder(booksWithNewOrder)

                // Update UI state
                _uiState.update { state ->
                    state.copy(
                        books = booksWithNewOrder,
                        booksWithReceiptCount = state.booksWithReceiptCount.map { bookWithCount ->
                            val updatedBook = booksWithNewOrder.find { it.id == bookWithCount.book.id }
                            if (updatedBook != null) {
                                bookWithCount.copy(book = updatedBook)
                            } else {
                                bookWithCount
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to reorder books")
                }
            }
        }
    }
}

/**
 * UI state for Books screen
 */
data class BooksUiState(
    val books: List<Book> = emptyList(),
    val booksWithReceiptCount: List<BookWithReceiptCount> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val editingBook: Book? = null
)
