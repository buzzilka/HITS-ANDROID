package com.example.hits_android.unsharpMask

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import com.example.hits_android.filters.Gauss
import kotlinx.coroutines.*

class Unsharp {
    suspend fun sharpenImage(image: Bitmap, strength: Double, threshold: Int, radius: Int, progressCallback: () -> Unit): Bitmap = coroutineScope {
        val gauss = Gauss()
        val blurredBitmap = withContext(Dispatchers.Default) {
            gauss.gaussianBlur(image, radius){}
        }
        val resultBitmap = image.copy(image.config, true)

        val kernel = arrayOf(
            doubleArrayOf(-1.0, -1.0, -1.0),
            doubleArrayOf(-1.0, 9.0, -1.0),
            doubleArrayOf(-1.0, -1.0, -1.0)
        )

        val rows = image.height

        val jobs = mutableListOf<Job>()

        for (y in 0 until rows) {
            val job = launch(Dispatchers.Default) {
                val rowPixels = IntArray(image.width)
                for (x in 0 until image.width) {
                    var sumR = 0.0
                    var sumG = 0.0
                    var sumB = 0.0

                    for (i in 0..2) {
                        for (j in 0..2) {
                            val blurredPixel = blurredBitmap.getPixel(
                                (x + i - 1).coerceIn(0, image.width - 1),
                                (y + j - 1).coerceIn(0, image.height - 1)
                            )
                            val weight = kernel[i][j]
                            sumR += weight * Color.red(blurredPixel)
                            sumG += weight * Color.green(blurredPixel)
                            sumB += weight * Color.blue(blurredPixel)
                        }
                    }

                    val originalPixel = image.getPixel(x, y)
                    val newPixelR = (Color.red(originalPixel) + strength * (sumR - Color.red(originalPixel))).toInt()
                    val newPixelG = (Color.green(originalPixel) + strength * (sumG - Color.green(originalPixel))).toInt()
                    val newPixelB = (Color.blue(originalPixel) + strength * (sumB - Color.blue(originalPixel))).toInt()

                    val diffR = abs(newPixelR - Color.red(originalPixel))
                    val diffG = abs(newPixelG - Color.green(originalPixel))
                    val diffB = abs(newPixelB - Color.blue(originalPixel))

                    val diff = max(diffR, max(diffG, diffB))

                    if (diff > threshold) {
                        rowPixels[x] = Color.rgb(
                            newPixelR.coerceIn(0, 255),
                            newPixelG.coerceIn(0, 255),
                            newPixelB.coerceIn(0, 255)
                        )
                    } else {
                        rowPixels[x] = originalPixel
                    }
                }
                withContext(Dispatchers.Main) {
                    resultBitmap.setPixels(rowPixels, 0, image.width, 0, y, image.width, 1)
                }
            }
            jobs.add(job)
        }

        jobs.forEach { it.join() }
        progressCallback()
        return@coroutineScope resultBitmap
    }
}
