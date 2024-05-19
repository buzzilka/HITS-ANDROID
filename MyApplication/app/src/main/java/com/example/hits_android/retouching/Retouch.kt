package com.example.hits_android.retouching

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.max
import kotlin.math.min

class Retouch {
    fun applyRetouching(x: Float, y: Float, retouchedBitmap: Bitmap, intencity: Int, brushSize: Int) : Bitmap {
        return retouch(retouchedBitmap, brushSize, intencity / 100.0, x, y)
    }

    private fun retouch(originalBitmap: Bitmap, brushSize: Int, retouchingFactor: Double, x: Float, y: Float): Bitmap {
        val paint = Paint().apply {
            color = Color.TRANSPARENT
            strokeWidth = 40f
            isAntiAlias = true
            isDither = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            style = Paint.Style.FILL
        }
        val retouchedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(retouchedBitmap)
        val radius = brushSize / 2f

        val startX = (x - radius).coerceAtLeast(0f).toInt()
        val startY = (y - radius).coerceAtLeast(0f).toInt()
        val endX = (x + radius).coerceAtMost((retouchedBitmap.width - 1).toFloat()).toInt()
        val endY = (y + radius).coerceAtMost((retouchedBitmap.height - 1).toFloat()).toInt()

        canvas.drawCircle(x, y, radius, paint)

        var redSum = 0.0
        var greenSum = 0.0
        var blueSum = 0.0
        var alphaSum = 0.0
        var count = 0

        for (i in startX..endX) {
            for (j in startY..endY) {
                val distance = Math.sqrt(((i - x) * (i - x) + (j - y) * (j - y)).toDouble()).toFloat()
                if (distance <= radius) {
                    val pix = originalBitmap.getPixel(i, j)
                    redSum += Color.red(pix)
                    greenSum += Color.green(pix)
                    blueSum += Color.blue(pix)
                    alphaSum += Color.alpha(pix)
                    count++
                }
            }
        }
        for (i in startX..endX) {
            for (j in startY..endY) {
                val distance = Math.sqrt(((i - x) * (i - x) + (j - y) * (j - y)).toDouble()).toFloat()
                if (distance <= radius) {
                    val coeff = (1 - distance / radius) * retouchingFactor

                    val pix = originalBitmap.getPixel(i, j)

                    retouchedBitmap.setPixel(i, j, Color.argb(
                        averColor(Color.alpha(pix), alphaSum / count, coeff),
                        averColor(Color.red(pix), redSum / count, coeff),
                        averColor(Color.green(pix), greenSum / count, coeff),
                        averColor(Color.blue(pix), blueSum / count, coeff)
                    )
                    )
                }
            }
        }
        return retouchedBitmap
    }
    private fun averColor(color: Int, colorSr: Double, coeff: Double): Int {
        var ansColor = color

        if (colorSr > ansColor) ansColor += ((colorSr - ansColor) * coeff).toInt()
        else if (colorSr < ansColor) ansColor -= ((ansColor - colorSr) * coeff).toInt()

        return ansColor
    }

}