package com.tawandachiteshe.coinage.feature.scan

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OcrResult(
    val rawText: String,
    val emphasizedLineTexts: Set<String>, // lines with above-average text height (bold/large print)
)

class OcrProcessor(private val context: Context) {
    suspend fun processUri(uri: Uri): OcrResult = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { result ->
                recognizer.close()
                val allLines = result.textBlocks.flatMap { it.lines }
                val heights = allLines.mapNotNull { it.boundingBox?.height() }.sorted()
                val median = heights.getOrNull(heights.size / 2)?.toFloat() ?: 0f
                val emphasized = if (median > 0f) {
                    allLines
                        .filter { (it.boundingBox?.height()?.toFloat() ?: 0f) > median * 1.4f }
                        .map { it.text.trim() }
                        .toSet()
                } else emptySet()
                cont.resume(OcrResult(result.text, emphasized))
            }
            .addOnFailureListener { e ->
                recognizer.close()
                cont.resumeWithException(e)
            }
    }
}
