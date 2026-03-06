package com.receiptkeeper.features.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptkeeper.features.receipts.components.ReceiptListItemSimple
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Book detail screen - shows receipts in a specific book
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToReceiptDetail: (Long) -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val book by viewModel.book.collectAsState()
    val receipts by viewModel.receipts.collectAsState()
    val totalSpending by viewModel.totalSpending.collectAsState()
    val vendors by viewModel.vendors.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()

    // Load book data
    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    // Group receipts by date and track expanded/collapsed state
    val receiptsByDate = receipts
        .groupBy { it.transactionDate }
        .toSortedMap(compareByDescending<LocalDate> { it })

    // Initially collapse all dates
    var expandedDates by remember(receiptsByDate.keys) {
        mutableStateOf<Set<LocalDate>>(emptySet())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.name ?: "Book Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Book info and total spending card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    book?.description?.let { description ->
                        if (description.isNotBlank()) {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Text(
                        text = "Total Spending",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$${"%.2f".format(totalSpending)}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${receipts.size} receipts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Receipts list
            if (receipts.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No receipts in this book yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan or add receipts to track spending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                // Receipts list grouped by date
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    receiptsByDate.forEach { (date, receiptsForDate) ->
                        val isExpanded = expandedDates.contains(date)
                        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
                        val dateTotal = receiptsForDate.sumOf { it.totalAmount }

                        // Date header
                        item(key = "header_$date") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedDates = if (isExpanded) {
                                            expandedDates - date
                                        } else {
                                            expandedDates + date
                                        }
                                    },
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = date.format(dateFormatter),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "$${"%.2f".format(dateTotal)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Receipts for this date
                        if (isExpanded) {
                            items(receiptsForDate, key = { it.id }) { receipt ->
                                val vendor = vendors.find { it.id == receipt.vendorId }
                                val category = categories.find { it.id == receipt.categoryId }
                                val currentBook = book

                                ReceiptListItemSimple(
                                    receipt = receipt,
                                    vendorName = vendor?.name ?: "Unknown",
                                    vendorIconName = vendor?.iconName ?: "Store",
                                    categoryName = category?.name ?: "Uncategorized",
                                    categoryColor = category?.colorHex ?: "#808080",
                                    categoryIconName = category?.iconName ?: "Category",
                                    bookName = currentBook?.name ?: "",
                                    onItemClick = { onNavigateToReceiptDetail(receipt.id) },
                                    onImageClick = { /* Not needed in detail view */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
