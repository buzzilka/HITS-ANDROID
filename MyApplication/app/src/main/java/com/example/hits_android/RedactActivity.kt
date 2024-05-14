package com.example.hits_android

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.viewmodel.MainViewModel
import com.example.hits_android.filters.*
import com.example.hits_android.retouching.Retouch
import com.example.hits_android.face.Face
import com.example.hits_android.rotate.Rotate
import com.example.hits_android.unsharpMask.Unsharp
import androidx.lifecycle.ViewModelProvider
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.*


class RedactActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var originalBitmap: Bitmap
    private lateinit var changeBitmap: Bitmap
    private var bitmapList = ArrayList<Bitmap>()
    private var check: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_redact)

        //hideSystemUI()

        image = findViewById(R.id.image)

        val viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        val imageUri = intent.getStringExtra("imageUri")
        viewModel.selectedFileUri.observe(this) { uri ->
            if (uri != null) {
                Glide.with(this)
                    .load(uri)
                    .into(image)
            }
        }
        viewModel.selectFile(imageUri)

        val buttonBack: Button = findViewById(R.id.back)
        buttonBack.setOnClickListener {
            val intent = Intent(this@RedactActivity, MainActivity::class.java)
            startActivity(intent)
        }

        val buttonCancel: Button = findViewById(R.id.cancel)
        buttonCancel.setOnClickListener {
            if (!check) {
                if (bitmapList.size > 1) {
                    bitmapList.removeAt(bitmapList.size - 1)
                    originalBitmap = bitmapList.get(bitmapList.size - 1)
                    image.setImageBitmap(originalBitmap)
                }
            }
        }

        val buttonComplete: Button = findViewById(R.id.complete)
        buttonComplete.setOnClickListener {
            if (!check) saveImageToGallery(this@RedactActivity, originalBitmap, "Processed_Image")
        }

        val seekBar2: SeekBar = findViewById(R.id.seekBar2)
        val seekBar1: SeekBar = findViewById(R.id.seekBar1)
        val seekBar3: SeekBar = findViewById(R.id.seekBar3)

        val buttonExit: Button = findViewById(R.id.exit)
        val buttonApply: Button = findViewById(R.id.apply)
        val buttonCancelRetouch: Button = findViewById(R.id.cancelRetouch)
        val buttonRotation90: Button = findViewById(R.id.rotation90)

        var changeList = ArrayList<Bitmap>()

        fun regVisibile() {
            seekBar2.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            seekBar1.visibility = View.GONE
            seekBar3.visibility = View.GONE
            buttonExit.visibility = View.VISIBLE
            buttonCancelRetouch.visibility = View.GONE
            buttonRotation90.visibility = View.GONE

            image.setOnTouchListener(null)
        }
        fun regGone() {
            seekBar2.visibility = View.GONE
            buttonApply.visibility = View.GONE
            buttonExit.visibility = View.GONE
            seekBar1.visibility = View.GONE
            seekBar3.visibility = View.GONE
            buttonCancelRetouch.visibility = View.GONE
            buttonRotation90.visibility = View.GONE
        }

        buttonApply.setOnClickListener {
            regGone()
            image.setOnTouchListener(null)
            val lastImg = image.drawable.toBitmap()
            bitmapList.add(lastImg)
            originalBitmap = lastImg
            changeList.clear()
        }

        buttonExit.setOnClickListener {
            image.setOnTouchListener(null)
            regGone()
            image.setImageBitmap(originalBitmap)
        }

        val loadingOverlay: FrameLayout = findViewById(R.id.loadingOverlay)

        val buttonGauss: Button = findViewById(R.id.gauss)
        buttonGauss.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 99
            seekBar2.progress = 19
            val valSeekBar2: TextView = findViewById(R.id.valSeekBar2)
            valSeekBar2.text = (seekBar2.progress + 1).toString()

            val gauss = Gauss()
            loadingOverlay.visibility = View.VISIBLE
            GlobalScope.launch(Dispatchers.Main) {
                changeBitmap = gauss.gaussianBlur(originalBitmap, seekBar2.progress + 1){
                    loadingOverlay.visibility = View.GONE
                }
                image.setImageBitmap(changeBitmap)
            }

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar2.text = (seekBar2.progress + 1).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val radius = seekBar?.progress ?: 0
                    loadingOverlay.visibility = View.VISIBLE
                    GlobalScope.launch(Dispatchers.Main) {
                        valSeekBar2.text = (seekBar2.progress + 1).toString()
                        changeBitmap = gauss.gaussianBlur(originalBitmap, radius + 1){
                            loadingOverlay.visibility = View.GONE
                        }
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
        }

        val buttonMosaic: Button = findViewById(R.id.mosaic)
        buttonMosaic.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 95
            seekBar2.progress = 10

            val valSeekBar2: TextView = findViewById(R.id.valSeekBar2)
            valSeekBar2.text = (seekBar2.progress + 5).toString()

            val mosaic = Mosaic()
            loadingOverlay.visibility = View.VISIBLE
            changeBitmap = mosaic.mosaic(originalBitmap, seekBar2.progress + 5){
                loadingOverlay.visibility = View.GONE
            }
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar2.text = (seekBar2.progress + 5).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val mosaicSize = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress + 5).toString()
                    loadingOverlay.visibility = View.VISIBLE
                    changeBitmap = mosaic.mosaic(originalBitmap, mosaicSize + 5){
                        loadingOverlay.visibility = View.GONE
                    }
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonContrast: Button = findViewById(R.id.contrast)
        buttonContrast.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 200
            seekBar2.progress = 100

            val valSeekBar2: TextView = findViewById(R.id.valSeekBar2)
            valSeekBar2.text = (seekBar2.progress - 100).toString()

            val contrast = Contrast()
            loadingOverlay.visibility = View.VISIBLE
            changeBitmap = contrast.contrast(originalBitmap, seekBar2.progress - 100){
                loadingOverlay.visibility = View.GONE
            }
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar2.text = (seekBar2.progress - 100).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val contrastVal = seekBar?.progress ?: 100
                    valSeekBar2.text = (seekBar2.progress - 100).toString()
                    loadingOverlay.visibility = View.VISIBLE
                    changeBitmap = contrast.contrast(originalBitmap, contrastVal - 100){
                        loadingOverlay.visibility = View.GONE
                    }
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonBlackWhite: Button = findViewById(R.id.blackWhite)
        buttonBlackWhite.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 100
            seekBar2.progress = 50

            val valSeekBar2: TextView = findViewById(R.id.valSeekBar2)
            valSeekBar2.text = (seekBar2.progress / 100.0).toString()

            val blackWhite = BlackWhite()
            loadingOverlay.visibility = View.VISIBLE
            changeBitmap = blackWhite.blackAndWhite(originalBitmap, seekBar2.progress / 100.0){
                loadingOverlay.visibility = View.GONE
            }
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar2.text = (seekBar2.progress / 100.0).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress / 100.0).toString()
                    loadingOverlay.visibility = View.VISIBLE
                    changeBitmap = blackWhite.blackAndWhite(originalBitmap, intensity / 100.0){
                        loadingOverlay.visibility = View.GONE
                    }
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonInvercy: Button = findViewById(R.id.invert)
        buttonInvercy.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 255
            seekBar2.progress = 150

            val valSeekBar2: TextView = findViewById(R.id.valSeekBar2)
            valSeekBar2.text = (seekBar2.progress).toString()

            val inverse = Invercy()
            loadingOverlay.visibility = View.VISIBLE
            changeBitmap = inverse.Inversion(originalBitmap, seekBar2.progress){
                loadingOverlay.visibility = View.GONE
            }
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar2.text = (seekBar2.progress).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val threshold = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress).toString()
                    loadingOverlay.visibility = View.VISIBLE
                    changeBitmap = inverse.Inversion(originalBitmap, threshold){
                        loadingOverlay.visibility = View.GONE
                    }
                    image.setImageBitmap(changeBitmap)
                }
            })

        }

        val buttonRed: Button = findViewById(R.id.red)
        buttonRed.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 510
            seekBar2.progress = 255

            val valSeekBar2: TextView = findViewById(R.id.valSeekBar2)
            valSeekBar2.text = (seekBar2.progress - 255).toString()

            val rgb = RGB()
            loadingOverlay.visibility = View.VISIBLE
            changeBitmap = rgb.rgbFilter(originalBitmap, seekBar2.progress - 255, "red"){
                loadingOverlay.visibility = View.GONE
            }
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar2.text = (seekBar2.progress - 255).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 255
                    valSeekBar2.text = (seekBar2.progress - 255).toString()
                    loadingOverlay.visibility = View.VISIBLE
                    changeBitmap = rgb.rgbFilter(originalBitmap, intensity - 255, "red"){
                        loadingOverlay.visibility = View.GONE
                    }
                    image.setImageBitmap(changeBitmap)
                }
            })
        }
        val buttonGreen: Button = findViewById(R.id.green)
        buttonGreen.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 510
            seekBar2.progress = 255

            val valSeekBar2: TextView = findViewById(R.id.valSeekBar2)
            valSeekBar2.text = (seekBar2.progress - 255).toString()

            val rgb = RGB()
            loadingOverlay.visibility = View.VISIBLE
            changeBitmap = rgb.rgbFilter(originalBitmap, seekBar2.progress - 255, "green"){
                loadingOverlay.visibility = View.GONE
            }
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar2.text = (seekBar2.progress - 255).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 255
                    valSeekBar2.text = (seekBar2.progress - 255).toString()
                    loadingOverlay.visibility = View.VISIBLE
                    changeBitmap = rgb.rgbFilter(originalBitmap, intensity - 255, "green"){
                        loadingOverlay.visibility = View.GONE
                    }
                    image.setImageBitmap(changeBitmap)
                }
            })
        }
        val buttonBlue: Button = findViewById(R.id.blue)
        buttonBlue.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 510
            seekBar2.progress = 255

            val valSeekBar2: TextView = findViewById(R.id.valSeekBar2)
            valSeekBar2.text = (seekBar2.progress - 255).toString()

            val rgb = RGB()
            loadingOverlay.visibility = View.VISIBLE
            changeBitmap = rgb.rgbFilter(originalBitmap, seekBar2.progress - 255, "blue"){
                loadingOverlay.visibility = View.GONE
            }
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar2.text = (seekBar2.progress - 255).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 255
                    valSeekBar2.text = (seekBar2.progress - 255).toString()
                    loadingOverlay.visibility = View.VISIBLE
                    changeBitmap = rgb.rgbFilter(originalBitmap, intensity - 255, "blue"){
                        loadingOverlay.visibility = View.GONE
                    }
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonSepia: Button = findViewById(R.id.sepia)
        buttonSepia.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 100
            seekBar2.progress = 50

            val valSeekBar2: TextView = findViewById(R.id.valSeekBar2)
            valSeekBar2.text = (seekBar2.progress).toString()

            val sepia = Sepia()
            loadingOverlay.visibility = View.VISIBLE
            changeBitmap = sepia.sepiaFilter(originalBitmap, seekBar2.progress){
                loadingOverlay.visibility = View.GONE
            }
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar2.text = (seekBar2.progress).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val sepiaDepth = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress).toString()
                    loadingOverlay.visibility = View.VISIBLE
                    changeBitmap = sepia.sepiaFilter(originalBitmap, sepiaDepth){
                        loadingOverlay.visibility = View.GONE
                    }
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonSharpness: Button = findViewById(R.id.unsharpedMask)
        buttonSharpness.setOnClickListener {
            setOriginalBitmap()

            seekBar2.visibility = View.VISIBLE
            seekBar1.visibility = View.VISIBLE
            seekBar3.visibility = View.VISIBLE

            buttonApply.visibility = View.VISIBLE
            buttonExit.visibility = View.VISIBLE
            buttonCancelRetouch.visibility = View.GONE
            buttonRotation90.visibility = View.GONE

            image.setOnTouchListener(null)

            val textThreshold: TextView = findViewById(R.id.textViewSeekBar1)
            textThreshold.text = "Threshold"
            val textRadius: TextView = findViewById(R.id.textViewSeekBar3)
            textRadius.text = "Radius"

            seekBar3.max = 99
            seekBar3.progress = 4
            seekBar2.max = 200
            seekBar2.progress = 150
            seekBar1.max = 255
            seekBar1.progress = 10
            var strength: Int = seekBar2.progress
            var radius: Int = seekBar3.progress
            var threshold: Int = seekBar1.progress

            val valSeekBar1: TextView = findViewById(R.id.valSeekBar1)
            valSeekBar1.text = (seekBar1.progress).toString()
            val valSeekBar2: TextView = findViewById(R.id.valSeekBar2)
            valSeekBar2.text = (seekBar2.progress / 100.0).toString()
            val valSeekBar3: TextView = findViewById(R.id.valSeekBar3)
            valSeekBar3.text = (seekBar3.progress + 1).toString()

            val sharp = Unsharp()
            loadingOverlay.visibility = View.VISIBLE
            GlobalScope.launch(Dispatchers.Main) {
                changeBitmap =
                    sharp.sharpenImage(originalBitmap, strength / 100.0, threshold, radius + 1){
                        loadingOverlay.visibility = View.GONE
                    }
                image.setImageBitmap(changeBitmap)
            }

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar2.text = (seekBar2.progress / 100.0).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    strength = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress / 100.0).toString()
                    loadingOverlay.visibility = View.VISIBLE
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap =
                            sharp.sharpenImage(originalBitmap, strength / 100.0, threshold, radius + 1){
                                loadingOverlay.visibility = View.GONE
                            }
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
            seekBar3.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar3.text = (seekBar3.progress + 1).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    radius = seekBar3?.progress ?: 0
                    valSeekBar3.text = (seekBar3.progress + 1).toString()
                    loadingOverlay.visibility = View.VISIBLE
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap =
                            sharp.sharpenImage(originalBitmap, strength / 100.0, threshold, radius + 1){
                                loadingOverlay.visibility = View.GONE
                            }
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
            seekBar1.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar1.text = (seekBar1.progress).toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    threshold = seekBar1?.progress ?: 0
                    valSeekBar1.text = (seekBar1.progress).toString()
                    loadingOverlay.visibility = View.VISIBLE
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap =
                            sharp.sharpenImage(originalBitmap, strength / 100.0, threshold, radius + 1){
                                loadingOverlay.visibility = View.GONE
                            }
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
        }

        val buttonRetouch: Button = findViewById(R.id.retouch)
        buttonRetouch.setOnClickListener {
            setOriginalBitmap()
            changeList.add(originalBitmap)

            seekBar2.visibility = View.GONE
            seekBar1.visibility = View.VISIBLE
            seekBar3.visibility = View.VISIBLE

            buttonApply.visibility = View.VISIBLE
            buttonExit.visibility = View.VISIBLE
            buttonCancelRetouch.visibility = View.VISIBLE
            buttonRotation90.visibility = View.GONE

            seekBar1.max = 100
            seekBar3.max = 95

            val valSeekBar1: TextView = findViewById(R.id.valSeekBar1)
            valSeekBar1.text = (seekBar1.progress).toString()
            val valSeekBar3: TextView = findViewById(R.id.valSeekBar3)
            valSeekBar3.text = (seekBar3.progress + 5).toString()

            val textIntencity: TextView = findViewById(R.id.textViewSeekBar1)
            textIntencity.text = "Intencity"
            val textBrushSize: TextView = findViewById(R.id.textViewSeekBar3)
            textBrushSize.text = "Brush Size"

            var brushSize: Int = seekBar1.progress
            var intencity: Int = seekBar3.progress

            seekBar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar1.text = (seekBar1.progress).toString()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    intencity = seekBar?.progress ?: 0
                    valSeekBar1.text = (seekBar1.progress).toString()
                }
            })
            seekBar3.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    valSeekBar3.text = (seekBar3.progress + 5).toString()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    brushSize = seekBar?.progress ?: 0
                    valSeekBar3.text = (seekBar3.progress + 5).toString()
                }
            })

            val retouch = Retouch()
            changeBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            image.setImageBitmap(changeBitmap)

            image.setOnTouchListener { _, event ->
                val x = event.x
                val y = event.y


                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        val curX = (x * originalBitmap.height / image.height).toInt()
                        val curY = (y * originalBitmap.height / image.height).toInt()
                        changeBitmap = retouch.applyRetouching(curX.toFloat(), curY.toFloat(), changeBitmap, intencity, brushSize + 5)
                        image.setImageBitmap(changeBitmap)
                        true
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        changeList.add(changeBitmap)
                        true
                    }

                    else -> false
                }
            }
        }

        buttonCancelRetouch.setOnClickListener {
            if (changeList.size > 1) changeList.removeAt(changeList.size - 1)
            changeBitmap = changeList.get(changeList.size - 1)
            image.setImageBitmap(changeBitmap)
        }

        val buttonFace: Button = findViewById(R.id.face)
        buttonFace.setOnClickListener {
            setOriginalBitmap()
            regGone()
            val face = Face()
            //loadingOverlay.visibility = View.VISIBLE
            changeBitmap = face.face(originalBitmap)
                    //{
                    //    loadingOverlay.visibility = View.GONE
                    //}
            image.setImageBitmap(changeBitmap)

        }
        var angleFor90 = 90
        var flag = false
        buttonRotation90.setOnClickListener {
            val rotate = Rotate()
            flag = true
            changeBitmap = rotate.rotate(originalBitmap, angleFor90.toFloat(), flag)
            image.setImageBitmap(changeBitmap)
            angleFor90 += 90
            if (angleFor90 == 0) {
                angleFor90 = 90
            }
        }


        val buttonRotate: Button = findViewById(R.id.rotate)
        buttonRotate.setOnClickListener {
            flag = false
            setOriginalBitmap()
            regVisibile()
            buttonRotation90.visibility = View.VISIBLE

            seekBar2.max = 360
            seekBar2.progress = 0
            val textSeekBar2: TextView = findViewById(R.id.textViewSeekBar2)
            textSeekBar2.text = "Angle"

            val valSeekBar2: TextView = findViewById(R.id.valSeekBar2)
            valSeekBar2.text = (seekBar2.progress).toString()

            val rotate = Rotate()
            changeBitmap = rotate.rotate(originalBitmap, seekBar2.progress.toFloat(), flag)
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val angle = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress).toString()
                    changeBitmap = rotate.rotate(originalBitmap, angle.toFloat(), flag)
                    image.setImageBitmap(changeBitmap)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val angle = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress).toString()
                    changeBitmap = rotate.rotate(originalBitmap, angle.toFloat(), flag)
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LOW_PROFILE
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    private fun saveImageToGallery(context: Context, bitmap: Bitmap, title: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$title.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { imageUri ->
            resolver.openOutputStream(imageUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        }
    }

    private fun setOriginalBitmap() {
        if (check) {
            originalBitmap = image.drawable.toBitmap()
            bitmapList.add(originalBitmap)
            check = false
        }
    }
}