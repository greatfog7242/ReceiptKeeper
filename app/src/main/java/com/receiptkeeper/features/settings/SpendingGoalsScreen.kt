package com.receiptkeeper.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptkeeper.data.local.entity.GoalPeriod
import com.receiptkeeper.domain.model.Category
import com.receiptkeeper.domain.model.SpendingGoal
import androidx.compose.foundation.text.KeyboardOptions

/**
 * Screen for managing spending goals
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingGoalsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SpendingGoalsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val spendingGoals by viewModel.spendingGoals.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SpendingGoal?>(null) }
    var deleteConfirmGoal by remember { mutableStateOf<SpendingGoal?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending Goals") },
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
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingGoal = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, "Add Goal")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (spendingGoals.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No spending goals yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Set budget goals to track your spending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(spendingGoals) { goal ->
                        SpendingGoalCard(
                            goal = goal,
                            category = categories.find { it.id == goal.categoryId },
                            onEdit = {
                                editingGoal = goal
                                showDialog = true
                            },
                            onDelete = {
                                deleteConfirmGoal = goal
                            }
                        )
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
    if (showDialog) {
        SpendingGoalDialog(
            goal = editingGoal,
            categories = categories,
            onDismiss = { showDialog = false },
            onSave = { amount, period, categoryId ->
                if (editingGoal != null) {
                    viewModel.updateSpendingGoal(
                        editingGoal!!.copy(
                            amount = amount,
                            period = period,
                            categoryId = categoryId
                        )
                    )
                } else {
                    viewModel.createSpendingGoal(amount, period, categoryId)
                }
                showDialog = false
            }
        )
    }

    // Delete confirmation dialog
    deleteConfirmGoal?.let { goal ->
        AlertDialog(
            onDismissRequest = { deleteConfirmGoal = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Delete Spending Goal?") },
            text = {
                val categoryName = categories.find { it.id == goal.categoryId }?.name ?: "All Categories"
                Text("Delete ${goal.period.name} goal for $categoryName ($${"%.2f".format(goal.amount)})?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSpendingGoal(goal)
                        deleteConfirmGoal = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmGoal = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SpendingGoalCard(
    goal: SpendingGoal,
    category: Category?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = category?.name ?: "All Categories",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${goal.period.name} Budget",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "$${"%.2f".format(goal.amount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpendingGoalDialog(
    goal: SpendingGoal?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Double, GoalPeriod, Long?) -> Unit
) {
    var amount by remember { mutableStateOf(goal?.amount?.toString() ?: "") }
    var selectedPeriod by remember { mutableStateOf(goal?.period ?: GoalPeriod.MONTHLY) }
    var selectedCategoryId by remember { mutableStateOf(goal?.categoryId) }
    var periodExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal != null) "Edit Spending Goal" else "New Spending Goal") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Budget Amount *") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("$") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Period dropdown
                ExposedDropdownMenuBox(
                    expanded = periodExpanded,
                    onExpandedChange = { periodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPeriod.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Period *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = periodExpanded,
                        onDismissRequest = { periodExpanded = false }
                    ) {
                        GoalPeriod.entries.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.name) },
                                onClick = {
                                    selectedPeriod = period
                                    periodExpanded = false
                                }
                            )
                        }
                    }
                }

                // Category dropdown (optional)
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "All Categories",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                selectedCategoryId = null
                                categoryExpanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "* Required fields",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (amountDouble != null && amountDouble > 0) {
                        onSave(amountDouble, selectedPeriod, selectedCategoryId)
                    }
                },
                enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
