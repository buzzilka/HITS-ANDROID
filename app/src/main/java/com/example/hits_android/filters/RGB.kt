package com.example.hits_android.filters

import android.graphics.Bitmap
import android.graphics.Color

class RGB {
    fun rgbFilter(bitmap: Bitmap, intensity: Int, color:String): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                var pixel = bitmap.getPixel(x, y)

                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                var newColor:Int
                if (color == "red") newColor = red + intensity
                else if (color == "green") newColor = green + intensity
                else newColor = green + intensity

                val finalColor = when {
                    newColor > 255 -> 255
                    newColor < 0 -> 0
                    else -> newColor
                }

                if (color == "red") pixel = Color.rgb(finalColor, green, blue)
                else if (color == "green") pixel = Color.rgb(red, finalColor, blue)
                else pixel = Color.rgb(red, green, finalColor)

                resultBitmap.setPixel(x, y, pixel)
            }
        }

        return resultBitmap
    }
}