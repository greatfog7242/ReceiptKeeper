package com.receiptkeeper.core.di

import android.content.Context
import androidx.work.WorkManager
import com.receiptkeeper.core.util.BackupRestoreService
import com.receiptkeeper.core.work.BackupScheduler
import com.receiptkeeper.data.local.dao.ReceiptDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI module for WorkManager and related services
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideBackupScheduler(@ApplicationContext context: Context): BackupScheduler {
        return BackupScheduler(context)
    }
}