package com.example.hits_android.rotate

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.*
import kotlin.math.*

class Rotate {

    suspend fun rotate(image: Bitmap, angle: Float): Bitmap = coroutineScope {
        val radians = Math.toRadians(angle.toDouble())
        val width = image.width
        val height = image.height
        val newWidth = (abs(width * cos(radians)) + abs(height * sin(radians))).toInt()
        val newHeight = (abs(width * sin(radians)) + abs(height * cos(radians))).toInt()

        val oldCenterX = width / 2.0
        val oldCenterY = height / 2.0
        val newCenterX = newWidth / 2.0
        val newCenterY = newHeight / 2.0

        val cosAngle = cos(radians)
        val sinAngle = sin(radians)

        val originalPixels = IntArray(width * height)
        image.getPixels(originalPixels, 0, width, 0, 0, width, height)

        val rotatedPixels = IntArray(newWidth * newHeight) { Color.parseColor("#E8E8F9") }

        val chunkSize = newHeight / 2
        val deferredResults = mutableListOf<Deferred<Unit>>()

        val deferredJob1 = async(Dispatchers.Default) {
            for (y in 0 until chunkSize) {
                val deltaY = y - newCenterY
                for (x in 0 until newWidth) {
                    val deltaX = x - newCenterX

                    val oldX = deltaX * cosAngle + deltaY * sinAngle + oldCenterX
                    val oldY = -deltaX * sinAngle + deltaY * cosAngle + oldCenterY

                    if (oldX in 0.0..(width - 1.0) && oldY in 0.0..(height - 1.0)) {
                        val x0 = oldX.toInt()
                        val y0 = oldY.toInt()
                        val x1 = (x0 + 1).coerceAtMost(width - 1)
                        val y1 = (y0 + 1).coerceAtMost(height - 1)

                        val p00 = originalPixels[y0 * width + x0]
                        val p10 = originalPixels[y0 * width + x1]
                        val p01 = originalPixels[y1 * width + x0]
                        val p11 = originalPixels[y1 * width + x1]

                        val coefX = oldX - x0
                        val coefY = oldY - y0

                        val r = (Color.red(p00) * (1 - coefX) * (1 - coefY) +
                                Color.red(p10) * coefX * (1 - coefY) +
                                Color.red(p01) * (1 - coefX) * coefY +
                                Color.red(p11) * coefX * coefY).toInt()

                        val g = (Color.green(p00) * (1 - coefX) * (1 - coefY) +
                                Color.green(p10) * coefX * (1 - coefY) +
                                Color.green(p01) * (1 - coefX) * coefY +
                                Color.green(p11) * coefX * coefY).toInt()

                        val b = (Color.blue(p00) * (1 - coefX) * (1 - coefY) +
                                Color.blue(p10) * coefX * (1 - coefY) +
                                Color.blue(p01) * (1 - coefX) * coefY +
                                Color.blue(p11) * coefX * coefY).toInt()

                        rotatedPixels[y * newWidth + x] = Color.rgb(r, g, b)
                    }
                }
            }
        }
        deferredResults.add(deferredJob1)

        val deferredJob2 = async(Dispatchers.Default) {
            for (y in chunkSize until newHeight) {
                val deltaY = y - newCenterY
                for (x in 0 until newWidth) {
                    val deltaX = x - newCenterX

                    val oldX = deltaX * cosAngle + deltaY * sinAngle + oldCenterX
                    val oldY = -deltaX * sinAngle + deltaY * cosAngle + oldCenterY

                    if (oldX in 0.0..(width - 1.0) && oldY in 0.0..(height - 1.0)) {
                        val x0 = oldX.toInt()
                        val y0 = oldY.toInt()
                        val x1 = (x0 + 1).coerceAtMost(width - 1)
                        val y1 = (y0 + 1).coerceAtMost(height - 1)


                        val p00 = originalPixels[y0 * width + x0]
                        val p10 = originalPixels[y0 * width + x1]
                        val p01 = originalPixels[y1 * width + x0]
                        val p11 = originalPixels[y1 * width + x1]

                        val coefX = oldX - x0
                        val coefY = oldY - y0

                        val r = (Color.red(p00) * (1 - coefX) * (1 - coefY) +
                                Color.red(p10) * coefX * (1 - coefY) +
                                Color.red(p01) * (1 - coefX) * coefY +
                                Color.red(p11) * coefX * coefY).toInt()

                        val g = (Color.green(p00) * (1 - coefX) * (1 - coefY) +
                                Color.green(p10) * coefX * (1 - coefY) +
                                Color.green(p01) * (1 - coefX) * coefY +
                                Color.green(p11) * coefX * coefY).toInt()

                        val b = (Color.blue(p00) * (1 - coefX) * (1 - coefY) +
                                Color.blue(p10) * coefX * (1 - coefY) +
                                Color.blue(p01) * (1 - coefX) * coefY +
                                Color.blue(p11) * coefX * coefY).toInt()

                        rotatedPixels[y * newWidth + x] = Color.rgb(r, g, b)
                    }
                }
            }
        }
        deferredResults.add(deferredJob2)

        deferredResults.awaitAll()

        val rotatedImage = Bitmap.createBitmap(newWidth, newHeight, image.config)
        rotatedImage.setPixels(rotatedPixels, 0, newWidth, 0, 0, newWidth, newHeight)

        return@coroutineScope rotatedImage
    }
}