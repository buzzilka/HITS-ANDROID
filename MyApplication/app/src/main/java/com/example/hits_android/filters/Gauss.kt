package com.example.hits_android.filters

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.*
import kotlin.math.*

class Gauss {
    suspend fun gaussianBlur(image: Bitmap, radius: Int, progressCallback: () -> Unit): Bitmap = coroutineScope {

        val kernel = calculateKernel(radius)

        val width = image.width
        val height = image.height
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        image.getPixels(pixels, 0, width, 0, 0, width, height)

        val horizontalPass = IntArray(width * height)
        val verticalPass = IntArray(width * height)

        val numChunks = (Runtime.getRuntime().availableProcessors() * 2).coerceAtLeast(16)
        val chunkSize = height / numChunks

        val horizontalJobs = (0 until numChunks).map { i ->
            async(Dispatchers.Default) {
                val startY = i * chunkSize
                val endY = if (i == numChunks - 1) height else (i + 1) * chunkSize
                for (y in startY until endY) {
                    for (x in 0 until width) {
                        var redSum = 0.0
                        var greenSum = 0.0
                        var blueSum = 0.0
                        for (k in -radius..radius) {
                            val currentX = (x + k).coerceIn(0, width - 1)
                            val color = pixels[y * width + currentX]
                            val weight = kernel[k + radius]
                            redSum += (color shr 16 and 0xFF) * weight
                            greenSum += (color shr 8 and 0xFF) * weight
                            blueSum += (color and 0xFF) * weight
                        }
                        horizontalPass[y * width + x] = (0xFF shl 24) or (redSum.roundToInt() shl 16) or (greenSum.roundToInt() shl 8) or blueSum.roundToInt()
                    }
                }
            }
        }
        horizontalJobs.awaitAll()

        val verticalJobs = (0 until numChunks).map { i ->
            async(Dispatchers.Default) {
                val startX = i * chunkSize
                val endX = if (i == numChunks - 1) width else (i + 1) * chunkSize
                for (x in startX until endX) {
                    for (y in 0 until height) {
                        var redSum = 0.0
                        var greenSum = 0.0
                        var blueSum = 0.0
                        for (k in -radius..radius) {
                            val currentY = (y + k).coerceIn(0, height - 1)
                            val color = horizontalPass[currentY * width + x]
                            val weight = kernel[k + radius]
                            redSum += (color shr 16 and 0xFF) * weight
                            greenSum += (color shr 8 and 0xFF) * weight
                            blueSum += (color and 0xFF) * weight
                        }
                        verticalPass[y * width + x] = (0xFF shl 24) or (redSum.roundToInt() shl 16) or (greenSum.roundToInt() shl 8) or blueSum.roundToInt()
                    }
                }
            }
        }
        verticalJobs.awaitAll()

        resultBitmap.setPixels(verticalPass, 0, width, 0, 0, width, height)
        progressCallback()
        return@coroutineScope resultBitmap
    }

    private fun calculateKernel(radius: Int): DoubleArray {
        val sigma: Double = radius / 3.0
        val twoSigmaSquare = 2 * sigma * sigma
        val sqrtTwoPiSigma = sqrt(2 * Math.PI) * sigma

        val halfKernel = DoubleArray(radius + 1)
        for (i in 0..radius) {
            halfKernel[i] = exp(-i * i / twoSigmaSquare) / sqrtTwoPiSigma
        }

        val kernel = DoubleArray(2 * radius + 1)
        for (i in 0 until radius) {
            kernel[radius - i] = halfKernel[i]
            kernel[radius + i] = halfKernel[i]
        }
        kernel[radius] = halfKernel[radius]

        val sum = kernel.sum()
        for (i in kernel.indices) {
            kernel[i] /= sum
        }

        return kernel
    }
}

