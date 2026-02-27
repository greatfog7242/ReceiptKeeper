package com.receiptkeeper.features.receipts

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.receiptkeeper.core.util.ImageHandler
import com.receiptkeeper.domain.model.Receipt
import com.receiptkeeper.features.receipts.components.ReceiptListItem
import com.receiptkeeper.features.scan.ocr.ReceiptParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    var fullScreenImageUri by remember { mutableStateOf<String?>(null) }

    // Group receipts by date and track expanded/collapsed state
    val receiptsByDate = uiState.receipts
        .groupBy { it.transactionDate }
        .toSortedMap(compareByDescending<LocalDate> { it })

    // Initially collapse all dates
    var expandedDates by remember(receiptsByDate.keys) {
        mutableStateOf<Set<LocalDate>>(emptySet())
    }

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
                                                text = formatCurrency(dateTotal),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                // Receipts for this date
                                if (isExpanded) {
                                    items(receiptsForDate, key = { it.id }) { receipt ->
                                        val vendor = uiState.vendors.find { it.id == receipt.vendorId }
                                        val category = uiState.categories.find { it.id == receipt.categoryId }
                                        val book = uiState.books.find { it.id == receipt.bookId }

                                        ReceiptListItem(
                                            receipt = receipt,
                                            vendorName = vendor?.name ?: "Unknown",
                                            vendorIconName = vendor?.iconName ?: "Store",
                                            categoryName = category?.name ?: "Unknown",
                                            categoryColor = category?.colorHex ?: "#95A5A6",
                                            categoryIconName = category?.iconName ?: "Category",
                                            bookName = book?.name ?: "Unknown",
                                            onItemClick = { onNavigateToReceiptDetail(it.id) },
                                            onEditClick = { viewModel.showEditDialog(it) },
                                            onDeleteClick = { receiptToDelete = it },
                                            onImageClick = { imageUri -> fullScreenImageUri = imageUri }
                                        )
                                    }
                                }
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
            onImageClick = { imageUri -> fullScreenImageUri = imageUri },
            onDismiss = {
                if (uiState.editingReceipt != null) viewModel.hideEditDialog()
                else viewModel.hideAddDialog()
            },
            onConfirm = { bookId, vendorName, categoryId, paymentMethodId, amount, date, notes, imageUri ->
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
                        oldImageUri = uiState.editingReceipt!!.imageUri,
                        newImageUri = imageUri
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
                        imageUri = imageUri
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

    // Full-screen image viewer
    fullScreenImageUri?.let { imageUri ->
        FullScreenImageDialog(
            imageUri = imageUri,
            onDismiss = { fullScreenImageUri = null }
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
    onImageClick: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, Long, Long?, Double, LocalDate, String?, Uri?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize vendor name - if editing, look up the vendor name by ID
    val initialVendorName = receipt?.vendorId?.let { vendorId ->
        vendors.find { it.id == vendorId }?.name ?: ""
    } ?: ""

    var vendorName by remember { mutableStateOf(initialVendorName) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isOcrProcessing by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }
    var selectedBookId by remember { mutableStateOf(receipt?.bookId ?: books.firstOrNull()?.id ?: 0L) }
    var selectedCategoryId by remember { mutableStateOf(receipt?.categoryId ?: categories.firstOrNull()?.id ?: 0L) }
    var selectedPaymentMethodId by remember { mutableStateOf<Long?>(receipt?.paymentMethodId) }
    var amount by remember { mutableStateOf(receipt?.totalAmount?.toString() ?: "") }
    var date by remember { mutableStateOf(receipt?.transactionDate?.toString() ?: LocalDate.now().toString()) }
    var notes by remember { mutableStateOf(receipt?.notes ?: "") }
    var showBookDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPaymentDropdown by remember { mutableStateOf(false) }
    var showVendorDropdown by remember { mutableStateOf(false) }
    var vendorError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (receipt == null) "Add Receipt" else "Edit Receipt") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vendor dropdown
                ExposedDropdownMenuBox(
                    expanded = showVendorDropdown,
                    onExpandedChange = { showVendorDropdown = !showVendorDropdown }
                ) {
                    OutlinedTextField(
                        value = vendorName,
                        onValueChange = {
                            vendorName = it
                            vendorError = it.isBlank()
                            showVendorDropdown = true
                        },
                        label = { Text("Vendor *") },
                        placeholder = { Text("Select or type vendor") },
                        isError = vendorError,
                        supportingText = if (vendorError) {{ Text("Vendor is required") }} else null,
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showVendorDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showVendorDropdown,
                        onDismissRequest = { showVendorDropdown = false }
                    ) {
                        // Show existing vendors as options
                        vendors.forEach { vendor ->
                            DropdownMenuItem(
                                text = { Text(vendor.name) },
                                onClick = {
                                    vendorName = vendor.name
                                    vendorError = false
                                    showVendorDropdown = false
                                }
                            )
                        }
                        // Allow adding a new vendor (option to create)
                        if (vendorName.isNotBlank() && vendors.none { it.name.equals(vendorName, ignoreCase = true) }) {
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Create \"$vendorName\"") },
                                onClick = {
                                    vendorError = false
                                    showVendorDropdown = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            )
                        }
                    }
                }

                // Image picker
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (selectedImageUri != null || receipt?.imageUri != null) {
                                    "Change Image"
                                } else {
                                    "Add Image"
                                }
                            )
                        }

                        // Remove image button (only show if image exists)
                        if (selectedImageUri != null || receipt?.imageUri != null) {
                            OutlinedButton(
                                onClick = { selectedImageUri = Uri.EMPTY },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove image")
                            }
                        }
                    }

                    // Image preview
                    val displayImageUri = if (selectedImageUri == Uri.EMPTY) {
                        null // User clicked remove
                    } else {
                        selectedImageUri ?: receipt?.imageUri?.let { Uri.parse("file://$it") }
                    }

                    if (displayImageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    // Show full-screen if it's an existing image (file path)
                                    receipt?.imageUri?.let { onImageClick(it) }
                                }
                        ) {
                            AsyncImage(
                                model = displayImageUri,
                                contentDescription = "Receipt image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // OCR Button - appears when image is loaded
                    if (displayImageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    // Run OCR on the selected image
                                    isOcrProcessing = true
                                    val imageUriForOcr = selectedImageUri ?: receipt?.imageUri?.let { Uri.parse("file://$it") }
                                    if (imageUriForOcr != null) {
                                        coroutineScope.launch {
                                            try {
                                                // Load bitmap from URI
                                                val inputStream = context.contentResolver.openInputStream(imageUriForOcr)
                                                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                                                inputStream?.close()

                                                if (bitmap != null) {
                                                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                                                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                                    val result = recognizer.process(inputImage).await()
                                                    bitmap.recycle()

                                                    if (result.text.isNotBlank()) {
                                                        val parsedData = ReceiptParser.parseReceipt(result.text)
                                                        parsedData.vendor?.let { vendorName = it }
                                                        parsedData.amount?.let { amount = it.toString() }
                                                        parsedData.date?.let { date = it.toString() }
                                                        withContext(Dispatchers.Main) {
                                                            Toast.makeText(context, "OCR: Extracted data", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            } finally {
                                                isOcrProcessing = false
                                            }
                                        }
                                    }
                                },
                                enabled = !isOcrProcessing,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isOcrProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.DocumentScanner, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isOcrProcessing) "Processing..." else "Extract Text (OCR)")
                            }
                        }
                    }
                }

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

                // Date with date picker
                var showDatePicker by remember { mutableStateOf(false) }
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = try {
                        // Use UTC to avoid timezone shifts
                        java.time.LocalDate.parse(date).atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
                    } catch (e: Exception) {
                        java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
                    }
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    placeholder = { Text(LocalDate.now().toString()) },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Select date")
                        }
                    }
                )

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        // Use UTC to avoid timezone shifts causing off-by-one errors
                                        val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                            .atZone(java.time.ZoneId.of("UTC"))
                                            .toLocalDate()
                                        date = selectedDate.toString()
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

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
                            notes.takeIf { it.isNotBlank() },
                            selectedImageUri
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

@Composable
private fun FullScreenImageDialog(
    imageUri: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val imageHandler = remember { ImageHandler(context) }
    var isDownloading by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Receipt image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Top bar with close and download buttons
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Download button
                IconButton(
                    onClick = {
                        isDownloading = true
                        coroutineScope.launch {
                            val uri = imageHandler.downloadImageToGallery(imageUri)
                            isDownloading = false
                            val message = if (uri != null) {
                                "Image saved to Downloads"
                            } else {
                                "Failed to download image"
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isDownloading
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download",
                            tint = Color.White
                        )
                    }
                }

                // Close button
                IconButton(onClick = onDismiss) {
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
