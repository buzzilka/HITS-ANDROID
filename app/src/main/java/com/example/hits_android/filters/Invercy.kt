package com.example.hits_android.filters

import android.graphics.Bitmap

class Invercy {
    fun Inversion(bitmap: Bitmap, threshold: Int): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val thresholdedValue = 255 - threshold

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = pixel and 0xff000000.toInt()
            val red = if ((pixel shr 16 and 0xff) < thresholdedValue) 255 else 0
            val green = if ((pixel shr 8 and 0xff) < thresholdedValue) 255 else 0
            val blue = if ((pixel and 0xff) < thresholdedValue) 255 else 0

            pixels[i] = alpha or (red shl 16) or (green shl 8) or blue
        }
        resultBitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return resultBitmap
    }
}