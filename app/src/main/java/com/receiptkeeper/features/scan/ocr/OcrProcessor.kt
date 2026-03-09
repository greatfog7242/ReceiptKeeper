package com.receiptkeeper.features.scan.ocr

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.receiptkeeper.core.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Processes images using ML Kit Text Recognition
 */
@Singleton
class OcrProcessor @Inject constructor(
    private val textRecognizer: TextRecognizer,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Process an image and extract text using ML Kit
     */
    suspend fun processImage(imageUri: Uri, context: Context): OcrResult {
        return withContext(ioDispatcher) {
            try {
                // Get file path from URI
                val imagePath = imageUri.path ?: throw IllegalArgumentException("Invalid image URI")
                val file = File(imagePath)
                
                if (!file.exists()) {
                    throw IllegalArgumentException("Image file does not exist: $imagePath")
                }

                // Load bitmap from file
                val bitmap = BitmapFactory.decodeFile(imagePath)
                    ?: throw IllegalArgumentException("Could not decode image file")

                // Create InputImage from bitmap
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                val visionText = textRecognizer.process(inputImage).await()

                // Clean up bitmap
                bitmap.recycle()

                OcrResult(
                    fullText = visionText.text,
                    success = true,
                    error = null
                )
            } catch (e: Exception) {
                OcrResult(
                    fullText = "",
                    success = false,
                    error = e.message ?: "OCR processing failed"
                )
            }
        }
    }
}
