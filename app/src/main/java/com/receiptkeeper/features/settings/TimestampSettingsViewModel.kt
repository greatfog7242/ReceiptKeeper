package com.receiptkeeper.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.core.preferences.PreferencesManager
import com.receiptkeeper.data.repository.BookRepository
import com.receiptkeeper.domain.model.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimestampSettingsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val books: StateFlow<List<Book>> = bookRepository.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedBookId: StateFlow<Long?> = preferencesManager.timestampBookId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setTimestampBook(bookId: Long?) {
        viewModelScope.launch { preferencesManager.updateTimestampBookId(bookId) }
    }
}
