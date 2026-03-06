package com.receiptkeeper.features.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptkeeper.domain.model.Book
import com.receiptkeeper.domain.model.BookWithReceiptCount
import com.receiptkeeper.features.books.components.BookCard
import com.receiptkeeper.features.books.components.BookDialog
import com.receiptkeeper.features.books.components.DeleteBookDialog

/**
 * Books screen - displays all receipt books/folders in a grid
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    onNavigateToBookDetail: (Long) -> Unit = {},
    viewModel: BooksViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var bookToDelete by remember { mutableStateOf<Book?>(null) }
    var isReorderMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Books") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (uiState.booksWithReceiptCount.size > 1) {
                        IconButton(
                            onClick = { isReorderMode = !isReorderMode }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reorder,
                                contentDescription = if (isReorderMode) "Exit reorder mode" else "Reorder books",
                                tint = if (isReorderMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add book")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.books.isEmpty() -> {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No books yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create a book to organize your receipts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.showAddDialog() }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Book")
                        }
                    }
                }

                else -> {
                    if (isReorderMode) {
                        // Reorder mode - list view with reorder buttons
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(uiState.booksWithReceiptCount, key = { _, item -> item.book.id }) { index, bookWithCount ->
                                Column {
                                    BookCard(
                                        book = bookWithCount.book,
                                        receiptCount = bookWithCount.receiptCount,
                                        onBookClick = { onNavigateToBookDetail(it.id) },
                                        onEditClick = { viewModel.showEditDialog(it) },
                                        onDeleteClick = { bookToDelete = it }
                                    )
                                    
                                    // Reorder buttons for this book
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        // Move up button
                                        if (index > 0) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.reorderBooks(index, index - 1)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowUpward,
                                                    contentDescription = "Move up",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        
                                        // Move down button  
                                        if (index < uiState.booksWithReceiptCount.size - 1) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.reorderBooks(index, index + 1)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDownward,
                                                    contentDescription = "Move down",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Normal mode - grid view
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.booksWithReceiptCount, key = { it.book.id }) { bookWithCount ->
                                BookCard(
                                    book = bookWithCount.book,
                                    receiptCount = bookWithCount.receiptCount,
                                    onBookClick = { onNavigateToBookDetail(it.id) },
                                    onEditClick = { viewModel.showEditDialog(it) },
                                    onDeleteClick = { bookToDelete = it }
                                )
                            }
                        }
                    }
                }
            }

            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }

    // Add/Edit dialog
    if (uiState.showAddDialog) {
        BookDialog(
            book = null,
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { name, description ->
                viewModel.createBook(name, description)
            }
        )
    }

    // Edit dialog
    uiState.editingBook?.let { book ->
        BookDialog(
            book = book,
            onDismiss = { viewModel.hideEditDialog() },
            onConfirm = { name, description ->
                viewModel.updateBook(
                    book.copy(
                        name = name,
                        description = description
                    )
                )
            }
        )
    }

    // Delete confirmation dialog
    bookToDelete?.let { book ->
        DeleteBookDialog(
            bookName = book.name,
            onDismiss = { bookToDelete = null },
            onConfirm = {
                viewModel.deleteBook(book)
                bookToDelete = null
            }
        )
    }
}
