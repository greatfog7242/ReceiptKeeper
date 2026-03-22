package com.receiptkeeper.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimestampSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TimestampSettingsViewModel = hiltViewModel()
) {
    val books by viewModel.books.collectAsState()
    val selectedBookId by viewModel.selectedBookId.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RFC 3161 Timestamps") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "RFC 3161 Trusted Timestamps",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = "When a book is selected, every receipt saved in that book will automatically receive a cryptographic timestamp from DigiCert's public TSA. The timestamp token is stored locally and proves when the receipt was recorded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Timestamp receipts in this book",
                style = MaterialTheme.typography.labelLarge
            )

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = if (selectedBookId == null) {
                        "None (disabled)"
                    } else {
                        books.find { it.id == selectedBookId }?.name ?: "None (disabled)"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Book") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None (disabled)") },
                        onClick = {
                            viewModel.setTimestampBook(null)
                            dropdownExpanded = false
                        }
                    )
                    books.forEach { book ->
                        DropdownMenuItem(
                            text = { Text(book.name) },
                            onClick = {
                                viewModel.setTimestampBook(book.id)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
