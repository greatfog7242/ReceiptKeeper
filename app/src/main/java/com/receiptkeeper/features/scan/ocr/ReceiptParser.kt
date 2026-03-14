package com.receiptkeeper.features.scan.ocr

import java.time.LocalDate

/**
 * Parses raw OCR text to extract receipt information using regex patterns
 */
object ReceiptParser {

    fun parseReceipt(
        rawText: String,
        knownVendors: List<String> = emptyList(),
        knownCardLast4s: List<String> = emptyList()
    ): ExtractedReceiptData {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        return ExtractedReceiptData(
            vendor = extractVendor(lines, knownVendors),
            date = extractDate(rawText),
            amount = extractAmount(rawText),
            cardLast4 = extractCardNumber(rawText, knownCardLast4s),
            fullText = rawText
        )
    }

    /**
     * Extract vendor name using known vendors and heuristic fallback
     */
    private fun extractVendor(lines: List<String>, knownVendors: List<String>): String? {
        val datePattern = Regex("\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}")
        val keywordPattern = Regex(
            "\\b(total|subtotal|tax|change|cash|credit|debit|card|visa|mastercard|amex|receipt|invoice|balance|amount)\\b",
            RegexOption.IGNORE_CASE
        )

        val filteredLines = lines.filter { line ->
            line.length >= 2 &&
                !datePattern.containsMatchIn(line) &&
                !keywordPattern.containsMatchIn(line) &&
                !isMostlyNumeric(line)
        }

        val candidates = if (filteredLines.isNotEmpty()) filteredLines.take(8) else lines.take(8)

        if (knownVendors.isNotEmpty() && candidates.isNotEmpty()) {
            val normalizedVendors = knownVendors.map { vendor ->
                NormalizedVendor(vendor, normalize(vendor))
            }.filter { it.normalized.isNotBlank() }

            var bestVendor: String? = null
            var bestScore = 0.0

            candidates.forEachIndexed { index, line ->
                val normalizedLine = normalize(line)
                if (normalizedLine.isBlank()) return@forEachIndexed

                normalizedVendors.forEach { vendor ->
                    val score = vendorSimilarity(normalizedLine, vendor.normalized, index, candidates.size)
                    if (score > bestScore) {
                        bestScore = score
                        bestVendor = vendor.original
                    }
                }
            }

            if (bestVendor != null && bestScore >= 0.6) {
                return bestVendor
            }
        }

        return lines.firstOrNull { !datePattern.containsMatchIn(it) }?.take(50)
    }

    /**
     * Extract date - tries multiple common formats
     */
    private fun extractDate(text: String): LocalDate? {
        // Try MM/DD/YYYY format
        val usDatePattern = Regex("""(\d{1,2})[\\/\\-\\\\](\d{1,2})[\\/\\-\\\\](\d{4})""")
        usDatePattern.find(text)?.let { match ->
            try {
                val (month, day, yearStr) = match.destructured
                val year = yearStr.toInt()
                // Validate year is reasonable for receipts (2000-2100)
                if (year in 2000..2100) {
                    return LocalDate.of(year, month.toInt(), day.toInt())
                }
            } catch (e: Exception) {
                // Invalid date, continue
            }
        }

        // Try DD-MM-YYYY format
        val euDatePattern = Regex("""(\d{1,2})[\\/\\-\\\\](\d{1,2})[\\/\\-\\\\](\d{4})""")
        euDatePattern.find(text)?.let { match ->
            try {
                val (day, month, yearStr) = match.destructured
                val year = yearStr.toInt()
                // Validate year is reasonable for receipts (2000-2100)
                if (year in 2000..2100) {
                    return LocalDate.of(year, month.toInt(), day.toInt())
                }
            } catch (e: Exception) {
                // Invalid date, continue
            }
        }

        // Try YYYY-MM-DD format (ISO)
        val isoDatePattern = Regex("""(\d{4})[\\/\\-\\\\](\d{2})[\\/\\-\\\\](\d{2})""")
        isoDatePattern.find(text)?.let { match ->
            try {
                val (yearStr, month, day) = match.destructured
                val year = yearStr.toInt()
                // Validate year is reasonable for receipts (2000-2100)
                if (year in 2000..2100) {
                    return LocalDate.of(year, month.toInt(), day.toInt())
                }
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
            Regex("""total.*?\$?\\s*(\\d+\\.\\d{2})""", RegexOption.IGNORE_CASE),
            Regex("""amount.*?\$?\\s*(\\d+\\.\\d{2})""", RegexOption.IGNORE_CASE),
            Regex("""\$\\s*(\\d+\\.\\d{2})""") // Just find any dollar amount
        )

        for (pattern in patterns) {
            pattern.find(text)?.groupValues?.get(1)?.toDoubleOrNull()?.let { return it }
        }

        return null
    }

    /**
     * Extract last 4 digits of card number, enhanced with known card last-4 list
     */
    private fun extractCardNumber(text: String, knownCardLast4s: List<String>): String? {
        val normalizedKnown = knownCardLast4s
            .map { it.filter(Char::isDigit) }
            .filter { it.length == 4 }
            .toSet()

        val normalizedText = normalizeCardText(text)

        if (normalizedKnown.isNotEmpty()) {
            findKnownLast4WithBoundary(normalizedText, normalizedKnown)?.let { return it }

            val maskedPattern = Regex(
                """(?:card|ending|x{4,})\\s*(\\d{4})""",
                RegexOption.IGNORE_CASE
            )
            for (match in maskedPattern.findAll(normalizedText)) {
                val last4 = match.groupValues.getOrNull(1)
                if (last4 != null && normalizedKnown.contains(last4)) {
                    return last4
                }
            }

            val compactMaskedPattern = Regex("""x{4,}(\\d{4})""", RegexOption.IGNORE_CASE)
            for (match in compactMaskedPattern.findAll(normalizedText)) {
                val last4 = match.groupValues.getOrNull(1)
                if (last4 != null && normalizedKnown.contains(last4)) {
                    return last4
                }
            }

            val digitChunks = Regex("""\\d{4,}""")
            for (match in digitChunks.findAll(normalizedText)) {
                val digits = match.value
                val last4 = digits.takeLast(4)
                if (normalizedKnown.contains(last4)) {
                    return last4
                }
            }

            val spacedDigits = Regex("""(?:\\d[\\s-]?){4,}""")
            for (match in spacedDigits.findAll(normalizedText)) {
                val digits = match.value.filter(Char::isDigit)
                if (digits.length >= 4) {
                    val last4 = digits.takeLast(4)
                    if (normalizedKnown.contains(last4)) {
                        return last4
                    }
                }
            }
        }

        val patterns = listOf(
            Regex("""(?:card|x{4,})\\s*(\\d{4})""", RegexOption.IGNORE_CASE),
            Regex("""ending\\s+in\\s+(\\d{4})""", RegexOption.IGNORE_CASE),
            Regex("""x{4,}(\\d{4})""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            pattern.find(normalizedText)?.groupValues?.get(1)?.let { found ->
                if (normalizedKnown.isEmpty() || normalizedKnown.contains(found)) {
                    return found
                }
            }
        }

        if (normalizedKnown.isNotEmpty()) {
            val matches = normalizedKnown
                .mapNotNull { last4 ->
                    val match = Regex("\\b$last4\\b").find(normalizedText)
                    match?.let { last4 to it.range.first }
                }
                .sortedBy { it.second }

            if (matches.isNotEmpty()) {
                return matches.first().first
            }
        }

        return null
    }

    private fun findKnownLast4WithBoundary(text: String, knownLast4s: Set<String>): String? {
        for (last4 in knownLast4s) {
            var index = text.indexOf(last4)
            while (index >= 0) {
                val before = if (index > 0) text[index - 1] else null
                val afterIndex = index + last4.length
                val after = if (afterIndex < text.length) text[afterIndex] else null
                val beforeIsDigit = before != null && before.isDigit()
                val afterIsDigit = after != null && after.isDigit()

                if (!beforeIsDigit && !afterIsDigit) {
                    return last4
                }

                index = text.indexOf(last4, index + 1)
            }
        }

        return null
    }

    private fun normalizeCardText(text: String): String {
        val builder = StringBuilder(text.length)
        for (ch in text) {
            val normalized = when (ch) {
                in '０'..'９' -> (ch.code - '０'.code + '0'.code).toChar()
                'x', 'X', '*', '#', '•', '·', '×', '●', '■', '□', '▪', '▫' -> 'x'
                '\u200B', '\u200C', '\u200D', '\uFEFF' -> ' '
                else -> ch
            }
            builder.append(normalized)
        }
        return builder.toString()
    }

    private fun normalize(text: String): String {
        return text
            .lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun isMostlyNumeric(text: String): Boolean {
        val digits = text.count { it.isDigit() }
        val letters = text.count { it.isLetter() }
        return digits >= 4 && digits > letters
    }

    private fun vendorSimilarity(line: String, vendor: String, index: Int, total: Int): Double {
        if (line == vendor) return 1.0

        val containsBonus = if (line.contains(vendor) || vendor.contains(line)) 0.4 else 0.0
        val tokensLine = line.split(" ").filter { it.isNotBlank() }.toSet()
        val tokensVendor = vendor.split(" ").filter { it.isNotBlank() }.toSet()
        val jaccard = if (tokensLine.isNotEmpty() && tokensVendor.isNotEmpty()) {
            val intersection = tokensLine.intersect(tokensVendor).size.toDouble()
            val union = tokensLine.union(tokensVendor).size.toDouble()
            intersection / union
        } else {
            0.0
        }
        val prefixBonus = if (line.startsWith(vendor) || vendor.startsWith(line)) 0.2 else 0.0
        val positionBonus = if (total > 1) ((total - 1 - index).toDouble() / (total - 1)) * 0.1 else 0.0

        return (jaccard + containsBonus + prefixBonus + positionBonus).coerceAtMost(1.0)
    }

    private data class NormalizedVendor(val original: String, val normalized: String)
}




