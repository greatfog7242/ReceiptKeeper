package com.receiptkeeper.core.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.receiptkeeper.domain.model.Receipt
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * Utility class for exporting receipts to CSV format
 */
object CsvExporter {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Export receipts to CSV file
     * Returns the File object for sharing
     */
    fun exportToCSV(
        context: Context,
        receipts: List<Receipt>,
        vendorNames: Map<Long, String>,
        categoryNames: Map<Long, String>,
        bookNames: Map<Long, String>,
        paymentMethodNames: Map<Long, String>
    ): File {
        val fileName = "receipts_export_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)

        file.bufferedWriter().use { writer ->
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
                val notes = receipt.notes?.replace("\"", "\"\"") ?: "" // Escape quotes

                writer.write("\"$date\",\"$vendor\",\"$category\",\"$book\",\"$paymentMethod\",\"$amount\",\"$notes\"\n")
            }
        }

        return file
    }

    /**
     * Share CSV file using system share sheet
     */
    fun shareCSV(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "ReceiptKeeper Export")
            putExtra(Intent.EXTRA_TEXT, "Exported receipts from ReceiptKeeper")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Export Receipts"))
    }
}
