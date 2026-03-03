package com.receiptkeeper.app

import android.app.Application
import com.receiptkeeper.core.work.BackupScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for ReceiptKeeper
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class ReceiptKeeperApp : Application() {

    @Inject
    lateinit var backupScheduler: BackupScheduler

    override fun onCreate() {
        super.onCreate()
        
        // Schedule daily backup at 5:00 AM
        backupScheduler.scheduleDailyBackup()
    }
}
