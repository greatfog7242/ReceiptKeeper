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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptkeeper.core.util.IconHelper
import com.receiptkeeper.domain.model.Category

// Predefined colors for categories
private val categoryColors = listOf(
    "#FF6B6B" to "Red",
    "#FF6B6B" to "Coral",
    "#4ECB71" to "Green",
    "#2ECC71" to "Lime",
    "#FFA500" to "Orange",
    "#F39C12" to "Yellow",
    "#E91E63" to "Pink",
    "#E74C3C" to "Crimson",
    "#3498DB" to "Blue",
    "#3498DB" to "Sky Blue",
    "#9B59B6" to "Purple",
    "#1ABC9C" to "Teal",
    "#16A085" to "Sea Green",
    "#95A5A6" to "Gray",
    "#7F8C8D" to "Slate",
    "#34495E" to "Navy",
    "#D35400" to "Pumpkin",
    "#8E44AD" to "Violet",
    "#27AE60" to "Nephritis",
    "#2980B9" to "Belize Hole"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: CategoriesViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
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
                Icon(Icons.Default.Add, contentDescription = "Add category")
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

                uiState.categories.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No categories",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.categories, key = { it.id }) { category ->
                            CategoryListItem(
                                category = category,
                                onEditClick = { viewModel.showEditDialog(it) },
                                onDeleteClick = { if (!it.isDefault) categoryToDelete = it }
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
    if (uiState.showAddDialog || uiState.editingCategory != null) {
        CategoryDialog(
            category = uiState.editingCategory,
            onDismiss = {
                if (uiState.editingCategory != null) viewModel.hideEditDialog()
                else viewModel.hideAddDialog()
            },
            onConfirm = { name, colorHex, iconName ->
                if (uiState.editingCategory != null) {
                    viewModel.updateCategory(
                        uiState.editingCategory!!.copy(name = name, colorHex = colorHex, iconName = iconName)
                    )
                } else {
                    viewModel.createCategory(name, colorHex, iconName)
                }
            }
        )
    }

    // Delete confirmation
    categoryToDelete?.let { category ->
        DeleteConfirmationDialog(
            title = "Delete Category?",
            message = "Are you sure you want to delete \"${category.name}\"?",
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                viewModel.deleteCategory(category)
                categoryToDelete = null
            }
        )
    }
}

@Composable
private fun CategoryListItem(
    category: Category,
    onEditClick: (Category) -> Unit,
    onDeleteClick: (Category) -> Unit,
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
            // Icon
            Icon(
                imageVector = IconHelper.getIconWithTheme(category.iconName),
                contentDescription = null,
                tint = Color(android.graphics.Color.parseColor(category.colorHex)),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (category.isDefault) {
                    Text(
                        text = "Default",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = { onEditClick(category) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }

            if (!category.isDefault) {
                IconButton(onClick = { onDeleteClick(category) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun CategoryDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedColor by remember { mutableStateOf(category?.colorHex ?: categoryColors[0].first) }
    var selectedIcon by remember { mutableStateOf(category?.iconName ?: "Category") }
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
        title = { Text(if (category == null) "Add Category" else "Edit Category") },
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
                    label = { Text("Category Name *") },
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
                                imageVector = IconHelper.getIconWithTheme(selectedIcon),
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

                // Color picker
                Column {
                    Text(
                        text = "Color",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categoryColors) { (colorHex, colorName) ->
                            ColorOption(
                                colorHex = colorHex,
                                isSelected = selectedColor == colorHex,
                                onClick = { selectedColor = colorHex }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedColor, selectedIcon)
                    } else {
                        error = true
                    }
                }
            ) {
                Text(if (category == null) "Add" else "Save")
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
private fun ColorOption(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = Color(android.graphics.Color.parseColor(colorHex)),
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.3f
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
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
