package com.appli.folist.ui.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StoreViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is share Fragment"
    }
    val text: LiveData<String> = _text
}