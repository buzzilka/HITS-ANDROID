package com.example.hits_android.filters

import android.graphics.Bitmap
import android.graphics.Color

class Sepia {
    fun sepiaFilter(bitmap: Bitmap, sepiaDepth: Int): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)

            var tr = (0.393 * red + 0.769 * green + 0.189 * blue).toInt()
            var tg = (0.349 * red + 0.686 * green + 0.168 * blue).toInt()
            var tb = (0.272 * red + 0.534 * green + 0.131 * blue).toInt()

            tr = if (tr > 255) 255 else tr
            tg = if (tg > 255) 255 else tg
            tb = if (tb > 255) 255 else tb

            tr += sepiaDepth * 2
            tg += sepiaDepth
            tb -= sepiaDepth

            tr = if (tr > 255) 255 else tr
            tg = if (tg > 255) 255 else tg
            tb = if (tb > 255) 255 else tb

            pixels[i] = Color.rgb(tr, tg, tb)
        }

        resultBitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return resultBitmap
    }
}