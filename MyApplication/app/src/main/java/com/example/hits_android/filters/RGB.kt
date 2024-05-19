package com.example.hits_android.filters

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class RGB {
    suspend fun rgbFilter(bitmap: Bitmap, intensity: Int, color: String): Bitmap = coroutineScope {
        val width = bitmap.width
        val height = bitmap.height
        val resultBitmap = Bitmap.createBitmap(width, height, bitmap.config)

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val deferredResults = mutableListOf<Deferred<Unit>>()

        val chunkSize = width / 4

        for (i in 0 until 4) {
            val deferredJob = async(Dispatchers.Default) {
                val start = i * chunkSize
                val end = if (i == 3) width else (i + 1) * chunkSize
                for (x in start until end) {
                    for (y in 0 until height) {
                        val index = y * width + x
                        var pixel = pixels[index]

                        val red = Color.red(pixel)
                        val green = Color.green(pixel)
                        val blue = Color.blue(pixel)

                        var newColor: Int
                        newColor = when (color) {
                            "red" -> red + intensity
                            "green" -> green + intensity
                            else -> blue + intensity
                        }

                        val finalColor = when {
                            newColor > 255 -> 255
                            newColor < 0 -> 0
                            else -> newColor
                        }

                        pixel = when (color) {
                            "red" -> Color.rgb(finalColor, green, blue)
                            "green" -> Color.rgb(red, finalColor, blue)
                            else -> Color.rgb(red, green, finalColor)
                        }

                        pixels[index] = pixel
                    }
                }
            }
            deferredResults.add(deferredJob)
        }

        deferredResults.awaitAll()

        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return@coroutineScope resultBitmap
    }
}