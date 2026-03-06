package com.receiptkeeper.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

/**
 * Helper for managing battery optimization settings
 * Android may prevent background work (like automatic backups) when battery optimization is enabled
 */
object BatteryOptimizationHelper {

    private const val TAG = "BatteryOptimizationHelper"

    /**
     * Check if the app is exempt from battery optimizations
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            // Below Android M, battery optimization doesn't exist
            true
        }
    }

    /**
     * Request battery optimization exemption
     * Opens system settings for the user to grant permission
     */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Log.d(TAG, "Opened battery optimization settings")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open battery optimization settings: ${e.message}", e)
            }
        }
    }

    /**
     * Open battery optimization settings for this app
     */
    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Opened battery optimization settings page")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open battery optimization settings page: ${e.message}", e)
        }
    }

    /**
     * Check if battery optimization might affect background work
     */
    fun checkBatteryOptimizationStatus(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val isIgnoring = isIgnoringBatteryOptimizations(context)
            if (isIgnoring) {
                "Battery optimization is disabled for this app. Background work should run normally."
            } else {
                "Battery optimization is enabled. This may prevent automatic backups from running in the background."
            }
        } else {
            "Battery optimization not applicable (Android < M)."
        }
    }
}