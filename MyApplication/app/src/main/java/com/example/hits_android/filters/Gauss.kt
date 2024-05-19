package com.example.hits_android.filters

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.*
import kotlin.math.*

class Gauss {
    suspend fun gaussianBlur(image: Bitmap, radius: Int, progressCallback: () -> Unit): Bitmap = coroutineScope {
        val sigma: Double = radius / 3.0
        val kernel = DoubleArray(2 * radius + 1)
        var sum: Double = 0.0

        for (i in -radius..radius) {
            kernel[i + radius] = exp(-(i * i) / (2 * sigma * sigma)) / (sqrt(2 * Math.PI) * sigma)
            sum += kernel[i + radius]
        }

        for (i in kernel.indices) {
            kernel[i] /= sum
        }

        val resultBitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)

        val jobs = mutableListOf<Deferred<Unit>>()

        val chunkSize = image.height / 16

        for (i in 0 until 16) {
            val job = async(Dispatchers.Default) {
                val startY = i * chunkSize
                val endY = if (i == 3) image.height else (i + 1) * chunkSize

                for (y in startY until endY) {
                    val startX = 0
                    val endX = image.width

                    var redSum = DoubleArray(endX)
                    var greenSum = DoubleArray(endX)
                    var blueSum = DoubleArray(endX)

                    for (k in -radius..radius) {
                        for (x in startX until endX) {
                            val currentX = min(max(x + k, 0), image.width - 1)
                            val color = pixels[y * image.width + currentX]
                            val weight = kernel[k + radius]
                            redSum[x] += Color.red(color) * weight
                            greenSum[x] += Color.green(color) * weight
                            blueSum[x] += Color.blue(color) * weight
                        }
                    }

                    for (x in startX until endX) {
                        val newColor = Color.rgb(redSum[x].toInt(), greenSum[x].toInt(), blueSum[x].toInt())
                        pixels[y * image.width + x] = newColor
                    }
                }
            }
            jobs.add(job)
        }

        jobs.awaitAll()

        resultBitmap.setPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        progressCallback()
        return@coroutineScope resultBitmap
    }
}

