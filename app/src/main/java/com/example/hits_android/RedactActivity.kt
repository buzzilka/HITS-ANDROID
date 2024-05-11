package com.example.hits_android

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.lifecycle.ViewModelProvider
import androidx.core.graphics.drawable.toBitmap
import android.graphics.Color
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlin.math.*


class RedactActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var originalBitmap: Bitmap
    private lateinit var changeBitmap: Bitmap
    var bitmapList = ArrayList<Bitmap>()
    private lateinit var viewModel: MainViewModel

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

        val buttonBack : Button = findViewById(R.id.back)
        buttonBack.setOnClickListener {
            val intent = Intent(this@RedactActivity, MainActivity::class.java)
            startActivity(intent)
        }





        var check: Boolean = true
        val seekBar: SeekBar = findViewById(R.id.seekBar)

        val buttonCancel : Button = findViewById(R.id.cancel)
        buttonCancel.setOnClickListener {
            if (!check) {
                if (bitmapList.size > 1) bitmapList.removeAt(bitmapList.size - 1)
                originalBitmap = bitmapList.get(bitmapList.size - 1)
                image.setImageBitmap(originalBitmap)
            }
        }
        val seekBarSharpThreshold : SeekBar =  findViewById(R.id.seekBarSharpThreshold)
        val seekBarSharpRadius : SeekBar =  findViewById(R.id.seekBarSharpRadius)
        val buttonExit: Button = findViewById(R.id.exit)
        val buttonApply:Button = findViewById(R.id.apply)
        buttonApply.setOnClickListener {
            seekBar.visibility = View.GONE
            buttonApply.visibility = View.GONE
            buttonExit.visibility = View.GONE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
            bitmapList.add(changeBitmap)
            originalBitmap = changeBitmap
        }

        val savePermissionRequestLauncher: ActivityResultLauncher<String> =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    saveImageToGallery(this@RedactActivity, originalBitmap, "Processed_Image")
                } else {
                    Toast.makeText(
                        this,
                        "Go to settings and enable storage permission to save the image to gallery",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        fun handleSavePermission() {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    saveImageToGallery(this@RedactActivity, originalBitmap, "Processed_Image")
                }
                else -> {
                    savePermissionRequestLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }

        val buttonComplete:Button = findViewById(R.id.complete)
        buttonComplete.setOnClickListener {
            if (!check) handleSavePermission()
        }


        buttonExit.setOnClickListener {
            seekBar.visibility = View.GONE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
            buttonApply.visibility = View.GONE
            image.setImageBitmap(originalBitmap)
            buttonExit.visibility = View.GONE
        }

        val buttonGauss : Button = findViewById(R.id.gauss)
        buttonGauss.setOnClickListener {
            if (check) {
                originalBitmap = image.drawable.toBitmap()
                bitmapList.add(originalBitmap)
                check = false
            }
            seekBar.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
            buttonExit.visibility = View.VISIBLE
            seekBar.max = 99
            seekBar.progress = 20

            GlobalScope.launch(Dispatchers.Main) {
                changeBitmap = GaussianBlur(originalBitmap, seekBar.progress + 1)
                image.setImageBitmap(changeBitmap)
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val radius = seekBar?.progress ?: 0
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = GaussianBlur(originalBitmap, radius + 1)
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
        }

        val buttonMosaic : Button = findViewById(R.id.mosaic)
        buttonMosaic.setOnClickListener {
            if (check) {
                originalBitmap = image.drawable.toBitmap()
                bitmapList.add(originalBitmap)
                check = false
            }
            seekBar.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
            buttonExit.visibility = View.VISIBLE
            seekBar.max = 95
            seekBar.progress = 10

            changeBitmap = mosaic(originalBitmap, seekBar.progress + 5)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val mosaicSize = seekBar?.progress ?: 0
                    changeBitmap = mosaic(originalBitmap, mosaicSize + 5)
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonContrast : Button = findViewById(R.id.contrast)
        buttonContrast.setOnClickListener {
            if (check) {
                originalBitmap = image.drawable.toBitmap()
                bitmapList.add(originalBitmap)
                check = false
            }
            seekBar.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
            buttonExit.visibility = View.VISIBLE
            seekBar.max = 200
            seekBar.progress = 100

            changeBitmap = contrast(originalBitmap, seekBar.progress - 100)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val contrastVal = seekBar?.progress ?: 100
                    changeBitmap = contrast(originalBitmap, contrastVal - 100)
                    image.setImageBitmap(changeBitmap)
                }
            })
        }
        val buttonSharpness : Button = findViewById(R.id.unsharpedMask)
        buttonSharpness.setOnClickListener {
            if (check) {
                originalBitmap = image.drawable.toBitmap()
                bitmapList.add(originalBitmap)
                check = false
            }
            seekBar.visibility = View.VISIBLE
            seekBarSharpThreshold.visibility = View.VISIBLE
            seekBarSharpRadius.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            buttonExit.visibility = View.VISIBLE
            seekBarSharpRadius.max = 99
            seekBarSharpRadius.progress = 5
            seekBar.max = 200
            seekBar.progress = 150
            seekBarSharpThreshold.max = 255
            seekBarSharpThreshold.progress = 10
            var strength: Int = seekBar.progress
            var radius: Int = seekBarSharpRadius.progress
            var threshold: Int = seekBarSharpThreshold.progress

            GlobalScope.launch(Dispatchers.Main) {
                changeBitmap = sharpenImage(originalBitmap, strength / 100.0, threshold, radius + 1)
                image.setImageBitmap(changeBitmap)
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    strength = seekBar?.progress ?: 0
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = sharpenImage(originalBitmap, strength / 100.0, threshold, radius + 1)
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
            seekBarSharpRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    radius = seekBarSharpRadius?.progress ?: 0
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = sharpenImage(originalBitmap, strength / 100.0, threshold, radius + 1)
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })
            seekBarSharpThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    threshold = seekBarSharpThreshold?.progress ?: 0
                    GlobalScope.launch(Dispatchers.Main) {
                        changeBitmap = sharpenImage(originalBitmap, strength / 100.0, threshold, radius + 1)
                        image.setImageBitmap(changeBitmap)
                    }
                }
            })

        }

        val buttonBlackWhite : Button = findViewById(R.id.blackWhite)
        buttonBlackWhite.setOnClickListener {
            if (check) {
                originalBitmap = image.drawable.toBitmap()
                bitmapList.add(originalBitmap)
                check = false
            }
            seekBar.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
            buttonExit.visibility = View.VISIBLE
            seekBar.max = 100
            seekBar.progress = 50

            changeBitmap = blackAndWhite(originalBitmap, seekBar.progress / 100.0)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 0
                    changeBitmap = blackAndWhite(originalBitmap, intensity / 100.0)
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonInvercy : Button = findViewById(R.id.invert)
        buttonInvercy.setOnClickListener {
            if (check) {
                originalBitmap = image.drawable.toBitmap()
                bitmapList.add(originalBitmap)
                check = false
            }
            seekBar.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
            buttonExit.visibility = View.VISIBLE
            seekBar.max = 255
            seekBar.progress = 150

            changeBitmap = Inversion(originalBitmap, seekBar.progress)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val threshold = seekBar?.progress ?: 0
                    changeBitmap = Inversion(originalBitmap, threshold)
                    image.setImageBitmap(changeBitmap)
                }
            })

        }

        val buttonRed : Button = findViewById(R.id.red)
        buttonRed.setOnClickListener {
            if (check) {
                originalBitmap = image.drawable.toBitmap()
                bitmapList.add(originalBitmap)
                check = false
            }
            seekBar.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
            buttonExit.visibility = View.VISIBLE
            seekBar.max = 510
            seekBar.progress = 255

            changeBitmap = redFilter(originalBitmap, seekBar.progress - 255)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 255
                    changeBitmap = redFilter(originalBitmap, intensity - 255)
                    image.setImageBitmap(changeBitmap)
                }
            })
        }
        val buttonBlue : Button = findViewById(R.id.blue)
        buttonBlue.setOnClickListener {
            if (check) {
                originalBitmap = image.drawable.toBitmap()
                bitmapList.add(originalBitmap)
                check = false
            }
            seekBar.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
            buttonExit.visibility = View.VISIBLE
            seekBar.max = 510
            seekBar.progress = 255

            changeBitmap = blueFilter(originalBitmap, seekBar.progress - 255)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 255
                    changeBitmap = blueFilter(originalBitmap, intensity - 255)
                    image.setImageBitmap(changeBitmap)
                }
            })
        }
        val buttonGreen : Button = findViewById(R.id.green)
        buttonGreen.setOnClickListener {
            if (check) {
                originalBitmap = image.drawable.toBitmap()
                bitmapList.add(originalBitmap)
                check = false
            }
            seekBar.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
            buttonExit.visibility = View.VISIBLE
            seekBar.max = 510
            seekBar.progress = 255

            changeBitmap = greenFilter(originalBitmap, seekBar.progress - 255)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val intensity = seekBar?.progress ?: 255
                    changeBitmap = greenFilter(originalBitmap, intensity - 255)
                    image.setImageBitmap(changeBitmap)
                }
            })
        }

        val buttonSepia : Button = findViewById(R.id.sepia)
        buttonSepia.setOnClickListener {
            if (check) {
                originalBitmap = image.drawable.toBitmap()
                bitmapList.add(originalBitmap)
                check = false
            }
            seekBar.visibility = View.VISIBLE
            buttonApply.visibility = View.VISIBLE
            seekBarSharpThreshold.visibility = View.GONE
            seekBarSharpRadius.visibility = View.GONE
            buttonExit.visibility = View.VISIBLE
            seekBar.max = 100
            seekBar.progress = 50

            changeBitmap = sepiaFilter(originalBitmap, seekBar.progress)
            image.setImageBitmap(changeBitmap)

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val sepiaDepth = seekBar?.progress ?: 0
                    changeBitmap = sepiaFilter(originalBitmap, sepiaDepth)
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

    fun redFilter(bitmap: Bitmap, intensity: Int): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                var pixel = bitmap.getPixel(x, y)

                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                // Увеличиваем интенсивность красного цвета
                val newRed = red + intensity

                // Ограничиваем значение красного до 0 и 255
                val finalRed = when {
                    newRed > 255 -> 255
                    newRed < 0 -> 0
                    else -> newRed
                }

                // Собираем пиксель с новыми значениями
                pixel = Color.rgb(finalRed, green, blue)

                // Устанавливаем новый пиксель в результирующее изображение
                resultBitmap.setPixel(x, y, pixel)
            }
        }

        return resultBitmap
    }
    fun greenFilter(bitmap: Bitmap, intensity: Int): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                var pixel = bitmap.getPixel(x, y)

                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                // Увеличиваем интенсивность красного цвета
                val newGreen = green + intensity

                // Ограничиваем значение красного до 0 и 255
                val finalGreen = when {
                    newGreen > 255 -> 255
                    newGreen < 0 -> 0
                    else -> newGreen
                }

                // Собираем пиксель с новыми значениями
                pixel = Color.rgb(red, finalGreen, blue)

                // Устанавливаем новый пиксель в результирующее изображение
                resultBitmap.setPixel(x, y, pixel)
            }
        }

        return resultBitmap
    }
    fun blueFilter(bitmap: Bitmap, intensity: Int): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                var pixel = bitmap.getPixel(x, y)

                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                // Увеличиваем интенсивность красного цвета
                val newBlue = blue + intensity

                // Ограничиваем значение красного до 0 и 255
                val finalBlue = when {
                    newBlue > 255 -> 255
                    newBlue < 0 -> 0
                    else -> newBlue
                }

                // Собираем пиксель с новыми значениями
                pixel = Color.rgb(red, green, finalBlue)

                // Устанавливаем новый пиксель в результирующее изображение
                resultBitmap.setPixel(x, y, pixel)
            }
        }

        return resultBitmap
    }

    fun blackAndWhite(bitmap: Bitmap, intensity:Double): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val color =((Color.red(pixel) * 0.3 + Color.green(pixel) * 0.59 + Color.blue(pixel) * 0.11) * intensity).toInt().coerceIn(0, 255)
                resultBitmap.setPixel(x, y, Color.rgb(color, color, color))
            }
        }
        return resultBitmap
    }

    fun sepiaFilter(bitmap: Bitmap, sepiaDepth: Int): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)

            var tr = (0.393 * red + 0.769 * green + 0.189 * blue).toInt()
            var tg = (0.349 * red + 0.686 * green + 0.168 * blue).toInt()
            var tb = (0.272 * red + 0.534 * green + 0.131 * blue).toInt()

            tr = if (tr > 255) 255 else tr
            tg = if (tg > 255) 255 else tg
            tb = if (tb > 255) 255 else tb

            val grayscalePixel = Color.rgb(tr, tg, tb)

            val finalRed = (grayscalePixel shr 16 and 0xFF).toFloat()
            val finalGreen = (grayscalePixel shr 8 and 0xFF).toFloat()
            val finalBlue = (grayscalePixel and 0xFF).toFloat()

            tr = (finalRed + sepiaDepth * 2).toInt()
            tg = (finalGreen + sepiaDepth).toInt()
            tb = (finalBlue - sepiaDepth).toInt()

            tr = if (tr > 255) 255 else tr
            tg = if (tg > 255) 255 else tg
            tb = if (tb > 255) 255 else tb

            pixels[i] = Color.rgb(tr, tg, tb)
        }

        resultBitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return resultBitmap
    }

    private suspend fun GaussianBlur(image: Bitmap, radius: Int): Bitmap = coroutineScope {
        val sigma:Double = radius / 3.0
        val kernel = DoubleArray(2 * radius + 1)
        var sum: Double = 0.0

        // Создаем ядро
        for (i in -radius..radius) {
            kernel[i + radius] = exp(-(i * i) / (2 * sigma * sigma)) / (sqrt(2 * Math.PI) * sigma)
            sum += kernel[i + radius]
        }

        // Нормализуем ядро
        for (i in kernel.indices) {
            kernel[i] /= sum
        }

        val resultBitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(image.width * image.height)
        image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
        val blurredPixels = IntArray(pixels.size)

        val rows = image.height

        val deferredList = (0 until rows).map { y ->
            async(Dispatchers.Default) {
                val rowPixels = IntArray(image.width)
                for (x in 0 until image.width) {
                    var red = 0.0
                    var green = 0.0
                    var blue = 0.0
                    for (i in -radius..radius) {
                        val current_x = min(max(x + i, 0), image.width - 1)
                        val color = pixels[y * image.width + current_x]
                        val weight = kernel[i + radius]
                        red += Color.red(color) * weight
                        green += Color.green(color) * weight
                        blue += Color.blue(color) * weight
                    }
                    rowPixels[x] = Color.rgb(red.toInt(), green.toInt(), blue.toInt())
                }
                rowPixels
            }
        }

        val rowsResult = deferredList.awaitAll()

        for (y in rowsResult.indices) {
            System.arraycopy(rowsResult[y], 0, blurredPixels, y * image.width, image.width)
        }

        resultBitmap.setPixels(blurredPixels, 0, image.width, 0, 0, image.width, image.height)
        return@coroutineScope resultBitmap
    }

    fun Inversion(bitmap: Bitmap, threshold: Int): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val thresholdedValue = 255 - threshold

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = pixel and 0xff000000.toInt()
            val red = if ((pixel shr 16 and 0xff) < thresholdedValue) 255 else 0
            val green = if ((pixel shr 8 and 0xff) < thresholdedValue) 255 else 0
            val blue = if ((pixel and 0xff) < thresholdedValue) 255 else 0

            pixels[i] = alpha or (red shl 16) or (green shl 8) or blue
        }
        resultBitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return resultBitmap
    }
    fun mosaic(image: Bitmap, px: Int): Bitmap {
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
        return resultBitmap
    }

    private fun contrast(image: Bitmap, contrastVal:Int) :Bitmap{
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
    private suspend fun sharpenImage(image: Bitmap, strength: Double, threshold: Int, radius: Int): Bitmap {
        val blurredBitmap = withContext(Dispatchers.Default) {
            GaussianBlur(image, radius)
        }
        val resultBitmap = image.copy(image.config, true)

        val kernel = arrayOf(
            doubleArrayOf(-1.0, -1.0, -1.0),
            doubleArrayOf(-1.0, 9.0, -1.0),
            doubleArrayOf(-1.0, -1.0, -1.0)
        )

        for (x in 1 until image.width - 1) {
            for (y in 1 until image.height - 1) {
                val originalPixel = image.getPixel(x, y)

                var sumR = 0.0
                var sumG = 0.0
                var sumB = 0.0

                for (i in -1..1) {
                    for (j in -1..1) {
                        val blurredPixel = blurredBitmap.getPixel(x + i, y + j)

                        val weight = kernel[i + 1][j + 1]
                        sumR += weight * Color.red(blurredPixel)
                        sumG += weight * Color.green(blurredPixel)
                        sumB += weight * Color.blue(blurredPixel)
                    }
                }

                val newPixelR = (Color.red(originalPixel) + strength * (sumR - Color.red(originalPixel))).toInt()
                val newPixelG = (Color.green(originalPixel) + strength * (sumG - Color.green(originalPixel))).toInt()
                val newPixelB = (Color.blue(originalPixel) + strength * (sumB - Color.blue(originalPixel))).toInt()

                val diffR = abs(newPixelR - Color.red(originalPixel))
                val diffG = abs(newPixelG - Color.green(originalPixel))
                val diffB = abs(newPixelB - Color.blue(originalPixel))

                val diff = max(diffR, max(diffG, diffB))

                if (diff > threshold) {
                    resultBitmap.setPixel(x, y, Color.rgb(
                        newPixelR.coerceIn(0, 255),
                        newPixelG.coerceIn(0, 255),
                        newPixelB.coerceIn(0, 255)
                    ))
                } else {
                    resultBitmap.setPixel(x, y, originalPixel)
                }
            }
        }

        return resultBitmap
    }
}