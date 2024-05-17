package com.example.hits_android.filters

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class BlackWhite {
    suspend fun blackAndWhite(bitmap: Bitmap, intensity: Double): Bitmap = coroutineScope {
        val resultBitmap = bitmap.copy(bitmap.config, true)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val chunkSize = pixels.size / 2
        val deferredResults = mutableListOf<Deferred<Unit>>()

        val deferredJob1 = async(Dispatchers.Default) {
            for (i in 0 until chunkSize) {
                val pixel = pixels[i]
                val gray = (Color.red(pixel) * 0.3 + Color.green(pixel) * 0.59 + Color.blue(pixel) * 0.11) * intensity
                pixels[i] = Color.rgb(gray.toInt().coerceIn(0, 255), gray.toInt().coerceIn(0, 255), gray.toInt().coerceIn(0, 255))
            }
        }
        deferredResults.add(deferredJob1)

        val deferredJob2 = async(Dispatchers.Default) {
            for (i in chunkSize until pixels.size) {
                val pixel = pixels[i]
                val gray = (Color.red(pixel) * 0.3 + Color.green(pixel) * 0.59 + Color.blue(pixel) * 0.11) * intensity
                pixels[i] = Color.rgb(gray.toInt().coerceIn(0, 255), gray.toInt().coerceIn(0, 255), gray.toInt().coerceIn(0, 255))
            }
        }
        deferredResults.add(deferredJob2)

        deferredResults.awaitAll()

        resultBitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        return@coroutineScope resultBitmap
    }
}