package com.receiptkeeper.core.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
    companion object {
        private const val RECEIPT_IMAGE_QUALITY = 75
        private const val RECEIPT_IMAGE_EXTENSION = "webp"
    }

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
            // Generate unique filename
            val filename = "receipt_${UUID.randomUUID()}.$RECEIPT_IMAGE_EXTENSION"
            val destinationFile = File(receiptsDir, filename)

            val orientationDegrees = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                exifOrientationDegrees(input)
            } ?: 0f

            val compressed = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                compressImage(input, destinationFile, orientationDegrees)
            } ?: return@withContext null

            if (!compressed) {
                // Fallback: copy original bytes if compression fails
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
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
     * Recompress an existing image file to WebP.
     * @param imagePath Absolute file path
     * @return New file path, original path if already WebP, or null on failure
     */
    suspend fun recompressImageFile(imagePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(imagePath)
            if (!sourceFile.exists()) return@withContext null

            val extension = sourceFile.extension.lowercase()
            if (extension == RECEIPT_IMAGE_EXTENSION) {
                return@withContext sourceFile.absolutePath
            }

            val targetFile = File(sourceFile.parentFile, "${sourceFile.nameWithoutExtension}.$RECEIPT_IMAGE_EXTENSION")

            if (targetFile.exists()) {
                // If target already exists, remove old file and return target
                if (sourceFile.absolutePath != targetFile.absolutePath) {
                    sourceFile.delete()
                }
                return@withContext targetFile.absolutePath
            }

            val orientationDegrees = try {
                exifOrientationDegrees(ExifInterface(imagePath))
            } catch (_: Exception) { 0f }

            val compressed = sourceFile.inputStream().use { input ->
                compressImage(input, targetFile, orientationDegrees)
            }

            if (compressed) {
                sourceFile.delete()
                return@withContext targetFile.absolutePath
            }

            null
        } catch (e: Exception) {
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

    /**
     * Downloads an image to the device's Downloads folder
     * @param imagePath The file path of the image to download
     * @return Uri of the saved file, or null on failure
     */
    suspend fun downloadImageToGallery(imagePath: String): Uri? = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(imagePath)
            if (!sourceFile.exists()) return@withContext null

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val extension = sourceFile.extension.ifEmpty { "jpg" }
            val displayName = "Receipt_$timestamp.$extension"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeTypeForExtension(extension))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext null

            resolver.openOutputStream(uri)?.use { outputStream ->
                sourceFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun compressImage(inputStream: InputStream, destinationFile: File, orientationDegrees: Float = 0f): Boolean {
        val options = BitmapFactory.Options().apply {
            inMutable = true
        }

        val raw = BitmapFactory.decodeStream(inputStream, null, options) ?: return false

        val bitmap = if (orientationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(orientationDegrees) }
            val rotated = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
            if (rotated !== raw) raw.recycle()
            rotated
        } else {
            raw
        }

        return try {
            destinationFile.outputStream().use { output ->
                val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                bitmap.compress(format, RECEIPT_IMAGE_QUALITY, output)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    private fun exifOrientationDegrees(inputStream: InputStream): Float {
        return try {
            exifOrientationDegrees(ExifInterface(inputStream))
        } catch (_: Exception) { 0f }
    }

    private fun exifOrientationDegrees(exif: ExifInterface): Float {
        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }

    private fun mimeTypeForExtension(extension: String): String {
        return when (extension.lowercase()) {
            "webp" -> "image/webp"
            "png" -> "image/png"
            else -> "image/jpeg"
        }
    }
}
