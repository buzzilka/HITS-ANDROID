package com.example.hits_android

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.viewmodel.MainViewModel
import com.example.hits_android.filters.*
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
                if (bitmapList.size > 1) bitmapList.removeAt(bitmapList.size - 1)
                originalBitmap = bitmapList.get(bitmapList.size - 1)
                image.setImageBitmap(originalBitmap)
            }
        }

        val buttonComplete: Button = findViewById(R.id.complete)
        buttonComplete.setOnClickListener {
            if (!check) saveImageToGallery(this@RedactActivity, originalBitmap, "Processed_Image")
        }

        val seekBar: SeekBar = findViewById(R.id.seekBar)
        val seekBarSharpThreshold: SeekBar = findViewById(R.id.seekBarSharpThreshold)
        val seekBarSharpRadius: SeekBar = findViewById(R.id.seekBarSharpRadius)

        val buttonExit: Button = findViewById(R.id.exit)
        val buttonApply: Button = findViewById(R.id.apply)

        fun regVisibile() {
            seekBar.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
            buttonExit.visibility = View.VISIBLE
        }

        fun regVisibileSharp() {
            seekBar.visibility = View.VISIBLE
            seekBarSharpThreshold.visibility = View.VISIBLE
            seekBarSharpRadius.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            buttonExit.visibility = View.VISIBLE
        }

        fun regGone() {
            seekBar.visibility = View.GONE
            buttonApply.visibility = View.GONE
            buttonExit.visibility = View.GONE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
        }

        buttonApply.setOnClickListener {
            regGone()
            bitmapList.add(changeBitmap)
            originalBitmap = changeBitmap
        }

        buttonExit.setOnClickListener {
            regGone()
        }

        val buttonGauss: Button = findViewById(R.id.gauss)
        buttonGauss.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar.max = 99
            seekBar.progress = 20

            val gauss = Gauss()
            GlobalScope.launch(Dispatchers.Main) {
                changeBitmap = gauss.gaussianBlur(originalBitmap, seekBar.progress + 1)
                image.setImageBitmap(changeBitmap)
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val radius = seekBar?.progress ?: 0
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = gauss.gaussianBlur(originalBitmap, radius + 1)
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
        }

        val buttonMosaic: Button = findViewById(R.id.mosaic)
        buttonMosaic.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar.max = 95
            seekBar.progress = 10

            val mosaic = Mosaic()
            changeBitmap = mosaic.mosaic(originalBitmap, seekBar.progress + 5)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val mosaicSize = seekBar?.progress ?: 0
                    changeBitmap = mosaic.mosaic(originalBitmap, mosaicSize + 5)
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonContrast: Button = findViewById(R.id.contrast)
        buttonContrast.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar.max = 200
            seekBar.progress = 100

            val contrast = Contrast()
            changeBitmap = contrast.contrast(originalBitmap, seekBar.progress - 100)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val contrastVal = seekBar?.progress ?: 100
                    changeBitmap = contrast.contrast(originalBitmap, contrastVal - 100)
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonBlackWhite: Button = findViewById(R.id.blackWhite)
        buttonBlackWhite.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar.max = 100
            seekBar.progress = 50

            val blackWhite = BlackWhite()
            changeBitmap = blackWhite.blackAndWhite(originalBitmap, seekBar.progress / 100.0)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 0
                    changeBitmap = blackWhite.blackAndWhite(originalBitmap, intensity / 100.0)
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonInvercy: Button = findViewById(R.id.invert)
        buttonInvercy.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar.max = 255
            seekBar.progress = 150

            val inverse = Invercy()
            changeBitmap = inverse.Inversion(originalBitmap, seekBar.progress)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val threshold = seekBar?.progress ?: 0
                    changeBitmap = inverse.Inversion(originalBitmap, threshold)
                    image.setImageBitmap(changeBitmap)
                }
            })

        }

        val buttonRed: Button = findViewById(R.id.red)
        buttonRed.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar.max = 510
            seekBar.progress = 255

            val rgb = RGB()
            changeBitmap = rgb.rgbFilter(originalBitmap, seekBar.progress - 255, "red")
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 255
                    changeBitmap = rgb.rgbFilter(originalBitmap, intensity - 255, "red")
                    image.setImageBitmap(changeBitmap)
                }
            })
        }
        val buttonGreen: Button = findViewById(R.id.green)
        buttonGreen.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar.max = 510
            seekBar.progress = 255

            val rgb = RGB()
            changeBitmap = rgb.rgbFilter(originalBitmap, seekBar.progress - 255, "green")
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 255
                    changeBitmap = rgb.rgbFilter(originalBitmap, intensity - 255, "green")
                    image.setImageBitmap(changeBitmap)
                }
            })
        }
        val buttonBlue: Button = findViewById(R.id.blue)
        buttonBlue.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar.max = 510
            seekBar.progress = 255

            val rgb = RGB()
            changeBitmap = rgb.rgbFilter(originalBitmap, seekBar.progress - 255, "blue")
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 255
                    changeBitmap = rgb.rgbFilter(originalBitmap, intensity - 255, "blue")
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonSepia: Button = findViewById(R.id.sepia)
        buttonSepia.setOnClickListener {
            setOriginalBitmap()
            regVisibile()
            seekBar.max = 100
            seekBar.progress = 50

            val sepia = Sepia()
            changeBitmap = sepia.sepiaFilter(originalBitmap, seekBar.progress)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val sepiaDepth = seekBar?.progress ?: 0
                    changeBitmap = sepia.sepiaFilter(originalBitmap, sepiaDepth)
                    image.setImageBitmap(changeBitmap)
                }
            })
        }


        val buttonSharpness: Button = findViewById(R.id.unsharpedMask)
        buttonSharpness.setOnClickListener {
            setOriginalBitmap()
            regVisibileSharp()

            seekBarSharpRadius.max = 99
            seekBarSharpRadius.progress = 5
            seekBar.max = 200
            seekBar.progress = 150
            seekBarSharpThreshold.max = 255
            seekBarSharpThreshold.progress = 10
            var strength: Int = seekBar.progress
            var radius: Int = seekBarSharpRadius.progress
            var threshold: Int = seekBarSharpThreshold.progress

            val sharp = Unsharp()
            GlobalScope.launch(Dispatchers.Main) {
                changeBitmap =
                    sharp.sharpenImage(originalBitmap, strength / 100.0, threshold, radius + 1)
                image.setImageBitmap(changeBitmap)
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    strength = seekBar?.progress ?: 0
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = sharp.sharpenImage(
                            originalBitmap,
                            strength / 100.0,
                            threshold,
                            radius + 1
                        )
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
            seekBarSharpRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    radius = seekBarSharpRadius?.progress ?: 0
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = sharp.sharpenImage(
                            originalBitmap,
                            strength / 100.0,
                            threshold,
                            radius + 1
                        )
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
            seekBarSharpThreshold.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    threshold = seekBarSharpThreshold?.progress ?: 0
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = sharp.sharpenImage(
                            originalBitmap,
                            strength / 100.0,
                            threshold,
                            radius + 1
                        )
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