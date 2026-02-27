package com.receiptkeeper.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptkeeper.core.util.IconHelper
import com.receiptkeeper.domain.model.Vendor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: VendorsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var vendorToDelete by remember { mutableStateOf<Vendor?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendors") },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add vendor")
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

                uiState.vendors.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Store,
                        title = "No vendors yet",
                        subtitle = "Add vendors to track where you shop",
                        onAddClick = { viewModel.showAddDialog() }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.vendors, key = { it.id }) { vendor ->
                            VendorListItem(
                                vendor = vendor,
                                onEditClick = { viewModel.showEditDialog(it) },
                                onDeleteClick = { vendorToDelete = it }
                            )
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
    if (uiState.showAddDialog || uiState.editingVendor != null) {
        VendorDialog(
            vendor = uiState.editingVendor,
            onDismiss = {
                if (uiState.editingVendor != null) viewModel.hideEditDialog()
                else viewModel.hideAddDialog()
            },
            onConfirm = { name, iconName ->
                if (uiState.editingVendor != null) {
                    viewModel.updateVendor(uiState.editingVendor!!.copy(name = name, iconName = iconName))
                } else {
                    viewModel.createVendor(name, iconName)
                }
            }
        )
    }

    // Delete confirmation
    vendorToDelete?.let { vendor ->
        DeleteConfirmationDialog(
            title = "Delete Vendor?",
            message = "Are you sure you want to delete \"${vendor.name}\"?",
            onDismiss = { vendorToDelete = null },
            onConfirm = {
                viewModel.deleteVendor(vendor)
                vendorToDelete = null
            }
        )
    }
}

@Composable
private fun VendorListItem(
    vendor: Vendor,
    onEditClick: (Vendor) -> Unit,
    onDeleteClick: (Vendor) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = IconHelper.getIcon(vendor.iconName),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = vendor.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { onEditClick(vendor) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = { onDeleteClick(vendor) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun VendorDialog(
    vendor: Vendor?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(vendor?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(vendor?.iconName ?: "Store") }
    var error by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    if (showIconPicker) {
        IconPickerDialog(
            currentIcon = selectedIcon,
            onDismiss = { showIconPicker = false },
            onIconSelected = { iconName ->
                selectedIcon = iconName
                showIconPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vendor == null) "Add Vendor" else "Edit Vendor") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = it.isBlank()
                    },
                    label = { Text("Vendor Name *") },
                    isError = error,
                    supportingText = if (error) {{ Text("Name is required") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Icon picker
                Column {
                    Text(
                        text = "Icon",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .clickable { showIconPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIcon(selectedIcon),
                                contentDescription = "Select icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        TextButton(onClick = { showIconPicker = true }) {
                            Text("Change Icon")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedIcon)
                    } else {
                        error = true
                    }
                }
            ) {
                Text(if (vendor == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add")
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun IconPickerDialog(
    currentIcon: String,
    onDismiss: () -> Unit,
    onIconSelected: (String) -> Unit
) {
    val iconCategories = remember { IconHelper.getIconCategories() }
    var selectedCategory by remember { mutableStateOf(iconCategories.keys.firstOrNull() ?: "General") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Icon") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Category tabs
                ScrollableTabRow(
                    selectedTabIndex = iconCategories.keys.toList().indexOf(selectedCategory).coerceAtLeast(0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    iconCategories.keys.forEach { category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            text = { Text(category, maxLines = 1) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Icons grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconCategories[selectedCategory] ?: emptyList()) { (iconName, icon) ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(
                                    color = if (currentIcon == iconName)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onIconSelected(iconName) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = iconName,
                                tint = if (currentIcon == iconName)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
