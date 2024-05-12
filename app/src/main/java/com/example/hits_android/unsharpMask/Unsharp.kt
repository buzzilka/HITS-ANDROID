package com.example.hits_android.unsharpMask

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import com.example.hits_android.filters.Gauss

class Unsharp {
    suspend fun sharpenImage(image: Bitmap, strength: Double, threshold: Int, radius: Int): Bitmap {
        val gauss = Gauss()
        val blurredBitmap = withContext(Dispatchers.Default) {
            gauss.gaussianBlur(image, radius)
        }
        val resultBitmap = image.copy(image.config, true)

        val kernel = arrayOf(
            doubleArrayOf(-1.0, -1.0, -1.0),
            doubleArrayOf(-1.0, 9.0, -1.0),
            doubleArrayOf(-1.0, -1.0, -1.0)
        )

        for (x in 1 until image.width - 1) {
            for (y in 1 until image.height - 1) {
                val originalPixel = image.getPixel(x, y)

                var sumR = 0.0
                var sumG = 0.0
                var sumB = 0.0

                for (i in -1..1) {
                    for (j in -1..1) {
                        val blurredPixel = blurredBitmap.getPixel(x + i, y + j)

                        val weight = kernel[i + 1][j + 1]
                        sumR += weight * Color.red(blurredPixel)
                        sumG += weight * Color.green(blurredPixel)
                        sumB += weight * Color.blue(blurredPixel)
                    }
                }

                val newPixelR = (Color.red(originalPixel) + strength * (sumR - Color.red(originalPixel))).toInt()
                val newPixelG = (Color.green(originalPixel) + strength * (sumG - Color.green(originalPixel))).toInt()
                val newPixelB = (Color.blue(originalPixel) + strength * (sumB - Color.blue(originalPixel))).toInt()

                val diffR = abs(newPixelR - Color.red(originalPixel))
                val diffG = abs(newPixelG - Color.green(originalPixel))
                val diffB = abs(newPixelB - Color.blue(originalPixel))

                val diff = max(diffR, max(diffG, diffB))

                if (diff > threshold) {
                    resultBitmap.setPixel(x, y, Color.rgb(
                        newPixelR.coerceIn(0, 255),
                        newPixelG.coerceIn(0, 255),
                        newPixelB.coerceIn(0, 255)
                    ))
                } else {
                    resultBitmap.setPixel(x, y, originalPixel)
                }
            }
        }

        return resultBitmap
    }
}
