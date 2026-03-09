package com.receiptkeeper.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.core.util.BackupPreferences
import com.receiptkeeper.core.util.BackupRestoreService
import com.receiptkeeper.core.work.BackupScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for backup and restore operations
 */
@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRestoreService: BackupRestoreService,
    private val backupScheduler: BackupScheduler,
    private val backupPreferences: BackupPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    init {
        // Load initial state
        loadAutoBackupStatus()
    }

    /**
     * Loads the list of available backups
     */
    fun loadBackups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val backups = backupRestoreService.listBackups()
                _uiState.update {
                    it.copy(
                        backups = backups,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load backups: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Loads auto-backup status
     */
    fun loadAutoBackupStatus() {
        viewModelScope.launch {
            try {
                val isEnabled = backupPreferences.isAutoBackupEnabled
                val lastBackupTime = backupPreferences.lastBackupTime
                val isScheduled = backupScheduler.isBackupScheduled()
                
                _uiState.update {
                    it.copy(
                        isAutoBackupEnabled = isEnabled,
                        lastBackupTime = lastBackupTime,
                        isBackupScheduled = isScheduled
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to load auto-backup status: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Toggles auto-backup enabled/disabled
     */
    fun toggleAutoBackup(enabled: Boolean) {
        backupPreferences.isAutoBackupEnabled = enabled
        _uiState.update { it.copy(isAutoBackupEnabled = enabled) }
        
        if (enabled) {
            backupScheduler.scheduleDailyBackup()
        } else {
            backupScheduler.cancelDailyBackup()
        }
    }

    /**
     * Creates a new backup
     */
    fun createBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingBackup = true, error = null) }
            try {
                val (success, message) = backupRestoreService.createBackup()
                if (success) {
                    _uiState.update {
                        it.copy(
                            isCreatingBackup = false,
                            lastBackupPath = message,
                            showSuccessMessage = true,
                            successMessage = "Backup created successfully: ${message.substringAfterLast("/")}"
                        )
                    }
                    // Reload backups list
                    loadBackups()
                } else {
                    _uiState.update {
                        it.copy(
                            isCreatingBackup = false,
                            error = message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingBackup = false,
                        error = "Backup failed: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Creates a backup via WorkManager (for testing automatic backup)
     */
    fun createBackupViaWorkManager() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingBackup = true, error = null) }
            try {
                backupScheduler.triggerImmediateBackup()
                _uiState.update {
                    it.copy(
                        isCreatingBackup = false,
                        showSuccessMessage = true,
                        successMessage = "Immediate backup triggered via WorkManager. Check logs for status."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingBackup = false,
                        error = "Failed to trigger WorkManager backup: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Loads battery optimization status
     */
    fun loadBatteryOptimizationStatus() {
        viewModelScope.launch {
            try {
                val isIgnoring = backupScheduler.isIgnoringBatteryOptimizations()
                val statusMessage = backupScheduler.checkBatteryOptimizationStatus()
                val isScheduled = backupScheduler.isBackupScheduled()
                
                _uiState.update {
                    it.copy(
                        isIgnoringBatteryOptimizations = isIgnoring,
                        batteryOptimizationStatus = statusMessage,
                        isBackupScheduled = isScheduled
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to load battery optimization status: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Requests battery optimization exemption
     */
    fun requestBatteryOptimizationExemption() {
        backupScheduler.requestIgnoreBatteryOptimizations()
    }

    /**
     * Restores from a backup
     * @param backupPath Path to the backup file to restore from
     */
    fun restoreBackup(backupPath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoringBackup = true, error = null) }
            try {
                val (success, message) = backupRestoreService.restoreBackup(backupPath)
                if (success) {
                    _uiState.update {
                        it.copy(
                            isRestoringBackup = false,
                            showSuccessMessage = true,
                            successMessage = "Restore completed successfully. Please restart the app."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isRestoringBackup = false,
                            error = message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRestoringBackup = false,
                        error = "Restore failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Deletes a backup
     * @param backupPath Path to the backup file to delete
     */
    fun deleteBackup(backupPath: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val success = backupRestoreService.deleteBackup(backupPath)
                if (success) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showSuccessMessage = true,
                            successMessage = "Backup deleted successfully"
                        )
                    }
                    // Reload backups list
                    loadBackups()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to delete backup"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Delete failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Dismisses the error message
     */
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Dismisses the success message
     */
    fun dismissSuccessMessage() {
        _uiState.update { it.copy(showSuccessMessage = false, successMessage = null) }
    }

    /**
     * Sets the backup to restore
     */
    fun setBackupToRestore(backupPath: String?) {
        _uiState.update { it.copy(backupToRestore = backupPath) }
    }

    /**
     * Shows the restore confirmation dialog
     */
    fun showRestoreConfirmation(backupPath: String) {
        _uiState.update {
            it.copy(
                backupToRestore = backupPath,
                showRestoreConfirmation = true
            )
        }
    }

    /**
     * Hides the restore confirmation dialog
     */
    fun hideRestoreConfirmation() {
        _uiState.update { it.copy(showRestoreConfirmation = false) }
    }

    /**
     * Shows the delete confirmation dialog
     */
    fun showDeleteConfirmation(backupPath: String) {
        _uiState.update {
            it.copy(
                backupToDelete = backupPath,
                showDeleteConfirmation = true
            )
        }
    }

    /**
     * Hides the delete confirmation dialog
     */
    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }
}

/**
 * UI state for backup and restore operations
 */
data class BackupRestoreUiState(
    val backups: List<Pair<String, String>> = emptyList(),
    val isLoading: Boolean = false,
    val isCreatingBackup: Boolean = false,
    val isRestoringBackup: Boolean = false,
    val error: String? = null,
    val showSuccessMessage: Boolean = false,
    val successMessage: String? = null,
    val backupToRestore: String? = null,
    val backupToDelete: String? = null,
    val showRestoreConfirmation: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val lastBackupPath: String? = null,
    // Battery optimization status
    val isIgnoringBatteryOptimizations: Boolean = false,
    val batteryOptimizationStatus: String = "",
    val isBackupScheduled: Boolean = false,
    // Auto-backup settings
    val isAutoBackupEnabled: Boolean = true,
    val lastBackupTime: Long = 0
)