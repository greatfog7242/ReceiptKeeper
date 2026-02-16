package com.receiptkeeper.features.scan.ocr

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parses raw OCR text to extract receipt information using regex patterns
 */
object ReceiptParser {

    fun parseReceipt(rawText: String): ExtractedReceiptData {
        val lines = rawText.lines().filter { it.isNotBlank() }

        return ExtractedReceiptData(
            vendor = extractVendor(lines),
            date = extractDate(rawText),
            amount = extractAmount(rawText),
            cardLast4 = extractCardNumber(rawText),
            fullText = rawText
        )
    }

    /**
     * Extract vendor name - assumes first non-date line is vendor
     */
    private fun extractVendor(lines: List<String>): String? {
        val datePattern = Regex("\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}")
        return lines.firstOrNull { !datePattern.containsMatchIn(it) }?.take(50)
    }

    /**
     * Extract date - tries multiple common formats
     */
    private fun extractDate(text: String): LocalDate? {
        // Try MM/DD/YYYY format
        val usDatePattern = Regex("""(\d{1,2})/(\d{1,2})/(\d{4})""")
        usDatePattern.find(text)?.let { match ->
            try {
                val (month, day, year) = match.destructured
                return LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            } catch (e: Exception) {
                // Invalid date, continue
            }
        }

        // Try DD-MM-YYYY format
        val euDatePattern = Regex("""(\d{1,2})-(\d{1,2})-(\d{4})""")
        euDatePattern.find(text)?.let { match ->
            try {
                val (day, month, year) = match.destructured
                return LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            } catch (e: Exception) {
                // Invalid date, continue
            }
        }

        // Try YYYY-MM-DD format (ISO)
        val isoDatePattern = Regex("""(\d{4})-(\d{2})-(\d{2})""")
        isoDatePattern.find(text)?.let { match ->
            try {
                val (year, month, day) = match.destructured
                return LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            } catch (e: Exception) {
                // Invalid date, continue
            }
        }

        return null
    }

    /**
     * Extract total amount - looks for "total" keyword followed by dollar amount
     */
    private fun extractAmount(text: String): Double? {
        val patterns = listOf(
            Regex("""total.*?\$?\s*(\d+\.\d{2})""", RegexOption.IGNORE_CASE),
            Regex("""amount.*?\$?\s*(\d+\.\d{2})""", RegexOption.IGNORE_CASE),
            Regex("""\$\s*(\d+\.\d{2})""") // Just find any dollar amount
        )

        for (pattern in patterns) {
            pattern.find(text)?.groupValues?.get(1)?.toDoubleOrNull()?.let { return it }
        }

        return null
    }

    /**
     * Extract last 4 digits of card number
     */
    private fun extractCardNumber(text: String): String? {
        val patterns = listOf(
            Regex("""(?:card|xxxx|\*{4})\s*(\d{4})""", RegexOption.IGNORE_CASE),
            Regex("""ending\s+in\s+(\d{4})""", RegexOption.IGNORE_CASE),
            Regex("""x{4}(\d{4})""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            pattern.find(text)?.groupValues?.get(1)?.let { return it }
        }

        return null
    }
}
