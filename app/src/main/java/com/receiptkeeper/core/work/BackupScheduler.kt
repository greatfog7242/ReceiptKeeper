package com.receiptkeeper.core.work

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules automatic backups at 5:00 AM daily
 */
@Singleton
class BackupScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Schedules the daily backup at 5:00 AM
     */
    fun scheduleDailyBackup() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Cancel any existing backup work
                WorkManager.getInstance(context).cancelUniqueWork(BackupWorker.WORK_NAME)

                // Create constraints for the backup work
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Backup doesn't need network
                    .setRequiresBatteryNotLow(true) // Don't run if battery is low
                    .setRequiresStorageNotLow(true) // Don't run if storage is low
                    .build()

                // Create a PeriodicWorkRequest that runs daily at 5:00 AM
                val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
                    24, // Repeat interval (24 hours)
                    TimeUnit.HOURS
                )
                    .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        30, // Initial backoff delay in minutes
                        TimeUnit.MINUTES
                    )
                    .addTag("backup")
                    .build()

                // Enqueue the work with a unique name
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    BackupWorker.WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP, // Keep existing schedule if already scheduled
                    backupRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Cancels the scheduled backup
     */
    fun cancelDailyBackup() {
        WorkManager.getInstance(context).cancelUniqueWork(BackupWorker.WORK_NAME)
    }

    /**
     * Checks if backup is scheduled
     */
    suspend fun isBackupScheduled(): Boolean {
        val workInfo = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(BackupWorker.WORK_NAME)
            .await()
        
        return workInfo.any { it.state == WorkInfo.State.ENQUEUED }
    }

    /**
     * Calculates the initial delay to run at 5:00 AM
     */
    private fun calculateInitialDelay(): Long {
        val now = System.currentTimeMillis()
        
        // Get current time in milliseconds
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now
        
        // Set target time to 5:00 AM
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 5)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        // If it's already past 5:00 AM today, schedule for tomorrow
        if (calendar.timeInMillis <= now) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        
        return calendar.timeInMillis - now
    }

    /**
     * Triggers an immediate backup (for testing or manual trigger)
     */
    fun triggerImmediateBackup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints)
            .addTag("immediate_backup")
            .build()

        WorkManager.getInstance(context).enqueue(backupRequest)
    }
}