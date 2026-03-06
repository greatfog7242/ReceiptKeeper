package com.receiptkeeper.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.receiptkeeper.core.work.BackupScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for ReceiptKeeper
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class ReceiptKeeperApp : Application(), Configuration.Provider {

    @Inject
    lateinit var backupScheduler: BackupScheduler

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // Schedule daily backup at 5:00 AM
        backupScheduler.scheduleDailyBackup()
    }
}
