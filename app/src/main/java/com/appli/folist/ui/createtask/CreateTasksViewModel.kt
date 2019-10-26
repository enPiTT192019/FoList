package com.appli.folist.ui.createtask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateTasksViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is create Fragment"
    }
    val text: LiveData<String> = _text
}