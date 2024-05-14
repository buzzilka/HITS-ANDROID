package com.example.hits_android.face

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File

class Face {
    fun face (originalBitmap: Bitmap):Bitmap {
        if (OpenCVLoader.initLocal()) {
            Log.i("OpenCV", "OpenCV successfully loaded.");
        }
        else{
            Log.i("OpenCV", "Nope.");
        }

        val cascadeFilePath = ""
        val cascadeFile = File(cascadeFilePath)
        val faceCascade = CascadeClassifier("")


        return originalBitmap
    }
}