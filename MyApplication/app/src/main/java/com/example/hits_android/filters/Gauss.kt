package com.example.hits_android.filters

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class Gauss {
    suspend fun gaussianBlur(image: Bitmap, radius: Int, progressCallback: () -> Unit): Bitmap = coroutineScope {
        val sigma:Double = radius / 3.0
        val kernel = DoubleArray(2 * radius + 1)
        var sum: Double = 0.0

        // Создаем ядро
        for (i in -radius..radius) {
            kernel[i + radius] = exp(-(i * i) / (2 * sigma * sigma)) / (sqrt(2 * Math.PI) * sigma)
            sum += kernel[i + radius]
        }

        // Нормализуем ядро
        for (i in kernel.indices) {
            kernel[i] /= sum
        }

        val resultBitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        val blurredPixels = IntArray(pixels.size)

        val rows = image.height

        val deferredList = (0 until rows).map { y ->
            async(Dispatchers.Default) {
                val rowPixels = IntArray(image.width)
                for (x in 0 until image.width) {
                    var red = 0.0
                    var green = 0.0
                    var blue = 0.0
                    for (i in -radius..radius) {
                        val current_x = min(max(x + i, 0), image.width - 1)
                        val color = pixels[y * image.width + current_x]
                        val weight = kernel[i + radius]
                        red += Color.red(color) * weight
                        green += Color.green(color) * weight
                        blue += Color.blue(color) * weight
                    }
                    rowPixels[x] = Color.rgb(red.toInt(), green.toInt(), blue.toInt())
                }
                rowPixels
            }
        }

        val rowsResult = deferredList.awaitAll()

        for (y in rowsResult.indices) {
            System.arraycopy(rowsResult[y], 0, blurredPixels, y * image.width, image.width)
        }

        resultBitmap.setPixels(blurredPixels, 0, image.width, 0, 0, image.width, image.height)
        progressCallback()
        return@coroutineScope resultBitmap
    }
}