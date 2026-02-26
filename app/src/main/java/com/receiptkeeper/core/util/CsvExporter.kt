package com.receiptkeeper.core.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.receiptkeeper.domain.model.Receipt
import java.io.File
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Utility class for exporting receipts to CSV format
 */
object CsvExporter {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Export receipts to CSV file and save to Downloads folder
     * Returns the URI of the saved file
     */
    fun exportToCSV(
        context: Context,
        receipts: List<Receipt>,
        vendorNames: Map<Long, String>,
        categoryNames: Map<Long, String>,
        bookNames: Map<Long, String>,
        paymentMethodNames: Map<Long, String>
    ): Uri? {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val fileName = "receipts_export_$timestamp.csv"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    // Write header
                    writer.write("Date,Vendor,Category,Book,Payment Method,Amount,Notes\n")

                    // Write data rows
                    receipts.forEach { receipt ->
                        val date = receipt.transactionDate.format(dateFormatter)
                        val vendor = vendorNames[receipt.vendorId] ?: ""
                        val category = categoryNames[receipt.categoryId] ?: ""
                        val book = bookNames[receipt.bookId] ?: ""
                        val paymentMethod = paymentMethodNames[receipt.paymentMethodId] ?: ""
                        val amount = String.format("%.2f", receipt.totalAmount)
                        val notes = receipt.notes?.replace("\"", "\"\"") ?: ""

                        writer.write("\"$date\",\"$vendor\",\"$category\",\"$book\",\"$paymentMethod\",\"$amount\",\"$notes\"\n")
                    }
                }
            }
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Legacy method - now just opens the saved file
     * Kept for compatibility
     */
    fun shareCSV(context: Context, file: File) {
        // Now just show a toast that file is saved
        // The exportToCSV method already saves to Downloads
    }

    /**
     * Save CSV to Downloads and return the URI
     */
    fun saveToDownloads(
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
