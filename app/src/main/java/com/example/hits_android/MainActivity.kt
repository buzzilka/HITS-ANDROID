package com.example.hits_android

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.myapplication.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.selectFile(uri.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val image: ImageView = findViewById(R.id.currentImage)
        viewModel.selectedFileUri.observe(this) { uri ->
            if (uri != null) {
                Glide.with(this)
                    .load(uri)
                    .into(image)
            }
        }

        val galleryButton: ImageButton = findViewById(R.id.gallery)
        galleryButton.setOnClickListener {
            pickFilesFromGallery()
        }

        val cameraButton: ImageButton = findViewById(R.id.camera)
        cameraButton.setOnClickListener {

        }
    }

    private fun pickFilesFromGallery() {
        filePickerLauncher.launch("image/*")
    }


}