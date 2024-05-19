package com.example.hits_android.unsharpMask

import android.graphics.Bitmap
import android.graphics.Color
import com.example.hits_android.filters.Gauss
import kotlinx.coroutines.*
import kotlin.math.abs

class Unsharp {
    suspend fun sharpenImage(image: Bitmap, strength: Double, threshold: Int, radius: Int, progressCallback: () -> Unit): Bitmap = coroutineScope {
        val gauss = Gauss()
        val blurredBitmap = withContext(Dispatchers.Default) {
            gauss.gaussianBlur(image, radius){}
        }
        val resultBitmap = Bitmap.createBitmap(image.width, image.height, image.config)

        val kernel = doubleArrayOf(
            -1.0, -1.0, -1.0,
            -1.0,  9.0, -1.0,
            -1.0, -1.0, -1.0
        )

        val sumKernel = kernel.sum()

        val pixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)

        val blurredPixels = IntArray(image.width * image.height)
        blurredBitmap.getPixels(blurredPixels, 0, image.width, 0, 0, image.width, image.height)

        val job = launch(Dispatchers.Default) {
            for (y in 0 until image.height) {
                for (x in 0 until image.width) {
                    var sumR = 0.0
                    var sumG = 0.0
                    var sumB = 0.0

                    for (i in 0..2) {
                        for (j in 0..2) {
                            val blurredPixel = blurredPixels[
                                ((y + j - 1).coerceIn(0, image.height - 1)) * image.width +
                                        (x + i - 1).coerceIn(0, image.width - 1)
                            ]
                            val weight = kernel[j * 3 + i]
                            sumR += weight * Color.red(blurredPixel)
                            sumG += weight * Color.green(blurredPixel)
                            sumB += weight * Color.blue(blurredPixel)
                        }
                    }

                    val originalPixel = pixels[y * image.width + x]
                    val newPixelR = (Color.red(originalPixel) + strength * (sumR - Color.red(originalPixel))).toInt()
                    val newPixelG = (Color.green(originalPixel) + strength * (sumG - Color.green(originalPixel))).toInt()
                    val newPixelB = (Color.blue(originalPixel) + strength * (sumB - Color.blue(originalPixel))).toInt()

                    val diffR = abs(newPixelR - Color.red(originalPixel))
                    val diffG = abs(newPixelG - Color.green(originalPixel))
                    val diffB = abs(newPixelB - Color.blue(originalPixel))

                    val diff = diffR.coerceAtLeast(diffG).coerceAtLeast(diffB)

                    pixels[y * image.width + x] =
                        if (diff > threshold) {
                            Color.rgb(
                                newPixelR.coerceIn(0, 255),
                                newPixelG.coerceIn(0, 255),
                                newPixelB.coerceIn(0, 255)
                            )
                        } else {
                            originalPixel
                        }
                }
            }
        }

        job.join()

        resultBitmap.setPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        progressCallback()

        return@coroutineScope resultBitmap
    }
}


