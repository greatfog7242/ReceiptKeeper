package com.receiptkeeper.core.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.receiptkeeper.core.util.BackupPreferences
import com.receiptkeeper.core.util.BackupRestoreService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker for performing automatic backups
 * Scheduled to run daily at 5:00 AM
 */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backupRestoreService: BackupRestoreService,
    private val backupPreferences: BackupPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "BackupWorker starting automatic backup")
        
        // Check if auto-backup is enabled
        if (!backupPreferences.isAutoBackupEnabled) {
            Log.d(TAG, "Auto-backup is disabled, skipping")
            return@withContext Result.success()
        }
        
        try {
            // Perform daily backup (uses fixed path, overwrites previous daily backup)
            val (success, message) = backupRestoreService.createDailyBackup()
            
            if (success) {
                Log.d(TAG, "BackupWorker completed successfully: $message")
                // Update last backup time
                backupPreferences.lastBackupTime = System.currentTimeMillis()
                Result.success()
            } else {
                Log.e(TAG, "BackupWorker failed: $message")
                // Retry with exponential backoff if backup fails
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "BackupWorker exception: ${e.message}", e)
            // Retry with exponential backoff
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "receipt_keeper_backup_worker"
        private const val TAG = "BackupWorker"
    }
}