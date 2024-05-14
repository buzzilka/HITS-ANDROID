package com.example.hits_android.rotate

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Canvas
import kotlin.math.*

class Rotate {
    fun scaleImage(image: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = image.width
        val height = image.height

        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        return Bitmap.createBitmap(image, 0, 0, width, height, matrix, true)
    }

    fun rotate(image: Bitmap, angle: Float, flag: Boolean): Bitmap {
        val matrix = Matrix()
        val diagonal = sqrt((image.width * image.width + image.height * image.height).toFloat())
        matrix.postRotate(angle)
        var rotatedImage = Bitmap.createBitmap(image.width, image.height, image.config)
        if (flag) {
            rotatedImage = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
        }
        else {
            val canvas = Canvas(rotatedImage)

            canvas.translate(image.width / 2f, image.height / 2f)
            canvas.rotate(angle)

            canvas.translate(-(image.width) / 2f, -(image.height) / 2f)

            if (cos(angle) != 0f) {
                val newWidth = image.width / cos(angle)
                val newHeight = image.height / cos(angle)

                val newDiagonal = sqrt(newWidth * newWidth + newHeight * newHeight)

                canvas.drawBitmap(
                    scaleImage(
                        image,
                        (image.width * newDiagonal / diagonal).toInt(),
                        (image.height * newDiagonal / diagonal).toInt()
                    ), 0f, 0f, null
                )
            }
        }
        return rotatedImage
    }
}