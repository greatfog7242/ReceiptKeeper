package com.receiptkeeper.features.scan

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.receiptkeeper.domain.model.Book
import com.receiptkeeper.domain.model.Category
import com.receiptkeeper.domain.model.PaymentMethod
import com.receiptkeeper.domain.model.Vendor
import com.receiptkeeper.features.scan.camera.CameraPreview
import com.receiptkeeper.features.scan.ocr.ExtractedReceiptData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Scan screen - camera preview and OCR processing
 * Three-mode workflow: Camera → Preview → Edit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onReceiptScanned: (Long) -> Unit = {},
    viewModel: ScanReceiptViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val books by viewModel.books.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    val vendors by viewModel.vendors.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Receipt") },
                actions = {
                    if (!uiState.isScanning) {
                        IconButton(onClick = { viewModel.retryCapture() }) {
                            Icon(Icons.Default.Refresh, "Retry capture")
                        }
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
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                uiState.isScanning -> {
                    // Mode 1: Camera preview
                    CameraPreview(
                        onImageCaptured = { uri -> viewModel.onImageCaptured(uri) },
                        onError = { /* Will be shown as snackbar */ }
                    )
                }

                uiState.capturedImageUri != null && uiState.extractedData == null -> {
                    // Mode 2: Image preview (before OCR)
                    CapturedImagePreview(
                        imageUri = uiState.capturedImageUri!!,
                        onProcessClick = { viewModel.processOcr() },
                        onSkipClick = { viewModel.skipOcr() },
                        onRetryClick = { viewModel.retryCapture() },
                        isProcessing = uiState.isProcessing
                    )
                }

                uiState.extractedData != null -> {
                    // Mode 3: Extracted data form
                    ExtractedDataForm(
                        extractedData = uiState.extractedData!!,
                        imageUri = uiState.capturedImageUri,
                        books = books,
                        categories = categories,
                        paymentMethods = paymentMethods,
                        vendors = vendors,
                        onVendorChange = { viewModel.updateVendor(it) },
                        onAmountChange = { viewModel.updateAmount(it) },
                        onDateChange = { viewModel.updateDate(it) },
                        onSaveClick = { bookId, categoryId, paymentMethodId, notes ->
                            viewModel.saveReceipt(
                                bookId = bookId,
                                categoryId = categoryId,
                                paymentMethodId = paymentMethodId,
                                notes = notes,
                                onSuccess = onReceiptScanned
                            )
                        },
                        isProcessing = uiState.isProcessing
                    )
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

            // Loading overlay during OCR/save
            if (uiState.isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Processing...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shows captured image with OCR processing options
 */
@Composable
private fun CapturedImagePreview(
    imageUri: Uri,
    onProcessClick: () -> Unit,
    onSkipClick: () -> Unit,
    onRetryClick: () -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Captured receipt",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Text(
            text = "Process this receipt with OCR to auto-extract data, or enter manually.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        // Action buttons
        Button(
            onClick = onProcessClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Process with OCR")
        }

        OutlinedButton(
            onClick = onSkipClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Skip OCR - Enter Manually")
        }

        TextButton(
            onClick = onRetryClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retake Photo")
        }
    }
}

/**
 * Form for editing extracted receipt data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExtractedDataForm(
    extractedData: ExtractedReceiptData,
    imageUri: Uri?,
    books: List<Book>,
    categories: List<Category>,
    paymentMethods: List<PaymentMethod>,
    vendors: List<Vendor>,
    onVendorChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDateChange: (LocalDate?) -> Unit,
    onSaveClick: (Long, Long, Long?, String) -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedBookId by remember { mutableStateOf(books.firstOrNull()?.id ?: 0L) }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: 0L) }
    var selectedPaymentMethodId by remember { mutableStateOf<Long?>(null) }
    var notes by remember { mutableStateOf("") }
    var showFullImage by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image thumbnail
        if (imageUri != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { showFullImage = true }
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Receipt image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Text(
            text = "Review and edit extracted data",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Vendor dropdown with free-text option
        var vendorExpanded by remember { mutableStateOf(false) }
        var vendorSearchText by remember(extractedData.vendor) {
            mutableStateOf(extractedData.vendor ?: "")
        }

        ExposedDropdownMenuBox(
            expanded = vendorExpanded,
            onExpandedChange = { vendorExpanded = it }
        ) {
            OutlinedTextField(
                value = vendorSearchText,
                onValueChange = { newValue ->
                    vendorSearchText = newValue
                    onVendorChange(newValue)
                },
                label = { Text("Vendor Name") },
                placeholder = { Text("e.g., Walmart, Target") },
                leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = vendorExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = vendorExpanded,
                onDismissRequest = { vendorExpanded = false }
            ) {
                // Filter vendors based on search text
                val filteredVendors = if (vendorSearchText.isBlank()) {
                    vendors
                } else {
                    vendors.filter {
                        it.name.contains(vendorSearchText, ignoreCase = true)
                    }
                }

                if (filteredVendors.isEmpty() && vendorSearchText.isNotBlank()) {
                    // Show "Add new" option when no matches
                    DropdownMenuItem(
                        text = { Text("Add \"$vendorSearchText\" as new vendor") },
                        onClick = {
                            onVendorChange(vendorSearchText)
                            vendorExpanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                    )
                } else {
                    filteredVendors.forEach { vendor ->
                        DropdownMenuItem(
                            text = { Text(vendor.name) },
                            onClick = {
                                vendorSearchText = vendor.name
                                onVendorChange(vendor.name)
                                vendorExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Amount field
        OutlinedTextField(
            value = extractedData.amount?.toString() ?: "",
            onValueChange = onAmountChange,
            label = { Text("Amount *") },
            placeholder = { Text("0.00") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("$") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        // Date field (simple text for now - can be enhanced with date picker)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var dateText by remember {
            mutableStateOf(extractedData.date?.format(dateFormatter) ?: LocalDate.now().format(dateFormatter))
        }

        OutlinedTextField(
            value = dateText,
            onValueChange = { newText ->
                dateText = newText
                try {
                    val parsedDate = LocalDate.parse(newText, dateFormatter)
                    onDateChange(parsedDate)
                } catch (e: Exception) {
                    // Invalid date format - ignore
                }
            },
            label = { Text("Date (YYYY-MM-DD)") },
            placeholder = { Text("2026-02-16") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
        )

        // Book dropdown
        var bookExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = bookExpanded,
            onExpandedChange = { bookExpanded = it }
        ) {
            OutlinedTextField(
                value = books.find { it.id == selectedBookId }?.name ?: "Select Book",
                onValueChange = {},
                readOnly = true,
                label = { Text("Book *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bookExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = bookExpanded,
                onDismissRequest = { bookExpanded = false }
            ) {
                books.forEach { book ->
                    DropdownMenuItem(
                        text = { Text(book.name) },
                        onClick = {
                            selectedBookId = book.id
                            bookExpanded = false
                        }
                    )
                }
            }
        }

        // Category dropdown
        var categoryExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = categories.find { it.id == selectedCategoryId }?.name ?: "Select Category",
                onValueChange = {},
                readOnly = true,
                label = { Text("Category *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
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

        // Payment method dropdown (optional)
        var paymentExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = paymentExpanded,
            onExpandedChange = { paymentExpanded = it }
        ) {
            OutlinedTextField(
                value = paymentMethods.find { it.id == selectedPaymentMethodId }?.name ?: "None",
                onValueChange = {},
                readOnly = true,
                label = { Text("Payment Method") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = paymentExpanded,
                onDismissRequest = { paymentExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        selectedPaymentMethodId = null
                        paymentExpanded = false
                    }
                )
                paymentMethods.forEach { method ->
                    DropdownMenuItem(
                        text = { Text(method.name) },
                        onClick = {
                            selectedPaymentMethodId = method.id
                            paymentExpanded = false
                        }
                    )
                }
            }
        }

        // Notes field
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            placeholder = { Text("Add any additional details") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        // OCR raw text (collapsible)
        if (extractedData.fullText.isNotBlank()) {
            var showRawText by remember { mutableStateOf(false) }

            TextButton(
                onClick = { showRawText = !showRawText },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showRawText) "Hide OCR Text" else "Show OCR Text")
                Icon(
                    if (showRawText) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            if (showRawText) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = extractedData.fullText,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Save button
        Button(
            onClick = {
                onSaveClick(
                    selectedBookId,
                    selectedCategoryId,
                    selectedPaymentMethodId,
                    notes
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing && selectedBookId > 0 && selectedCategoryId > 0
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Receipt")
        }
    }

    // Full-screen image dialog
    if (showFullImage && imageUri != null) {
        Dialog(onDismissRequest = { showFullImage = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullImage = false },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Full receipt image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = { showFullImage = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
