package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _selectedFileUri = MutableLiveData<String?>()
    val selectedFileUri: LiveData<String?> = _selectedFileUri

    fun selectFile(uri: String?){
        _selectedFileUri.value = uri
    }
}