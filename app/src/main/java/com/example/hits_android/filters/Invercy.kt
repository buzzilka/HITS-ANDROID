package com.example.hits_android.filters

import android.graphics.Bitmap
import android.graphics.Color

class Invercy {
    fun Inversion(bitmap: Bitmap, threshold: Int, progressCallback: () -> Unit): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val thresholdedValue = 255 - threshold

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            val red = if (Color.red(pixel) < thresholdedValue) 255 else 0
            val green = if (Color.green(pixel) < thresholdedValue) 255 else 0
            val blue = if (Color.blue(pixel) < thresholdedValue) 255 else 0

            pixels[i] = Color.argb(alpha, red, green, blue)
        }
        resultBitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        progressCallback()
        return resultBitmap
    }
}