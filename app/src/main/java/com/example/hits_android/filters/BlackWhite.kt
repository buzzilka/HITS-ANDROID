package com.example.hits_android.filters

import android.graphics.Bitmap
import android.graphics.Color

class BlackWhite {
    fun blackAndWhite(bitmap: Bitmap, intensity:Double): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val color =((Color.red(pixel) * 0.3 + Color.green(pixel) * 0.59 + Color.blue(pixel) * 0.11) * intensity).toInt().coerceIn(0, 255)
                resultBitmap.setPixel(x, y, Color.rgb(color, color, color))
            }
        }
        return resultBitmap
    }
}