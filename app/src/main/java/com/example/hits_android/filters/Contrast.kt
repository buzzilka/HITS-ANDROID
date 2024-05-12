package com.example.hits_android.filters

import android.graphics.Bitmap
import android.graphics.Color

class Contrast{
    fun contrast(image: Bitmap, contrastVal:Int) : Bitmap {
        val resultBitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        val contrastPixels = IntArray(pixels.size)

        val contrastCoefficient:Double = (259.0 * (contrastVal + 255)) / (255.0 * (259 - contrastVal))

        for (rows in 0 until image.height)
        {
            for (cols in 0 until image.width)
            {
                val color = pixels[rows * image.width + cols]
                val red = (Color.red(color) - 128) * contrastCoefficient + 128
                val green = (Color.green(color) - 128) * contrastCoefficient + 128
                val blue = (Color.blue(color) - 128) * contrastCoefficient + 128
                contrastPixels[rows * image.width + cols] = Color.rgb(red.toInt(), green.toInt(), blue.toInt())
            }
        }

        resultBitmap.setPixels(contrastPixels, 0, image.width, 0, 0, image.width, image.height)
        return resultBitmap
    }
}