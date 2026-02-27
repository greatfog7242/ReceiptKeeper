package com.receiptkeeper.core.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.receiptkeeper.domain.model.Receipt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Utility class for exporting receipts to CSV format with images
 *
 * Export structure:
 *   ReceiptKeeper_YYYYMMDD_HHMMSS/
 *   ├── receipts.csv
 *   └── images/
 *       ├── receipt_xxx.jpg
 *       └── ...
 */
object CsvExporter {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private const val IMAGES_FOLDER = "images"

    /**
     * Export receipts to CSV file with images in a timestamped folder
     * Returns the URI of the saved folder, or null on failure
     */
    suspend fun exportToCSV(
        context: Context,
        receipts: List<Receipt>,
        vendorNames: Map<Long, String>,
        categoryNames: Map<Long, String>,
        bookNames: Map<Long, String>,
        paymentMethodNames: Map<Long, String>
    ): Uri? = withContext(Dispatchers.IO) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val folderName = "ReceiptKeeper_$timestamp"

        try {
            // Create the export folder and images subfolder
            val exportFolder = createExportFolder(context, folderName)
                ?: return@withContext null

            val imagesFolder = File(exportFolder, IMAGES_FOLDER)
            if (!imagesFolder.exists() && !imagesFolder.mkdirs()) {
                return@withContext null
            }

            // Copy images and build relative path map
            val imagePathMap = mutableMapOf<String, String>() // original path -> relative path
            receipts.forEach { receipt ->
                receipt.imageUri?.let { originalPath ->
                    val sourceFile = File(originalPath)
                    if (sourceFile.exists()) {
                        val extension = sourceFile.extension.ifEmpty { "jpg" }
                        val newFileName = "receipt_${receipt.id}_${UUID.randomUUID()}.$extension"
                        val destFile = File(imagesFolder, newFileName)

                        try {
                            sourceFile.inputStream().use { input ->
                                destFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            imagePathMap[originalPath] = "$IMAGES_FOLDER/$newFileName"
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            // Create CSV file in the export folder
            val csvFile = File(exportFolder, "receipts.csv")
            writeCSVFile(csvFile, receipts, vendorNames, categoryNames, bookNames, paymentMethodNames, imagePathMap)

            // Return URI of the export folder
            getFolderUri(context, folderName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Create export folder in Downloads
     */
    private fun createExportFolder(context: Context, folderName: String): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+, we need to create folder via MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, folderName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.android.package-archive")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$folderName")
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                // Create a placeholder file to create the folder
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(ByteArray(0))
                }
                // Delete the placeholder, we'll use file-based approach
                resolver.delete(it, null, null)
            }

            // Now access the folder via file
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName).apply {
                if (!exists()) mkdirs()
            }
        } else {
            // For older versions, use file directly
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName).apply {
                if (!exists()) mkdirs()
            }
        }
    }

    /**
     * Get URI for the export folder
     */
    private fun getFolderUri(context: Context, folderName: String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, folderName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.android.package-archive")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$folderName")
            }
            context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)?.let {
                // Return parent folder URI
                Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, folderName)
            }
        } else {
            val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName)
            Uri.fromFile(folder)
        }
    }

    /**
     * Write CSV file with relative image paths
     */
    private fun writeCSVFile(
        csvFile: File,
        receipts: List<Receipt>,
        vendorNames: Map<Long, String>,
        categoryNames: Map<Long, String>,
        bookNames: Map<Long, String>,
        paymentMethodNames: Map<Long, String>,
        imagePathMap: Map<String, String>
    ) {
        csvFile.outputStream().use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                // Write header with Image column
                writer.write("Date,Vendor,Category,Book,Payment Method,Amount,Notes,Image\n")

                // Write data rows
                receipts.forEach { receipt ->
                    val date = receipt.transactionDate.format(dateFormatter)
                    val vendor = escapeCSV(vendorNames[receipt.vendorId] ?: "")
                    val category = escapeCSV(categoryNames[receipt.categoryId] ?: "")
                    val book = escapeCSV(bookNames[receipt.bookId] ?: "")
                    val paymentMethod = escapeCSV(paymentMethodNames[receipt.paymentMethodId] ?: "")
                    val amount = String.format("%.2f", receipt.totalAmount)
                    val notes = escapeCSV(receipt.notes ?: "")
                    val imagePath = receipt.imageUri?.let { imagePathMap[it] } ?: ""

                    writer.write("$date,$vendor,$category,$book,$paymentMethod,$amount,$notes,$imagePath\n")
                }
            }
        }
    }

    /**
     * Escape CSV field value
     */
    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    /**
     * Legacy method - kept for compatibility
     */
    fun shareCSV(context: Context, file: File) {
        // Now just shows that file is saved
    }

    /**
     * Save CSV to Downloads and return the URI (now with images)
     */
    suspend fun saveToDownloads(
        context: Context,
        receipts: List<Receipt>,
        vendorNames: Map<Long, String>,
        categoryNames: Map<Long, String>,
        bookNames: Map<Long, String>,
        paymentMethodNames: Map<Long, String>
    ): Uri? {
        return exportToCSV(context, receipts, vendorNames, categoryNames, bookNames, paymentMethodNames)
    }
}
