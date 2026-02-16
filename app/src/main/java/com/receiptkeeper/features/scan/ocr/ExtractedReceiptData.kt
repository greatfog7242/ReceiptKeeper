package com.receiptkeeper.features.scan.ocr

import java.time.LocalDate

/**
 * Data extracted from receipt OCR processing
 */
data class ExtractedReceiptData(
    val vendor: String?,
    val date: LocalDate?,
    val amount: Double?,
    val cardLast4: String?,
    val fullText: String
)

/**
 * Result of OCR processing
 */
data class OcrResult(
    val fullText: String,
    val success: Boolean,
    val error: String?
)
