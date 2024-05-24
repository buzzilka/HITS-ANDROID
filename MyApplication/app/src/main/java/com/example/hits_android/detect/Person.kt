package com.example.hits_android.detect

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY
import org.opencv.imgproc.Imgproc.cvtColor
import org.opencv.objdetect.CascadeClassifier
import java.io.File

class Person {
    private lateinit var fullbodyCascade: CascadeClassifier
    private val fullbodyModel = "haarcascade_fullbody.xml"

    fun apply(originalBitmap: Bitmap, activity: Activity): Bitmap {
        loadModels(activity)
        val originalMap = Mat()
        Utils.bitmapToMat(originalBitmap, originalMap)

        val resultBitmap: Bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(resultBitmap)

        detectAndDraw(fullbodyCascade, originalMap, canvas)

        return resultBitmap
    }

    private fun detectAndDraw (cascadeClassifier: CascadeClassifier, image: Mat, canvas: Canvas) {
        val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            color = Color.GRAY
        }

        val rectangles = MatOfRect()

        val grayImage:Mat = image.clone()
        cvtColor(grayImage, grayImage, COLOR_BGR2GRAY)
        val clahe = Imgproc.createCLAHE()
        clahe.clipLimit = 2.0
        clahe.apply(grayImage, grayImage)

        cascadeClassifier.detectMultiScale(
            grayImage,
            rectangles,
            1.08,
            3,
            0,
            Size(50.0, 50.0),
            Size()
        )

        rectangles.toArray().forEach { rect ->
            canvas.drawRect(
                Rect(
                    rect.x,
                    rect.y,
                    (rect.x + rect.width),
                    (rect.y + rect.height)
                ), paint
            )
        }
    }

    private fun loadModels(activity: Activity) {
        OpenCVLoader.initDebug()
        if (OpenCVLoader.initLocal()) {
            Log.i("OpenCV", "OpenCV successfully loaded.")
        } else {
            Log.i("OpenCV", "Nope.")
        }

        fullbodyCascade = CascadeClassifier(File(activity.filesDir, "fullbodyface_cascade.xml").apply {
            writeBytes(activity.assets.open(fullbodyModel).readBytes())
        }.path)
    }
}