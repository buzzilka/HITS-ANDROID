package com.example.hits_android

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import com.example.hits_android.detect.*
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

        val textSeekBar2: TextView = findViewById(R.id.textViewSeekBar2)
        val textSeekBar1: TextView = findViewById(R.id.textViewSeekBar1)
        val textSeekBar3: TextView = findViewById(R.id.textViewSeekBar3)

        val valSeekBar2: TextView = findViewById(R.id.valSeekBar2)
        val valSeekBar1: TextView = findViewById(R.id.valSeekBar1)
        val valSeekBar3: TextView = findViewById(R.id.valSeekBar3)

        var changeList = ArrayList<Bitmap>()

        fun regVisibile() {
            seekBar2.visibility = View.VISIBLE
            textSeekBar2.visibility = View.VISIBLE
            textSeekBar2.text = "Intencity"
            valSeekBar2.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            seekBar1.visibility = View.GONE
            textSeekBar1.visibility = View.GONE
            valSeekBar1.visibility = View.GONE
            seekBar3.visibility = View.GONE
            textSeekBar3.visibility = View.GONE
            valSeekBar3.visibility = View.GONE
            buttonExit.visibility = View.VISIBLE
            buttonCancelRetouch.visibility = View.GONE
            buttonRotation90.visibility = View.GONE

            image.setOnTouchListener(null)
        }
        fun regGone() {
            seekBar2.visibility = View.GONE
            textSeekBar2.visibility = View.GONE
            valSeekBar2.visibility = View.GONE
            buttonApply.visibility = View.GONE
            buttonExit.visibility = View.GONE
            seekBar1.visibility = View.GONE
            textSeekBar1.visibility = View.GONE
            valSeekBar1.visibility = View.GONE
            seekBar3.visibility = View.GONE
            textSeekBar3.visibility = View.GONE
            valSeekBar3.visibility = View.GONE
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
                    valSeekBar2.text = (seekBar2.progress + 1).toString()
                    GlobalScope.launch(Dispatchers.Main) {
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

            valSeekBar2.text = (seekBar2.progress + 5).toString()

            val mosaic = Mosaic()
            changeBitmap = mosaic.mosaic(originalBitmap, seekBar2.progress + 5)
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val mosaicSize = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress + 5).toString()
                    changeBitmap = mosaic.mosaic(originalBitmap, mosaicSize + 5)
                    image.setImageBitmap(changeBitmap)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val mosaicSize = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress + 5).toString()
                    changeBitmap = mosaic.mosaic(originalBitmap, mosaicSize + 5)
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

            valSeekBar2.text = (seekBar2.progress - 100).toString()

            val contrast = Contrast()
            changeBitmap = contrast.contrast(originalBitmap, seekBar2.progress - 100)
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val contrastVal = seekBar?.progress ?: 100
                    valSeekBar2.text = (seekBar2.progress - 100).toString()
                    changeBitmap = contrast.contrast(originalBitmap, contrastVal - 100)
                    image.setImageBitmap(changeBitmap)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val contrastVal = seekBar?.progress ?: 100
                    valSeekBar2.text = (seekBar2.progress - 100).toString()
                    changeBitmap = contrast.contrast(originalBitmap, contrastVal - 100)
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

            valSeekBar2.text = (seekBar2.progress / 100.0).toString()

            val blackWhite = BlackWhite()
            GlobalScope.launch(Dispatchers.Main) {
                changeBitmap = blackWhite.blackAndWhite(originalBitmap, seekBar2.progress / 100.0)
                image.setImageBitmap(changeBitmap)
            }

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val intensity = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress / 100.0).toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = blackWhite.blackAndWhite(originalBitmap, intensity / 100.0)
                        image.setImageBitmap(changeBitmap)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress / 100.0).toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = blackWhite.blackAndWhite(originalBitmap, intensity / 100.0)
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
        }

        val buttonInvercy: Button = findViewById(R.id.invert)
        buttonInvercy.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 255
            seekBar2.progress = 150

            valSeekBar2.text = (seekBar2.progress).toString()

            val inverse = Invercy()
            changeBitmap = inverse.Inversion(originalBitmap, seekBar2.progress)
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val threshold = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress).toString()
                    changeBitmap = inverse.Inversion(originalBitmap, threshold)
                    image.setImageBitmap(changeBitmap)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val threshold = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress).toString()
                    changeBitmap = inverse.Inversion(originalBitmap, threshold)
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

            valSeekBar2.text = (seekBar2.progress - 255).toString()

            val rgb = RGB()
            GlobalScope.launch(Dispatchers.Main) {
                changeBitmap = rgb.rgbFilter(originalBitmap, seekBar2.progress - 255, "red")
                image.setImageBitmap(changeBitmap)
            }

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val intensity = seekBar?.progress ?: 255
                    valSeekBar2.text = (seekBar2.progress - 255).toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = rgb.rgbFilter(originalBitmap, intensity - 255, "red")
                        image.setImageBitmap(changeBitmap)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 255
                    valSeekBar2.text = (seekBar2.progress - 255).toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = rgb.rgbFilter(originalBitmap, intensity - 255, "red")
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
        }
        val buttonGreen: Button = findViewById(R.id.green)
        buttonGreen.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 510
            seekBar2.progress = 255

            valSeekBar2.text = (seekBar2.progress - 255).toString()

            val rgb = RGB()
            GlobalScope.launch(Dispatchers.Main) {
                changeBitmap = rgb.rgbFilter(originalBitmap, seekBar2.progress - 255, "green")
                image.setImageBitmap(changeBitmap)
            }

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val intensity = seekBar?.progress ?: 255
                    valSeekBar2.text = (seekBar2.progress - 255).toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = rgb.rgbFilter(originalBitmap, intensity - 255, "green")
                        image.setImageBitmap(changeBitmap)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 255
                    valSeekBar2.text = (seekBar2.progress - 255).toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = rgb.rgbFilter(originalBitmap, intensity - 255, "green")
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
        }
        val buttonBlue: Button = findViewById(R.id.blue)
        buttonBlue.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 510
            seekBar2.progress = 255

            valSeekBar2.text = (seekBar2.progress - 255).toString()

            val rgb = RGB()
            GlobalScope.launch(Dispatchers.Main) {
                changeBitmap = rgb.rgbFilter(originalBitmap, seekBar2.progress - 255, "blue")
                image.setImageBitmap(changeBitmap)
            }

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val intensity = seekBar?.progress ?: 255
                    valSeekBar2.text = (seekBar2.progress - 255).toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = rgb.rgbFilter(originalBitmap, intensity - 255, "blue")
                        image.setImageBitmap(changeBitmap)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 255
                    valSeekBar2.text = (seekBar2.progress - 255).toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = rgb.rgbFilter(originalBitmap, intensity - 255, "blue")
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
        }

        val buttonSepia: Button = findViewById(R.id.sepia)
        buttonSepia.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar2.max = 100
            seekBar2.progress = 50

            valSeekBar2.text = (seekBar2.progress).toString()

            val sepia = Sepia()
            changeBitmap = sepia.sepiaFilter(originalBitmap, seekBar2.progress)
            image.setImageBitmap(changeBitmap)

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val sepiaDepth = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress).toString()
                    changeBitmap = sepia.sepiaFilter(originalBitmap, sepiaDepth)
                    image.setImageBitmap(changeBitmap)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val sepiaDepth = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress).toString()
                    changeBitmap = sepia.sepiaFilter(originalBitmap, sepiaDepth)
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonSharpness: Button = findViewById(R.id.unsharpedMask)
        buttonSharpness.setOnClickListener {
            setOriginalBitmap()

            seekBar2.visibility = View.VISIBLE
            textSeekBar2.visibility = View.VISIBLE
            valSeekBar2.visibility = View.VISIBLE
            seekBar1.visibility = View.VISIBLE
            textSeekBar1.visibility = View.VISIBLE
            valSeekBar1.visibility = View.VISIBLE
            seekBar3.visibility = View.VISIBLE
            textSeekBar3.visibility = View.VISIBLE
            valSeekBar3.visibility = View.VISIBLE

            buttonApply.visibility = View.VISIBLE
            buttonExit.visibility = View.VISIBLE
            buttonCancelRetouch.visibility = View.GONE
            buttonRotation90.visibility = View.GONE

            image.setOnTouchListener(null)

            textSeekBar1.text = "Threshold"
            textSeekBar3.text = "Radius"

            seekBar3.max = 99
            seekBar3.progress = 4
            seekBar2.max = 200
            seekBar2.progress = 150
            seekBar1.max = 255
            seekBar1.progress = 10
            var strength: Int = seekBar2.progress
            var radius: Int = seekBar3.progress
            var threshold: Int = seekBar1.progress

            valSeekBar1.text = (seekBar1.progress).toString()
            valSeekBar2.text = (seekBar2.progress / 100.0).toString()
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
            textSeekBar2.visibility = View.GONE
            valSeekBar2.visibility = View.GONE
            seekBar1.visibility = View.VISIBLE
            textSeekBar1.visibility = View.VISIBLE
            valSeekBar1.visibility = View.VISIBLE
            seekBar3.visibility = View.VISIBLE
            textSeekBar3.visibility = View.VISIBLE
            valSeekBar3.visibility = View.VISIBLE

            buttonApply.visibility = View.VISIBLE
            buttonExit.visibility = View.VISIBLE
            buttonCancelRetouch.visibility = View.VISIBLE
            buttonRotation90.visibility = View.GONE

            seekBar1.max = 100
            seekBar3.max = 95

            valSeekBar1.text = (seekBar1.progress).toString()
            valSeekBar3.text = (seekBar3.progress + 5).toString()

            textSeekBar1.text = "Intencity"
            textSeekBar3.text = "Brush Size"

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
                        var curX : Float
                        var curY : Float
                        if (originalBitmap.width.toFloat() / image.width.toFloat() > originalBitmap.height.toFloat() / image.height.toFloat()) {
                            curX = x / (image.width.toFloat() / originalBitmap.width.toFloat())
                            curY = (y - (image.height.toFloat() - (image.width.toFloat() / originalBitmap.width.toFloat()) * originalBitmap.height.toFloat()) / 2) / (image.width.toFloat() / originalBitmap.width.toFloat())
                        }
                        else {
                            curX = (x - (image.width.toFloat() - (image.height.toFloat() / originalBitmap.height.toFloat()) * originalBitmap.width.toFloat()) / 2) / (image.height.toFloat() / originalBitmap.height.toFloat())
                            curY = y / (image.height.toFloat() / originalBitmap.height.toFloat())
                        }

                        curX = curX.coerceIn(0F, originalBitmap.width.toFloat() - 1)
                        curY = curY.coerceIn(0F, originalBitmap.height.toFloat() - 1)

                        changeBitmap = retouch.applyRetouching(curX, curY, changeBitmap, intencity, brushSize + 5)
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
            buttonApply.visibility = View.VISIBLE
            val face = Face()
            changeBitmap = face.apply(originalBitmap, this)
            image.setImageBitmap(changeBitmap)
        }

        val buttonPerson: Button = findViewById(R.id.person)
        buttonPerson.setOnClickListener {
            setOriginalBitmap()
            regGone()
            buttonApply.visibility = View.VISIBLE
            val person = Person()
            changeBitmap = person.apply(originalBitmap, this)
            image.setImageBitmap(changeBitmap)
        }


        var angleFor90 = 90
        buttonRotation90.setOnClickListener {
            val rotate = Rotate()
            GlobalScope.launch(Dispatchers.Main) {
                changeBitmap = rotate.rotate(originalBitmap, angleFor90.toFloat())
                image.setImageBitmap(changeBitmap)
            }
            angleFor90 += 90
            if (angleFor90 == 0) {
                angleFor90 = 90
            }
        }


        val buttonRotate: Button = findViewById(R.id.rotate)
        buttonRotate.setOnClickListener {
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
            GlobalScope.launch(Dispatchers.Main) {
                changeBitmap = rotate.rotate(originalBitmap, seekBar2.progress.toFloat())
                image.setImageBitmap(changeBitmap)
            }

            seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val angle = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress).toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = rotate.rotate(originalBitmap, angle.toFloat())
                        image.setImageBitmap(changeBitmap)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val angle = seekBar?.progress ?: 0
                    valSeekBar2.text = (seekBar2.progress).toString()
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = rotate.rotate(originalBitmap, angle.toFloat())
                        image.setImageBitmap(changeBitmap)
                    }
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