package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object ImageProcessingUtil {

    /**
     * Completely offline, on-device background removal for signatures and stamps.
     * Takes a photo/scan on paper (usually white, off-white, light gray) and converts
     * the bright paper background into transparency (ARGB_8888) while preserving
     * dark ink lines (blue, black, purple) and colored stamp ink (red, violet, blue).
     *
     * @param source Source bitmap (e.g. from camera or photo picker)
     * @param threshold Sensitivity threshold between 0.50f (strict) and 0.95f (aggressive)
     * @param enhanceContrast If true, sharpens ink lines so they look crisp on invoices
     */
    fun removeBackground(
        source: Bitmap,
        threshold: Float = 0.80f,
        enhanceContrast: Boolean = true
    ): Bitmap {
        // Downscale if image is too large to maintain fast UI responsiveness
        val maxDim = 900
        val originalWidth = source.width
        val originalHeight = source.height
        val scale = if (originalWidth > maxDim || originalHeight > maxDim) {
            maxDim.toFloat() / max(originalWidth, originalHeight)
        } else {
            1f
        }

        val targetWidth = (originalWidth * scale).toInt().coerceAtLeast(1)
        val targetHeight = (originalHeight * scale).toInt().coerceAtLeast(1)

        val workingBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
        } else {
            source.copy(Bitmap.Config.ARGB_8888, true)
        }

        val resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(targetWidth * targetHeight)
        workingBitmap.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)

        // Calculate luminance cutoff (0..255)
        val cutoff = (255 * threshold).toInt()
        val featherZone = 35 // Smooth transition zone to prevent harsh edges

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            // Standard perceptual luminance formula
            val lum = (0.299f * r + 0.587f * g + 0.114f * b).toInt()

            if (lum >= cutoff) {
                // Background paper -> 100% Transparent
                pixels[i] = Color.TRANSPARENT
            } else if (lum <= cutoff - featherZone) {
                // Definite ink -> Opaque
                if (enhanceContrast) {
                    // Deepen colors slightly so scanned pencil/ballpoint ink appears solid
                    val contrastR = (r * 0.9f).toInt().coerceIn(0, 255)
                    val contrastG = (g * 0.9f).toInt().coerceIn(0, 255)
                    val contrastB = (b * 0.9f).toInt().coerceIn(0, 255)
                    pixels[i] = Color.argb(255, contrastR, contrastG, contrastB)
                } else {
                    pixels[i] = Color.argb(255, r, g, b)
                }
            } else {
                // Edge transition zone -> Smooth alpha feathering
                val alpha = (255 * (cutoff - lum) / featherZone).coerceIn(0, 255)
                pixels[i] = Color.argb(alpha, r, g, b)
            }
        }

        resultBitmap.setPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)
        return resultBitmap
    }

    fun loadBitmapFromUri(context: Context, uri: Uri, maxDim: Int = 1200): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(stream, null, options)

                var sampleSize = 1
                val maxSource = max(options.outWidth, options.outHeight)
                while (maxSource / sampleSize > maxDim) {
                    sampleSize *= 2
                }

                context.contentResolver.openInputStream(uri)?.use { secondStream ->
                    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    BitmapFactory.decodeStream(secondStream, null, decodeOptions)
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun saveBitmapAsPng(context: Context, bitmap: Bitmap, fileName: String): String {
        val dir = File(context.filesDir, "branding").apply { mkdirs() }
        val file = File(dir, "$fileName.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }

    fun loadBitmapFromFile(path: String): Bitmap? {
        if (path.isBlank()) return null
        val file = File(path)
        if (!file.exists()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (_: Exception) {
            null
        }
    }
}
