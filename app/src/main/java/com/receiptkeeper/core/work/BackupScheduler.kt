package com.receiptkeeper.core.work

import android.content.Context
import android.util.Log
import androidx.work.*
import com.receiptkeeper.core.util.BatteryOptimizationHelper
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
                Log.d(TAG, "Scheduling daily backup at 5:00 AM")
                
                // Check battery optimization status
                val batteryOptimizationStatus = BatteryOptimizationHelper.checkBatteryOptimizationStatus(context)
                Log.d(TAG, batteryOptimizationStatus)
                
                // Create constraints for the backup work
                // Allow execution during Doze mode and low battery conditions
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Backup doesn't need network
                    .setRequiresDeviceIdle(false)    // Allow during Doze mode
                    .setRequiresBatteryNotLow(false) // Allow when battery is low
                    .setRequiresStorageNotLow(false) // Allow when storage is low
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

                // Use KEEP so re-scheduling on app restart doesn't reset the 24-hour timer.
                // REPLACE was cancelling any pending work and restarting the delay on every
                // app process start, which meant the backup never had a chance to fire.
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    BackupWorker.WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    backupRequest
                )
                
                Log.d(TAG, "Daily backup scheduled successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule daily backup: ${e.message}", e)
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
        
        val isScheduled = workInfo.any { it.state == WorkInfo.State.ENQUEUED }
        Log.d(TAG, "Backup scheduled: $isScheduled")
        return isScheduled
    }

    /**
     * Checks if battery optimization might prevent background work
     */
    fun checkBatteryOptimizationStatus(): String {
        return BatteryOptimizationHelper.checkBatteryOptimizationStatus(context)
    }

    /**
     * Checks if app is exempt from battery optimizations
     */
    fun isIgnoringBatteryOptimizations(): Boolean {
        return BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
    }

    /**
     * Opens battery optimization settings for user to grant permission
     */
    fun requestIgnoreBatteryOptimizations() {
        BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
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
        Log.d(TAG, "Triggering immediate backup")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresDeviceIdle(false)
            .setRequiresBatteryNotLow(false)
            .setRequiresStorageNotLow(false)
            .build()

        val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints)
            .addTag("immediate_backup")
            .build()

        WorkManager.getInstance(context).enqueue(backupRequest)
    }

    companion object {
        private const val TAG = "BackupScheduler"
    }
}