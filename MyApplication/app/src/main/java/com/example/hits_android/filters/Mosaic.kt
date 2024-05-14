package com.example.hits_android.filters

import android.graphics.Bitmap
import android.graphics.Color

class Mosaic {
    fun mosaic(image: Bitmap, px: Int, progressCallback: () -> Unit): Bitmap {
        val resultBitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        val mosaicPixels = IntArray(pixels.size)

        for (rows in 0 until (image.height + px - 1) / px) {
            for (cols in 0 until (image.width + px - 1) / px) {
                var red = 0.0
                var green = 0.0
                var blue = 0.0
                var count = 0

                for (mosaicRows in 0 until px) {
                    for (mosaicCols in 0 until px) {
                        val currentRow = rows * px + mosaicRows
                        val currentCol = cols * px + mosaicCols

                        if (currentRow < image.height && currentCol < image.width) {
                            val color = pixels[currentRow * image.width + currentCol]
                            red += Color.red(color)
                            green += Color.green(color)
                            blue += Color.blue(color)
                            count++
                        }
                    }
                }

                val avgRed = (red / count).toInt()
                val avgGreen = (green / count).toInt()
                val avgBlue = (blue / count).toInt()

                for (mosaicRows in 0 until px) {
                    for (mosaicCols in 0 until px) {
                        val currentRow = rows * px + mosaicRows
                        val currentCol = cols * px + mosaicCols

                        if (currentRow < image.height && currentCol < image.width) {
                            mosaicPixels[currentRow * image.width + currentCol] = Color.rgb(avgRed, avgGreen, avgBlue)
                        }
                    }
                }
            }
        }

        resultBitmap.setPixels(mosaicPixels, 0, image.width, 0, 0, image.width, image.height)
        progressCallback()
        return resultBitmap
    }
}