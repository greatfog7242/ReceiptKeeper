package com.receiptkeeper.features.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
    val context = LocalContext.current

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
            // Display brand logo or Material icon
            if (IconHelper.isBrandIcon(vendor.iconName)) {
                val imageModel = if (IconHelper.isCustomIcon(vendor.iconName)) {
                    IconHelper.getCustomBrandIconUri(context, vendor.iconName)
                } else {
                    val brandName = IconHelper.getBrandIconName(vendor.iconName)
                    "file:///android_asset/brand_logos/$brandName.png"
                }
                AsyncImage(
                    model = imageModel,
                    contentDescription = vendor.name,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = IconHelper.getIcon(vendor.iconName),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

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
    val context = LocalContext.current
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
                            if (IconHelper.isBrandIcon(selectedIcon)) {
                                val imageModel = if (IconHelper.isCustomIcon(selectedIcon)) {
                                    IconHelper.getCustomBrandIconUri(context, selectedIcon)
                                } else {
                                    val brandName = IconHelper.getBrandIconName(selectedIcon)
                                    "file:///android_asset/brand_logos/$brandName.png"
                                }
                                AsyncImage(
                                    model = imageModel,
                                    contentDescription = "Select icon",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = IconHelper.getIcon(selectedIcon),
                                    contentDescription = "Select icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
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
    val context = LocalContext.current
    var showAddCustomIconDialog by remember { mutableStateOf(false) }
    var iconRefreshCounter by remember { mutableStateOf(0) }
    val iconCategories = remember { IconHelper.getIconCategories() }
    val brandIcons = remember { IconHelper.getBrandIcons() }
    // Refresh custom icons when counter changes (after adding new icon)
    val customIcons by remember(iconRefreshCounter) { mutableStateOf(IconHelper.getCustomBrandIcons(context)) }
    val allCategories = remember {
        listOf("Brands", "Custom") + iconCategories.keys.toList()
    }
    var selectedCategory by remember { mutableStateOf(allCategories.first()) }

    // Check if current icon is a brand icon
    val isCurrentBrand = IconHelper.isBrandIcon(currentIcon)

    // Add custom icon dialog
    if (showAddCustomIconDialog) {
        AddCustomIconDialog(
            onDismiss = { showAddCustomIconDialog = false },
            onIconAdded = { iconName ->
                showAddCustomIconDialog = false
                iconRefreshCounter++ // Force refresh of custom icons list
                // Refresh custom icons and select the new one
                onIconSelected(iconName)
            }
        )
    }

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
                    selectedTabIndex = allCategories.indexOf(selectedCategory).coerceAtLeast(0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    allCategories.forEach { category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            text = { Text(category, maxLines = 1) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Icons grid
                when (selectedCategory) {
                    "Brands" -> {
                        // Built-in brand logos grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(brandIcons) { (iconName, displayName) ->
                                val fullIconName = IconHelper.BRAND_PREFIX + iconName
                                val isSelected = currentIcon == fullIconName ||
                                               (isCurrentBrand && IconHelper.getBrandIconName(currentIcon) == iconName)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { onIconSelected(fullIconName) }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .background(
                                                color = if (isSelected)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = "file:///android_asset/brand_logos/$iconName.png",
                                            contentDescription = displayName,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    "Custom" -> {
                        // Custom brand icons grid
                        Column {
                            // Add button
                            OutlinedButton(
                                onClick = {
                                    iconRefreshCounter++ // Refresh before opening dialog
                                    showAddCustomIconDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Custom Icon")
                            }

                            if (customIcons.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No custom icons yet.\nTap above to add one.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(4),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(customIcons) { (iconName, displayName) ->
                                        val isSelected = currentIcon == iconName

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clickable { onIconSelected(iconName) }
                                                .padding(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(
                                                        color = if (isSelected)
                                                            MaterialTheme.colorScheme.primaryContainer
                                                        else
                                                            MaterialTheme.colorScheme.surfaceVariant,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = IconHelper.getCustomBrandIconUri(context, iconName),
                                                    contentDescription = displayName,
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                )
                                            }
                                            Text(
                                                text = displayName,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        // Material icons grid
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

@Composable
private fun AddCustomIconDialog(
    onDismiss: () -> Unit,
    onIconAdded: (String) -> Unit
) {
    val context = LocalContext.current
    var iconName by remember { mutableStateOf("") }
    var showInstructions by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && iconName.isNotBlank()) {
            // Read image and save to internal storage
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val imageBytes = inputStream?.readBytes()
                inputStream?.close()

                if (imageBytes != null) {
                    val cleanName = iconName
                        .replace(" ", "_")
                        .replace(Regex("[^a-zA-Z0-9_]"), "")

                    val success = IconHelper.saveCustomBrandIcon(context, cleanName, imageBytes)
                    if (success) {
                        onIconAdded(IconHelper.CUSTOM_BRAND_PREFIX + cleanName)
                    } else {
                        errorMessage = "Failed to save icon"
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to read image: ${e.message}"
            }
        }
    }

    if (showInstructions) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Add Custom Brand Icon") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "To add a custom brand icon:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "1. File Type",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Use PNG or JPG/JPEG images. PNG is recommended for transparent backgrounds.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "2. Dimensions",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Square images work best. Recommended size: 256x256 pixels or larger.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "3. Naming",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Use a short name with underscores for spaces (e.g., My_Store). This name will appear in the icon list.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "4. How to Add",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Click Continue, enter a name, then select your image file.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showInstructions = false }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Select Image") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = iconName,
                        onValueChange = {
                            iconName = it
                            errorMessage = null
                        },
                        label = { Text("Icon Name *") },
                        placeholder = { Text("e.g., My_Store") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text("Use letters, numbers, and underscores only")
                        }
                    )

                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (iconName.isBlank()) {
                            errorMessage = "Please enter a name"
                        } else {
                            imagePickerLauncher.launch("image/*")
                        }
                    },
                    enabled = iconName.isNotBlank()
                ) {
                    Text("Select Image")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstructions = true }) {
                    Text("Back")
                }
            }
        )
    }
}
