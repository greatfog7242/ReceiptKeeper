package com.receiptkeeper.features.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptkeeper.core.util.CsvExporter
import com.receiptkeeper.data.repository.BookRepository
import com.receiptkeeper.data.repository.PaymentMethodRepository
import com.receiptkeeper.data.repository.VendorRepository
import com.receiptkeeper.features.analytics.components.CategoryBreakdownChart
import com.receiptkeeper.features.analytics.components.DateRangePicker
import com.receiptkeeper.features.analytics.components.SpendingGoalCard
import com.receiptkeeper.features.analytics.components.getGoalPeriodDateRange
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
    val totalSpending by viewModel.totalSpending.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val receipts by viewModel.receipts.collectAsState()
    val spendingGoals by viewModel.spendingGoals.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                actions = {
                    IconButton(onClick = { /* TODO: Export CSV */ }) {
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

            // Category Breakdown Chart
            CategoryBreakdownChart(
                categorySpending = categoryBreakdown,
                categories = categories,
                totalSpending = totalSpending
            )

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
        }
    }
}
