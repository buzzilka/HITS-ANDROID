package com.example.hits_android.scaling

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*
import kotlinx.coroutines.*


class Scale {
    fun scaleImage(bitmap: Bitmap, scaleFactor: Double): Bitmap {
        var originalCopy = bitmap.copy(bitmap.config, true)
        if (scaleFactor > 1.0) {
            originalCopy = scaleBilinear(originalCopy, scaleFactor)
        } else if (scaleFactor < 1.0) {
            originalCopy = scaleTrilinear(originalCopy, scaleFactor)
        }
        return originalCopy
    }

    fun scaleBilinear(bitmap: Bitmap, scaleFactor: Double): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, bitmap.config)

        for (y in 0 until newHeight) {
            for (x in 0 until newWidth) {
                val srcX = x / scaleFactor
                val srcY = y / scaleFactor

                val x1 = srcX.toInt().coerceIn(0, width - 1)
                val y1 = srcY.toInt().coerceIn(0, height - 1)
                val x2 = (x1 + 1).coerceIn(0, width - 1)
                val y2 = (y1 + 1).coerceIn(0, height - 1)

                val q11 = getPixel(bitmap, x1, y1)
                val q12 = getPixel(bitmap, x1, y2)
                val q21 = getPixel(bitmap, x2, y1)
                val q22 = getPixel(bitmap, x2, y2)

                val r1 = interpolate(q11[0], q21[0], srcX - x1)
                val g1 = interpolate(q11[1], q21[1], srcX - x1)
                val b1 = interpolate(q11[2], q21[2], srcX - x1)

                val r2 = interpolate(q12[0], q22[0], srcX - x1)
                val g2 = interpolate(q12[1], q22[1], srcX - x1)
                val b2 = interpolate(q12[2], q22[2], srcX - x1)

                val r = interpolate(r1, r2, srcY - y1)
                val g = interpolate(g1, g2, srcY - y1)
                val b = interpolate(b1, b2, srcY - y1)

                val newPixel = (255 shl 24) or (r shl 16) or (g shl 8) or b
                scaledBitmap.setPixel(x, y, newPixel)
            }
        }

        return scaledBitmap
    }

    fun scaleTrilinear(bitmap: Bitmap, scaleFactor: Double): Bitmap {
        val logScale = log2(1.0 / scaleFactor)
        val level = logScale.toInt()
        val alpha = logScale - level

        val lowRes = createMipMap(bitmap, level)
        val highRes = createMipMap(bitmap, level + 1)

        val lowResScaled = scaleBilinear(lowRes, scaleFactor * 2.0.pow(level))
        val highResScaled = scaleBilinear(highRes, scaleFactor * 2.0.pow(level + 1))

        val newWidth = (bitmap.width * scaleFactor).toInt()
        val newHeight = (bitmap.height * scaleFactor).toInt()
        val scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, bitmap.config)

        for (y in 0 until newHeight) {
            for (x in 0 until newWidth) {
                val lowPixel = getPixel(lowResScaled, x, y)
                val highPixel = getPixel(highResScaled, x, y)

                val r = interpolate(lowPixel[0], highPixel[0], alpha)
                val g = interpolate(lowPixel[1], highPixel[1], alpha)
                val b = interpolate(lowPixel[2], highPixel[2], alpha)

                val newPixel = (255 shl 24) or (r shl 16) or (g shl 8) or b
                scaledBitmap.setPixel(x, y, newPixel)
            }
        }

        return scaledBitmap
    }

    fun createMipMap(image: Bitmap, level: Int): Bitmap {
        var result = image
        repeat(level) {
            val width = result.width / 2
            val height = result.height / 2
            val mipMap = Bitmap.createBitmap(width, height, image.config)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel1 = getPixel(result, x * 2, y * 2)
                    val pixel2 = getPixel(result, x * 2 + 1, y * 2)
                    val pixel3 = getPixel(result, x * 2, y * 2 + 1)
                    val pixel4 = getPixel(result, x * 2 + 1, y * 2 + 1)

                    val r = (pixel1[0] + pixel2[0] + pixel3[0] + pixel4[0]) / 4
                    val g = (pixel1[1] + pixel2[1] + pixel3[1] + pixel4[1]) / 4
                    val b = (pixel1[2] + pixel2[2] + pixel3[2] + pixel4[2]) / 4

                    val newPixel = (255 shl 24) or (r shl 16) or (g shl 8) or b
                    mipMap.setPixel(x, y, newPixel)
                }
            }
            result = mipMap
        }
        return result
    }

    fun getPixel(image: Bitmap, x: Int, y: Int): IntArray {
        val rgb = image.getPixel(x.coerceIn(0, image.width - 1), y.coerceIn(0, image.height - 1))
        return intArrayOf((rgb shr 16) and 0xFF, (rgb shr 8) and 0xFF, rgb and 0xFF)
    }

    fun interpolate(start: Int, end: Int, factor: Double): Int {
        return (start + factor * (end - start)).toInt()
    }
}