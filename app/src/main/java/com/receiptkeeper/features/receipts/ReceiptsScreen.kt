package com.receiptkeeper.features.receipts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptkeeper.domain.model.Receipt
import com.receiptkeeper.features.receipts.components.ReceiptListItem
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsScreen(
    onNavigateToReceiptDetail: (Long) -> Unit = {},
    viewModel: ReceiptsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var receiptToDelete by remember { mutableStateOf<Receipt?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Book filter dropdown
                    if (uiState.books.isNotEmpty()) {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                imageVector = if (uiState.selectedBookFilter != null) {
                                    Icons.Default.FilterAltOff
                                } else {
                                    Icons.Default.FilterAlt
                                },
                                contentDescription = "Filter by book",
                                tint = if (uiState.selectedBookFilter != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }

                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Books") },
                                onClick = {
                                    viewModel.setBookFilter(null)
                                    showFilterMenu = false
                                },
                                leadingIcon = {
                                    if (uiState.selectedBookFilter == null) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                            HorizontalDivider()
                            uiState.books.forEach { book ->
                                DropdownMenuItem(
                                    text = { Text(book.name) },
                                    onClick = {
                                        viewModel.setBookFilter(book.id)
                                        showFilterMenu = false
                                    },
                                    leadingIcon = {
                                        if (uiState.selectedBookFilter == book.id) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add receipt")
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
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.receipts.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.selectedBookFilter != null) {
                                "No receipts in this book"
                            } else {
                                "No receipts yet"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add a receipt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Total spending card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                Text(
                                    text = "Total Spending",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = formatCurrency(uiState.totalSpending),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Receipts list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(uiState.receipts, key = { it.id }) { receipt ->
                                val vendor = uiState.vendors.find { it.id == receipt.vendorId }
                                val category = uiState.categories.find { it.id == receipt.categoryId }
                                val book = uiState.books.find { it.id == receipt.bookId }

                                ReceiptListItem(
                                    receipt = receipt,
                                    vendorName = vendor?.name ?: "Unknown",
                                    categoryName = category?.name ?: "Unknown",
                                    categoryColor = category?.colorHex ?: "#95A5A6",
                                    bookName = book?.name ?: "Unknown",
                                    onItemClick = { onNavigateToReceiptDetail(it.id) },
                                    onEditClick = { viewModel.showEditDialog(it) },
                                    onDeleteClick = { receiptToDelete = it }
                                )
                            }
                        }
                    }
                }
            }

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
    if (uiState.showAddDialog || uiState.editingReceipt != null) {
        ReceiptDialog(
            receipt = uiState.editingReceipt,
            books = uiState.books,
            vendors = uiState.vendors,
            categories = uiState.categories,
            paymentMethods = uiState.paymentMethods,
            onDismiss = {
                if (uiState.editingReceipt != null) viewModel.hideEditDialog()
                else viewModel.hideAddDialog()
            },
            onConfirm = { bookId, vendorName, categoryId, paymentMethodId, amount, date, notes ->
                if (uiState.editingReceipt != null) {
                    viewModel.updateReceiptFromDialog(
                        receiptId = uiState.editingReceipt!!.id,
                        bookId = bookId,
                        vendorName = vendorName,
                        categoryId = categoryId,
                        paymentMethodId = paymentMethodId,
                        totalAmount = amount,
                        transactionDate = date,
                        notes = notes,
                        imageUri = uiState.editingReceipt!!.imageUri // Preserve existing image
                    )
                } else {
                    viewModel.createReceipt(
                        bookId = bookId,
                        vendorName = vendorName,
                        categoryId = categoryId,
                        paymentMethodId = paymentMethodId,
                        totalAmount = amount,
                        transactionDate = date,
                        notes = notes,
                        imageUri = null // Will add image picker in next step
                    )
                }
            }
        )
    }

    // Delete confirmation
    receiptToDelete?.let { receipt ->
        val vendor = uiState.vendors.find { it.id == receipt.vendorId }
        AlertDialog(
            onDismissRequest = { receiptToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Receipt?") },
            text = {
                Text("Are you sure you want to delete the receipt from \"${vendor?.name ?: "Unknown"}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteReceipt(receipt)
                        receiptToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { receiptToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiptDialog(
    receipt: Receipt?,
    books: List<com.receiptkeeper.domain.model.Book>,
    vendors: List<com.receiptkeeper.domain.model.Vendor>,
    categories: List<com.receiptkeeper.domain.model.Category>,
    paymentMethods: List<com.receiptkeeper.domain.model.PaymentMethod>,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, Long, Long?, Double, LocalDate, String?) -> Unit
) {
    // Initialize vendor name - if editing, look up the vendor name by ID
    val initialVendorName = receipt?.vendorId?.let { vendorId ->
        vendors.find { it.id == vendorId }?.name ?: ""
    } ?: ""

    var vendorName by remember { mutableStateOf(initialVendorName) }
    var selectedBookId by remember { mutableStateOf(receipt?.bookId ?: books.firstOrNull()?.id ?: 0L) }
    var selectedCategoryId by remember { mutableStateOf(receipt?.categoryId ?: categories.firstOrNull()?.id ?: 0L) }
    var selectedPaymentMethodId by remember { mutableStateOf<Long?>(receipt?.paymentMethodId) }
    var amount by remember { mutableStateOf(receipt?.totalAmount?.toString() ?: "") }
    var date by remember { mutableStateOf(receipt?.transactionDate?.toString() ?: LocalDate.now().toString()) }
    var notes by remember { mutableStateOf(receipt?.notes ?: "") }
    var showBookDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPaymentDropdown by remember { mutableStateOf(false) }
    var vendorError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (receipt == null) "Add Receipt" else "Edit Receipt") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vendor name
                OutlinedTextField(
                    value = vendorName,
                    onValueChange = {
                        vendorName = it
                        vendorError = it.isBlank()
                    },
                    label = { Text("Vendor *") },
                    placeholder = { Text("e.g., Walmart") },
                    isError = vendorError,
                    supportingText = if (vendorError) {{ Text("Vendor is required") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Book dropdown
                ExposedDropdownMenuBox(
                    expanded = showBookDropdown,
                    onExpandedChange = { showBookDropdown = !showBookDropdown }
                ) {
                    OutlinedTextField(
                        value = books.find { it.id == selectedBookId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Book *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBookDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showBookDropdown,
                        onDismissRequest = { showBookDropdown = false }
                    ) {
                        books.forEach { book ->
                            DropdownMenuItem(
                                text = { Text(book.name) },
                                onClick = {
                                    selectedBookId = book.id
                                    showBookDropdown = false
                                }
                            )
                        }
                    }
                }

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                // Payment method dropdown
                ExposedDropdownMenuBox(
                    expanded = showPaymentDropdown,
                    onExpandedChange = { showPaymentDropdown = !showPaymentDropdown }
                ) {
                    OutlinedTextField(
                        value = paymentMethods.find { it.id == selectedPaymentMethodId }?.name ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPaymentDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showPaymentDropdown,
                        onDismissRequest = { showPaymentDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                selectedPaymentMethodId = null
                                showPaymentDropdown = false
                            }
                        )
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.name) },
                                onClick = {
                                    selectedPaymentMethodId = method.id
                                    showPaymentDropdown = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                            amount = it
                            amountError = it.isEmpty()
                        }
                    },
                    label = { Text("Amount *") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("$") },
                    isError = amountError,
                    supportingText = if (amountError) {{ Text("Amount is required") }} else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date (simplified for now)
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    placeholder = { Text(LocalDate.now().toString()) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    val dateValue = try {
                        LocalDate.parse(date)
                    } catch (e: Exception) {
                        LocalDate.now()
                    }

                    if (vendorName.isNotBlank() && amountValue != null && amountValue > 0) {
                        onConfirm(
                            selectedBookId,
                            vendorName.trim(),
                            selectedCategoryId,
                            selectedPaymentMethodId,
                            amountValue,
                            dateValue,
                            notes.takeIf { it.isNotBlank() }
                        )
                    } else {
                        vendorError = vendorName.isBlank()
                        amountError = amountValue == null || amountValue <= 0
                    }
                }
            ) {
                Text(if (receipt == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}
