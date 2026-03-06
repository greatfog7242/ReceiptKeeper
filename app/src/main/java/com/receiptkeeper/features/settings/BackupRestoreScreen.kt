package com.receiptkeeper.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.receiptkeeper.R
import kotlinx.coroutines.launch

/**
 * Screen for backup and restore operations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    actionLabel = "Dismiss"
                )
                viewModel.dismissError()
            }
        }
    }

    // Show success snackbar
    LaunchedEffect(uiState.showSuccessMessage) {
        if (uiState.showSuccessMessage && uiState.successMessage != null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = uiState.successMessage!!,
                    actionLabel = "OK"
                )
                viewModel.dismissSuccessMessage()
            }
        }
    }

    // Load backups and battery optimization status on first launch
    LaunchedEffect(Unit) {
        viewModel.loadBackups()
        viewModel.loadBatteryOptimizationStatus()
    }

    // Restore confirmation dialog
    if (uiState.showRestoreConfirmation && uiState.backupToRestore != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideRestoreConfirmation() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Restore Backup")
            },
            text = {
                Text(
                    "Are you sure you want to restore from this backup? " +
                    "This will replace your current data. " +
                    "The app will need to be restarted after restoration."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restoreBackup(uiState.backupToRestore!!)
                        viewModel.hideRestoreConfirmation()
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideRestoreConfirmation() }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (uiState.showDeleteConfirmation && uiState.backupToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Delete Backup")
            },
            text = {
                Text("Are you sure you want to delete this backup? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBackup(uiState.backupToDelete!!)
                        viewModel.hideDeleteConfirmation()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideDeleteConfirmation() }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Backup section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Create Backup",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Backups are saved to: Downloads/雪松堡账本/",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.createBackup() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isCreatingBackup
                    ) {
                        if (uiState.isCreatingBackup) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Creating Backup...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Backup Now")
                        }
                    }
                    
                     if (uiState.lastBackupPath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Last backup: ${uiState.lastBackupPath!!.substringAfterLast("/")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Test WorkManager backup button
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.createBackupViaWorkManager() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isCreatingBackup
                    ) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test WorkManager Backup")
                    }
                }
            }

            // Battery optimization status section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Automatic Backup Status",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Battery optimization status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isIgnoringBatteryOptimizations) 
                                Icons.Default.BatteryFull 
                            else 
                                Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = if (uiState.isIgnoringBatteryOptimizations) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (uiState.isIgnoringBatteryOptimizations) 
                                    "Battery optimization: Disabled ✓" 
                                else 
                                    "Battery optimization: Enabled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.isIgnoringBatteryOptimizations) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = uiState.batteryOptimizationStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Backup schedule status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.isBackupScheduled) 
                                Icons.Default.Schedule 
                            else 
                                Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (uiState.isBackupScheduled) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (uiState.isBackupScheduled) 
                                    "Backup scheduled: Daily at 5:00 AM ✓" 
                                else 
                                    "Backup not scheduled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (uiState.isBackupScheduled) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action buttons
                    if (!uiState.isIgnoringBatteryOptimizations) {
                        Button(
                            onClick = { viewModel.requestBatteryOptimizationExemption() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Disable Battery Optimization")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Note: Battery optimization may prevent automatic backups from running in the background.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Restore section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Restore from Backup",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Select a backup to restore from:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.backups.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No backups found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            items(uiState.backups) { (backupPath, timestamp) ->
                                BackupItem(
                                    backupPath = backupPath,
                                    timestamp = timestamp,
                                    onRestoreClick = { viewModel.showRestoreConfirmation(backupPath) },
                                    onDeleteClick = { viewModel.showDeleteConfirmation(backupPath) },
                                    isRestoring = uiState.isRestoringBackup && uiState.backupToRestore == backupPath
                                )
                            }
                        }
                    }
                    
                    if (uiState.isRestoringBackup) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Restoring backup...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Information section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Backup Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    InfoRow(
                        icon = Icons.Default.Info,
                        title = "What's included",
                        description = "Database (receipts, books, categories, vendors, payment methods, goals) and all receipt images"
                    )
                    
                    InfoRow(
                        icon = Icons.Default.Folder,
                        title = "Storage location",
                        description = "Downloads/雪松堡账本/backup_YYYYMMDD_HHMMSS.zip"
                    )
                    
                    InfoRow(
                        icon = Icons.Default.Schedule,
                        title = "Automatic backups",
                        description = "Daily at 5:00 AM (if device is on and app has been used)"
                    )
                    
                    InfoRow(
                        icon = Icons.Default.Warning,
                        title = "Important notes",
                        description = "Restoring will replace all current data. The app needs to be restarted after restoration."
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Composable for displaying a backup item
 */
@Composable
private fun BackupItem(
    backupPath: String,
    timestamp: String,
    onRestoreClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isRestoring: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = formatTimestamp(timestamp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = backupPath.substringAfterLast("/"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                if (isRestoring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onRestoreClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Restore",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Composable for displaying information rows
 */
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Formats a timestamp string for display
 */
private fun formatTimestamp(timestamp: String): String {
    return try {
        val year = timestamp.substring(0, 4)
        val month = timestamp.substring(4, 6)
        val day = timestamp.substring(6, 8)
        val hour = timestamp.substring(9, 11)
        val minute = timestamp.substring(11, 13)
        val second = timestamp.substring(13, 15)
        "$year-$month-$day $hour:$minute:$second"
    } catch (e: Exception) {
        timestamp
    }
}

@Preview(showBackground = true)
@Composable
fun BackupRestoreScreenPreview() {
    MaterialTheme {
        BackupRestoreScreen(onNavigateBack = {})
    }
}