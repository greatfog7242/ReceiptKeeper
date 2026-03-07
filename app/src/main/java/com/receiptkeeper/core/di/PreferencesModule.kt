package com.receiptkeeper.core.di

import android.content.Context
import com.receiptkeeper.core.preferences.IconThemeManager
import com.receiptkeeper.core.preferences.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for preferences dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideIconThemeManager(
        preferencesManager: PreferencesManager
    ): IconThemeManager {
        return IconThemeManager(preferencesManager)
    }
}