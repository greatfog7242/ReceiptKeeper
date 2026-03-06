package com.receiptkeeper.core.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.receiptkeeper.data.local.dao.ReceiptDao
import java.io.RandomAccessFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for backing up and restoring the database and images
 * Uses VACUUM INTO for database export and creates timestamped folders in Downloads/雪松堡账本
 */
@Singleton
class BackupRestoreService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageHandler: ImageHandler,
    private val receiptDao: ReceiptDao,
    private val database: com.receiptkeeper.core.database.ReceiptDatabase
) {
    companion object {
        private const val BACKUP_FOLDER_NAME = "雪松堡账本"
        private const val DAILY_BACKUP_FOLDER_NAME = "DailyBackup"
        private const val DATABASE_BACKUP_NAME = "receipt_keeper_backup.db"
        private const val IMAGES_SUBFOLDER = "images"
        private const val BACKUP_ZIP_NAME = "receipt_keeper_backup.zip"
        private const val BACKUP_METADATA_FILE = "backup_info.txt"
        
        private val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
    }

    /**
     * Creates a daily backup with fixed path (overwrites previous daily backup)
     * Used for automatic daily backups
     * @return Pair of success status and message
     */
    suspend fun createDailyBackup(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            println("=== Starting daily backup creation ===")
            
            // Create daily backup directory (fixed path)
            val dailyBackupDir = getDailyBackupDirectory()
            println("Daily backup directory: $dailyBackupDir")
            
            // Export database using VACUUM INTO
            val dbBackupFile = File(dailyBackupDir, DATABASE_BACKUP_NAME)
            println("Database backup file: $dbBackupFile")
            
            val dbExportSuccess = exportDatabase(dbBackupFile)
            println("Database export success: $dbExportSuccess")
            
            if (!dbExportSuccess) {
                return@withContext Pair(false, "Failed to export database")
            }
            
            // Copy all receipt images to images subfolder
            val imagesDir = File(dailyBackupDir, IMAGES_SUBFOLDER)
            imagesDir.mkdirs()
            println("Images directory: $imagesDir")
            
            val imageCopySuccess = copyReceiptImages(imagesDir)
            println("Image copy success: $imageCopySuccess")
            
            if (!imageCopySuccess) {
                return@withContext Pair(false, "Failed to copy images")
            }
            
            // Create backup metadata file
            createBackupMetadata(dailyBackupDir, LocalDateTime.now().format(timestampFormatter))
            println("Created backup metadata")
            
            println("Daily backup completed successfully")
            Pair(true, "Daily backup completed: ${dailyBackupDir.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Daily backup failed with exception: ${e.message}")
            Pair(false, "Daily backup failed: ${e.message}")
        }
    }

    /**
     * Creates a backup of the database and all receipt images with timestamp
     * Used for manual backups (creates timestamped zip file)
     * @return Pair of success status and backup file path (or error message)
     */
    suspend fun createBackup(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            println("=== Starting backup creation ===")
            
            // Create backup directory with timestamp
            val timestamp = LocalDateTime.now().format(timestampFormatter)
            println("Backup timestamp: $timestamp")
            
            val backupDir = createBackupDirectory(timestamp)
            println("Backup directory: $backupDir")
            
            // Export database using VACUUM INTO
            val dbBackupFile = File(backupDir, DATABASE_BACKUP_NAME)
            println("Database backup file: $dbBackupFile")
            
            val dbExportSuccess = exportDatabase(dbBackupFile)
            println("Database export success: $dbExportSuccess")
            
            if (!dbExportSuccess) {
                return@withContext Pair(false, "Failed to export database")
            }
            
            // Copy all receipt images
            val imagesDir = File(backupDir, IMAGES_SUBFOLDER)
            imagesDir.mkdirs()
            println("Images directory: $imagesDir")
            
            val imageCopySuccess = copyReceiptImages(imagesDir)
            println("Image copy success: $imageCopySuccess")
            
            if (!imageCopySuccess) {
                return@withContext Pair(false, "Failed to copy images")
            }
            
            // Create backup metadata file
            createBackupMetadata(backupDir, timestamp)
            println("Created backup metadata")
            
            // Create zip archive of the backup
            val zipFile = File(backupDir.parentFile, "receipt_keeper_backup_$timestamp.zip")
            println("Creating zip archive: $zipFile")
            
            val zipSuccess = createZipArchive(backupDir, zipFile)
            println("Zip creation success: $zipSuccess")
            
            if (!zipSuccess) {
                return@withContext Pair(false, "Failed to create zip archive")
            }
            
            // Clean up the unzipped backup directory (keep only the zip)
            backupDir.deleteRecursively()
            println("Cleaned up temporary directory")
            
            val zipSize = zipFile.length()
            println("Backup completed: $zipFile, size: $zipSize bytes")
            
            Pair(true, zipFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Backup failed with exception: ${e.message}")
            Pair(false, "Backup failed: ${e.message}")
        }
    }

    /**
     * Restores database and images from a backup
     * @param backupZipPath Path to the backup zip file
     * @return Pair of success status and message
     */
    suspend fun restoreBackup(backupZipPath: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val backupZipFile = File(backupZipPath)
            if (!backupZipFile.exists()) {
                return@withContext Pair(false, "Backup file not found")
            }
            
            // Extract backup to temp directory
            val tempDir = File(context.cacheDir, "restore_temp_${System.currentTimeMillis()}")
            tempDir.mkdirs()
            
            val extractSuccess = extractZipArchive(backupZipFile, tempDir)
            if (!extractSuccess) {
                tempDir.deleteRecursively()
                return@withContext Pair(false, "Failed to extract backup archive")
            }
            
            // Find database backup file
            val dbBackupFile = File(tempDir, DATABASE_BACKUP_NAME)
            if (!dbBackupFile.exists()) {
                tempDir.deleteRecursively()
                return@withContext Pair(false, "Database backup not found in archive")
            }
            
            // Import database
            val dbImportSuccess = importDatabase(dbBackupFile)
            if (!dbImportSuccess) {
                tempDir.deleteRecursively()
                return@withContext Pair(false, "Failed to import database")
            }
            
            // Copy images back to app directory
            val imagesDir = File(tempDir, IMAGES_SUBFOLDER)
            if (imagesDir.exists()) {
                copyImagesToAppDirectory(imagesDir)
            }
            
            // Clean up temp directory
            tempDir.deleteRecursively()
            
            Pair(true, "Restore completed successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "Restore failed: ${e.message}")
        }
    }

    /**
     * Lists all available backups in the backup directory
     * @return List of backup file paths with timestamps
     */
    suspend fun listBackups(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val backups = mutableListOf<Pair<String, String>>()
        
        try {
            val backupParentDir = getBackupParentDirectory()
            if (!backupParentDir.exists()) {
                return@withContext backups
            }
            
            backupParentDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".zip") && file.name.contains("receipt_keeper_backup_")) {
                    val timestamp = extractTimestampFromFilename(file.name)
                    backups.add(Pair(file.absolutePath, timestamp))
                }
            }
            
            // Sort by timestamp (newest first)
            backups.sortByDescending { it.second }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        backups
    }

    /**
     * Deletes a backup file
     * @param backupPath Path to the backup file to delete
     * @return Success status
     */
    suspend fun deleteBackup(backupPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupPath)
            if (backupFile.exists()) {
                backupFile.delete()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Creates a backup directory with the given timestamp
     */
    private fun createBackupDirectory(timestamp: String): File {
        val backupParentDir = getBackupParentDirectory()
        val backupDir = File(backupParentDir, "backup_$timestamp")
        backupDir.mkdirs()
        return backupDir
    }

    /**
     * Gets the parent backup directory (Downloads/雪松堡账本)
     */
    private fun getBackupParentDirectory(): File {
        return try {
            // For all Android versions, try to use the traditional approach first
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val backupDir = File(downloadsDir, BACKUP_FOLDER_NAME)
            
            // Create directory if it doesn't exist
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            backupDir
        } catch (e: Exception) {
            // Fallback to app-specific directory if external storage is not accessible
            val fallbackDir = File(context.getExternalFilesDir(null), BACKUP_FOLDER_NAME)
            fallbackDir.mkdirs()
            fallbackDir
        }
    }

    /**
     * Gets the daily backup directory (Downloads/雪松堡账本/DailyBackup)
     */
    private fun getDailyBackupDirectory(): File {
        return try {
            val parentDir = getBackupParentDirectory()
            val dailyBackupDir = File(parentDir, DAILY_BACKUP_FOLDER_NAME)
            
            // Create directory if it doesn't exist
            if (!dailyBackupDir.exists()) {
                dailyBackupDir.mkdirs()
            }
            
            dailyBackupDir
        } catch (e: Exception) {
            // Fallback to app-specific directory
            val fallbackDir = File(context.getExternalFilesDir(null), "$BACKUP_FOLDER_NAME/$DAILY_BACKUP_FOLDER_NAME")
            fallbackDir.mkdirs()
            fallbackDir
        }
    }

    /**
     * Exports database using VACUUM INTO command
     */
    private fun exportDatabase(destinationFile: File): Boolean {
        return try {
            // Get the database file path
            val dbFile = context.getDatabasePath("receipt_keeper_db")
            
            if (!dbFile.exists()) {
                println("Database file doesn't exist: $dbFile")
                return false
            }
            
            println("Database file exists: $dbFile, size: ${dbFile.length()} bytes")
            
            // Execute VACUUM INTO to create a clean backup using the existing database connection
            database.openHelper.writableDatabase.execSQL(
                "VACUUM INTO '${destinationFile.absolutePath}'"
            )
            
            // Verify the backup was created
            if (!destinationFile.exists()) {
                println("Backup file not created: $destinationFile")
                return false
            }
            
            val backupSize = destinationFile.length()
            println("Backup created: $destinationFile, size: $backupSize bytes")
            
            if (backupSize == 0L) {
                println("Backup file is empty!")
                destinationFile.delete()
                return false
            }
            
            // Verify it's a valid SQLite database
            val isValid = isSqliteDatabaseValid(destinationFile)
            println("Backup database valid: $isValid")
            
            isValid
        } catch (e: Exception) {
            e.printStackTrace()
            println("Export database error: ${e.message}")
            false
        }
    }

    /**
     * Imports database from backup file
     */
    private fun importDatabase(sourceFile: File): Boolean {
        return try {
            println("Starting database import from: $sourceFile")
            println("Source file size: ${sourceFile.length()} bytes")
            
            // First verify the backup is a valid SQLite database
            if (!isSqliteDatabaseValid(sourceFile)) {
                println("Backup file is not a valid SQLite database")
                return false
            }
            
            // Get the current database file path
            val dbFile = context.getDatabasePath("receipt_keeper_db")
            println("Current database path: $dbFile")
            
            // Close any open database connections
            try {
                database.close()
                println("Closed existing database connection")
            } catch (e: Exception) {
                println("Warning: Could not close database: ${e.message}")
            }
            
            // Delete the existing database files (main db + WAL journal)
            val dbDir = dbFile.parentFile
            if (dbDir != null && dbDir.exists()) {
                dbDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("receipt_keeper_db")) {
                        println("Deleting old database file: ${file.name}")
                        file.delete()
                    }
                }
            }
            
            // Copy the backup file to the database location
            println("Copying backup to: $dbFile")
            sourceFile.copyTo(dbFile, overwrite = true)
            
            // Verify the copy succeeded
            if (!dbFile.exists()) {
                println("Database file not created after copy")
                return false
            }
            
            val newSize = dbFile.length()
            println("New database size: $newSize bytes")
            
            if (newSize == 0L) {
                println("Database file is empty after copy")
                return false
            }
            
            // Verify the new database is valid
            if (!isSqliteDatabaseValid(dbFile)) {
                println("Restored database is not valid")
                return false
            }
            
            println("Database import successful")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            println("Import database error: ${e.message}")
            false
        }
    }

    /**
     * Copies all receipt images to backup directory
     */
    private fun copyReceiptImages(destinationDir: File): Boolean {
        return try {
            val receiptsDir = imageHandler.getReceiptsDirectory()
            if (!receiptsDir.exists()) {
                return true // No images to copy is not an error
            }
            
            receiptsDir.listFiles()?.forEach { imageFile ->
                if (imageFile.isFile && (imageFile.name.endsWith(".jpg") || imageFile.name.endsWith(".jpeg") || imageFile.name.endsWith(".png"))) {
                    val destFile = File(destinationDir, imageFile.name)
                    imageFile.copyTo(destFile, overwrite = true)
                }
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Copies images from backup to app directory
     */
    private fun copyImagesToAppDirectory(sourceDir: File) {
        try {
            val receiptsDir = imageHandler.getReceiptsDirectory()
            receiptsDir.mkdirs()
            
            sourceDir.listFiles()?.forEach { imageFile ->
                if (imageFile.isFile) {
                    val destFile = File(receiptsDir, imageFile.name)
                    imageFile.copyTo(destFile, overwrite = true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Creates backup metadata file
     */
    private fun createBackupMetadata(backupDir: File, timestamp: String) {
        try {
            val metadataFile = File(backupDir, BACKUP_METADATA_FILE)
            metadataFile.writeText(
                """
                ReceiptKeeper Backup
                Timestamp: $timestamp
                Created: ${LocalDateTime.now()}
                Database: $DATABASE_BACKUP_NAME
                Images: ${IMAGES_SUBFOLDER}/
                
                To restore:
                1. Extract this zip file
                2. Copy $DATABASE_BACKUP_NAME to app database location
                3. Copy images from $IMAGES_SUBFOLDER/ to app receipts directory
                """.trimIndent()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Creates a zip archive of the backup directory
     */
    private fun createZipArchive(sourceDir: File, destinationFile: File): Boolean {
        return try {
            ZipOutputStream(FileOutputStream(destinationFile)).use { zipOut ->
                sourceDir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        val relativePath = sourceDir.toPath().relativize(file.toPath()).toString()
                        val zipEntry = ZipEntry(relativePath)
                        zipOut.putNextEntry(zipEntry)
                        
                        FileInputStream(file).use { fis ->
                            fis.copyTo(zipOut)
                        }
                        
                        zipOut.closeEntry()
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Extracts a zip archive
     */
    private fun extractZipArchive(sourceFile: File, destinationDir: File): Boolean {
        return try {
            ZipInputStream(FileInputStream(sourceFile)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val entryFile = File(destinationDir, entry.name)
                    
                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        entryFile.parentFile?.mkdirs()
                        
                        FileOutputStream(entryFile).use { fos ->
                            zipIn.copyTo(fos)
                        }
                    }
                    
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Extracts timestamp from backup filename
     */
    private fun extractTimestampFromFilename(filename: String): String {
        val pattern = "receipt_keeper_backup_(\\d{8}_\\d{6})\\.zip".toRegex()
        val match = pattern.find(filename)
        return match?.groupValues?.get(1) ?: "Unknown"
    }

    /**
     * Checks if a file is a valid SQLite database
     */
    private fun isSqliteDatabaseValid(file: File): Boolean {
        return try {
            if (!file.exists() || file.length() < 100) {
                return false
            }
            
            RandomAccessFile(file, "r").use { raf ->
                // Check SQLite magic header
                val magic = ByteArray(16)
                raf.readFully(magic)
                if (String(magic) != "SQLite format 3\u0000") {
                    println("Invalid SQLite magic: ${String(magic)}")
                    return false
                }
                
                // Read page size
                raf.seek(16)
                val pageSize = raf.readShort().toInt() and 0xFFFF
                if (pageSize !in 512..65536) {
                    println("Invalid page size: $pageSize")
                    return false
                }
                
                // Read page count
                raf.seek(28)
                val pageCount = raf.readInt()
                val expectedSize = pageSize.toLong() * pageCount
                val actualSize = file.length()
                
                // Allow small difference for WAL/shm files
                if (actualSize < expectedSize) {
                    println("File truncated: expected $expectedSize, got $actualSize")
                    return false
                }
                
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}