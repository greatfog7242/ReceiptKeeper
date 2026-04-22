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
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Book detail screen - shows receipts in a specific book organised as a year/month/day tree
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

    // Load book data
    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    // Build year > month > date tree
    val receiptsByYear = receipts
        .groupBy { it.transactionDate.year }
        .toSortedMap(compareByDescending { it })
    val receiptsByYearMonth = receipts
        .groupBy { YearMonth.from(it.transactionDate) }
    val receiptsByDate = receipts
        .groupBy { it.transactionDate }

    // Reset expansion/selection when the set of receipts changes
    val receiptIds = receipts.map { it.id }.toSet()
    var expandedYears by remember(receiptIds) { mutableStateOf<Set<Int>>(emptySet()) }
    var expandedMonths by remember(receiptIds) { mutableStateOf<Set<YearMonth>>(emptySet()) }
    var expandedDates by remember(receiptIds) { mutableStateOf<Set<LocalDate>>(emptySet()) }
    var selectedNode by remember(receiptIds) { mutableStateOf<BookTreeSelection>(BookTreeSelection.None) }

    // Compute spending for the selected tree node
    val displayedSpending = when (val sel = selectedNode) {
        is BookTreeSelection.None -> totalSpending
        is BookTreeSelection.Year -> receiptsByYear[sel.year]?.sumOf { it.totalAmount } ?: 0.0
        is BookTreeSelection.Month -> receiptsByYearMonth[sel.yearMonth]?.sumOf { it.totalAmount } ?: 0.0
        is BookTreeSelection.Day -> receiptsByDate[sel.date]?.sumOf { it.totalAmount } ?: 0.0
    }
    val spendingLabel = when (val sel = selectedNode) {
        is BookTreeSelection.None -> "Total Spending"
        is BookTreeSelection.Year -> "${sel.year} Spending"
        is BookTreeSelection.Month -> "${sel.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${sel.yearMonth.year} Spending"
        is BookTreeSelection.Day -> sel.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) + " Spending"
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
            // Book description (only shown when non-empty)
            book?.description?.let { description ->
                if (description.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Spending card — updates based on selected tree node
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = spendingLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${receipts.size} receipts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = formatBookCurrency(displayedSpending),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Receipts list / empty state
            if (receipts.isEmpty()) {
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
                // Year > Month > Date tree
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    receiptsByYear.forEach { (year, receiptsForYear) ->
                        val isYearExpanded = expandedYears.contains(year)
                        val yearTotal = receiptsForYear.sumOf { it.totalAmount }
                        val isYearSelected = selectedNode == BookTreeSelection.Year(year)

                        // Year header
                        item(key = "year_$year") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedYears = if (isYearExpanded) expandedYears - year else expandedYears + year
                                        selectedNode = if (isYearSelected) BookTreeSelection.None else BookTreeSelection.Year(year)
                                    },
                                color = if (isYearSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
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
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isYearExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isYearExpanded) "Collapse" else "Expand",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = year.toString(),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Text(
                                        text = formatBookCurrency(yearTotal),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (isYearExpanded) {
                            val byMonth = receiptsForYear
                                .groupBy { YearMonth.from(it.transactionDate) }
                                .toSortedMap(compareByDescending { it })

                            byMonth.forEach { (yearMonth, receiptsForMonth) ->
                                val isMonthExpanded = expandedMonths.contains(yearMonth)
                                val monthTotal = receiptsForMonth.sumOf { it.totalAmount }
                                val isMonthSelected = selectedNode == BookTreeSelection.Month(yearMonth)
                                val monthName = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

                                // Month header
                                item(key = "month_$yearMonth") {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedMonths = if (isMonthExpanded) expandedMonths - yearMonth else expandedMonths + yearMonth
                                                selectedNode = if (isMonthSelected) BookTreeSelection.None else BookTreeSelection.Month(yearMonth)
                                            },
                                        color = if (isMonthSelected)
                                            MaterialTheme.colorScheme.surfaceVariant
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 32.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isMonthExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = if (isMonthExpanded) "Collapse" else "Expand",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = monthName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = formatBookCurrency(monthTotal),
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                if (isMonthExpanded) {
                                    val byDate = receiptsForMonth
                                        .groupBy { it.transactionDate }
                                        .toSortedMap(compareByDescending { it })

                                    byDate.forEach { (date, receiptsForDate) ->
                                        val isDateExpanded = expandedDates.contains(date)
                                        val dateTotal = receiptsForDate.sumOf { it.totalAmount }
                                        val isDateSelected = selectedNode == BookTreeSelection.Day(date)
                                        val dayFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

                                        // Date header
                                        item(key = "date_$date") {
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        expandedDates = if (isDateExpanded) expandedDates - date else expandedDates + date
                                                        selectedNode = if (isDateSelected) BookTreeSelection.None else BookTreeSelection.Day(date)
                                                    },
                                                color = if (isDateSelected)
                                                    MaterialTheme.colorScheme.surface
                                                else
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 48.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isDateExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                            contentDescription = if (isDateExpanded) "Collapse" else "Expand",
                                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                        )
                                                        Text(
                                                            text = date.format(dayFormatter),
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    Text(
                                                        text = formatBookCurrency(dateTotal),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        // Receipt items
                                        if (isDateExpanded) {
                                            items(receiptsForDate, key = { it.id }) { receipt ->
                                                val vendor = vendors.find { it.id == receipt.vendorId }
                                                val category = categories.find { it.id == receipt.categoryId }
                                                Box(modifier = Modifier.padding(start = 16.dp)) {
                                                    ReceiptListItemSimple(
                                                        receipt = receipt,
                                                        vendorName = vendor?.name ?: "Unknown",
                                                        vendorIconName = vendor?.iconName ?: "Store",
                                                        categoryName = category?.name ?: "Uncategorized",
                                                        categoryColor = category?.colorHex ?: "#808080",
                                                        categoryIconName = category?.iconName ?: "Category",
                                                        bookName = book?.name ?: "",
                                                        onItemClick = { onNavigateToReceiptDetail(receipt.id) },
                                                        onImageClick = { }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed class BookTreeSelection {
    object None : BookTreeSelection()
    data class Year(val year: Int) : BookTreeSelection()
    data class Month(val yearMonth: YearMonth) : BookTreeSelection()
    data class Day(val date: LocalDate) : BookTreeSelection()
}

private fun formatBookCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}
