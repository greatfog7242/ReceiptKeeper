package com.receiptkeeper.core.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles image operations for receipts
 * - Saves images to app-specific directory
 * - Generates unique filenames
 * - Cleans up deleted images
 */
@Singleton
class ImageHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val receiptsDir: File by lazy {
        File(context.filesDir, "receipts").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Saves an image from URI to app-specific directory
     * @param sourceUri URI from photo picker or camera
     * @return File URI string for database storage, or null on error
     */
    suspend fun saveImage(sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return@withContext null

            // Generate unique filename
            val filename = "receipt_${UUID.randomUUID()}.jpg"
            val destinationFile = File(receiptsDir, filename)

            // Copy file
            inputStream.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Return file URI
            destinationFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes an image file
     * @param imageUri File path to delete
     */
    suspend fun deleteImage(imageUri: String?) = withContext(Dispatchers.IO) {
        if (imageUri == null) return@withContext

        try {
            val file = File(imageUri)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Gets a content URI for sharing/viewing an image
     * @param imagePath Absolute file path
     * @return Content URI for FileProvider
     */
    fun getContentUri(imagePath: String): Uri {
        val file = File(imagePath)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Checks if an image file exists
     */
    fun imageExists(imageUri: String?): Boolean {
        if (imageUri == null) return false
        return File(imageUri).exists()
    }

    /**
     * Gets the receipts directory
     */
    fun getReceiptsDirectory(): File = receiptsDir
}
