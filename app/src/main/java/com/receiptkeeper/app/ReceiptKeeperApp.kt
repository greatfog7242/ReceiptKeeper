package com.receiptkeeper.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for ReceiptKeeper
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class ReceiptKeeperApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application initialization code here
    }
}
