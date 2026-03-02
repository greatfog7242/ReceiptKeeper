package com.receiptkeeper.features.analytics

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.StackedBarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptkeeper.core.util.CsvExporter
import com.receiptkeeper.features.analytics.components.CategoryBreakdownChart
import com.receiptkeeper.features.analytics.components.ChartType
import com.receiptkeeper.features.analytics.components.DateRangePicker
import com.receiptkeeper.features.analytics.components.SpendingGoalCard
import com.receiptkeeper.features.analytics.components.SpendingTrendChart
import com.receiptkeeper.features.analytics.components.VendorBreakdownChart
import com.receiptkeeper.features.analytics.components.getGoalPeriodDateRange
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import kotlinx.coroutines.launch

/**
 * Analytics screen - spending insights and reports
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val selectedBookId by viewModel.selectedBookId.collectAsState()
    val totalSpending by viewModel.totalSpending.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val vendorBreakdown by viewModel.vendorBreakdown.collectAsState()
    val receipts by viewModel.receipts.collectAsState()
    val spendingGoals by viewModel.spendingGoals.collectAsState()
    val vendors by viewModel.vendors.collectAsState()
    val books by viewModel.books.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    val dailyAccumulatedSpending by viewModel.dailyAccumulatedSpending.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedChartType by remember { mutableStateOf(ChartType.PIE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    // Create maps for CSV export
                                    val vendorNames = vendors.associate { it.id to it.name }
                                    val categoryNames = categories.associate { it.id to it.name }
                                    val bookNames = books.associate { it.id to it.name }
                                    val paymentMethodNames = paymentMethods.associate { it.id to it.name }

                                    // Get current book selection for export
                                    val selectedBook = books.find { it.id == selectedBookId }
                                    val bookFilterText = if (selectedBook != null) {
                                        " for ${selectedBook.name}"
                                    } else {
                                        ""
                                    }

                                    // Save to Downloads folder
                                    val uri = CsvExporter.saveToDownloads(
                                        context = context,
                                        receipts = receipts,
                                        vendorNames = vendorNames,
                                        categoryNames = categoryNames,
                                        bookNames = bookNames,
                                        paymentMethodNames = paymentMethodNames
                                    )

                                    // Show result message
                                    val message = if (uri != null) {
                                        "Exported${bookFilterText} to Downloads/ReceiptKeeper_ timestamp folder"
                                    } else {
                                        "Failed to export"
                                    }
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = receipts.isNotEmpty()
                    ) {
                        Icon(Icons.Default.IosShare, contentDescription = "Export CSV")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Range Picker
            DateRangePicker(
                startDate = startDate,
                endDate = endDate,
                onRangeSelected = { preset ->
                    viewModel.setPredefinedRange(preset)
                }
            )

            // Book Selection
            var showBookDropdown by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Book Filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        IconButton(
                            onClick = { showBookDropdown = !showBookDropdown }
                        ) {
                            Icon(
                                if (showBookDropdown) Icons.Default.Book else Icons.Default.BookmarkBorder,
                                contentDescription = "Toggle book selection"
                            )
                        }
                    }

                    if (showBookDropdown) {
                        // All Books option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setSelectedBook(null)
                                    showBookDropdown = false
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All Books",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedBookId == null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            if (selectedBookId == null) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        // Individual books
                        books.forEach { book ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setSelectedBook(book.id)
                                        showBookDropdown = false
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = book.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selectedBookId == book.id) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                if (selectedBookId == book.id) {
                                    Icon(
                                        Icons.Default.Book,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    } else {
                        // Show current selection summary
                        val selectedBook = books.find { it.id == selectedBookId }
                        Text(
                            text = selectedBook?.name ?: "All Books",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // Total Spending Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Total Spending",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$${"%.2f".format(totalSpending)}",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${receipts.size} receipts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Spending Trend Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpendingTrendChart(
                        dailySpending = dailyAccumulatedSpending,
                        spendingGoals = spendingGoals,
                        startDate = startDate,
                        endDate = endDate,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Spending Goals with Progress
            if (spendingGoals.isNotEmpty()) {
                Text(
                    text = "Spending Goals",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                spendingGoals.forEach { goal ->
                    // Calculate spending for this goal's period
                    val (goalStart, goalEnd) = getGoalPeriodDateRange(goal.period)

                    // Get current spending for goal period
                    // We'll use a simplified calculation based on current data
                    // In production, you'd query the repository with goal's date range
                    val goalSpending = if (goal.categoryId != null) {
                        // Category-specific goal
                        categoryBreakdown.find { it.categoryId == goal.categoryId }?.total ?: 0.0
                    } else {
                        // Global goal
                        totalSpending
                    }

                    val category = categories.find { it.id == goal.categoryId }

                    SpendingGoalCard(
                        goal = goal,
                        currentSpending = goalSpending,
                        category = category
                    )
                }
            } else {
                // Empty state for goals
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Spending Goals",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Set budget goals in Settings to track your spending",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Category Breakdown Chart with type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending by Category",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Chart type toggle
                Row {
                    IconButton(
                        onClick = { selectedChartType = ChartType.PIE }
                    ) {
                        Icon(
                            Icons.Default.PieChart,
                            contentDescription = "Pie Chart",
                            tint = if (selectedChartType == ChartType.PIE) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }
                        )
                    }
                    IconButton(
                        onClick = { selectedChartType = ChartType.STACKED_BAR }
                    ) {
                        Icon(
                            Icons.Default.StackedBarChart,
                            contentDescription = "Stacked Bar Chart",
                            tint = if (selectedChartType == ChartType.STACKED_BAR) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }
                        )
                    }
                    IconButton(
                        onClick = { selectedChartType = ChartType.TREEMAP }
                    ) {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = "Treemap",
                            tint = if (selectedChartType == ChartType.TREEMAP) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }
                        )
                    }
                }
            }

            CategoryBreakdownChart(
                categorySpending = categoryBreakdown,
                categories = categories,
                totalSpending = totalSpending,
                chartType = selectedChartType
            )

            // Vendor Breakdown Chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending by Vendor",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Chart type toggle (reusing selectedChartType)
                Row {
                    IconButton(
                        onClick = { selectedChartType = ChartType.PIE }
                    ) {
                        Icon(
                            Icons.Default.PieChart,
                            contentDescription = "Pie Chart",
                            tint = if (selectedChartType == ChartType.PIE) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }
                        )
                    }
                    IconButton(
                        onClick = { selectedChartType = ChartType.STACKED_BAR }
                    ) {
                        Icon(
                            Icons.Default.StackedBarChart,
                            contentDescription = "Stacked Bar Chart",
                            tint = if (selectedChartType == ChartType.STACKED_BAR) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }
                        )
                    }
                    IconButton(
                        onClick = { selectedChartType = ChartType.TREEMAP }
                    ) {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = "Treemap",
                            tint = if (selectedChartType == ChartType.TREEMAP) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }
                        )
                    }
                }
            }

            VendorBreakdownChart(
                vendorSpending = vendorBreakdown,
                vendors = vendors,
                totalSpending = totalSpending,
                chartType = selectedChartType
            )
        }
    }
}
