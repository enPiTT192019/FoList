package com.appli.folist.ui.seeds

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SeedsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is seeds Fragment"
    }
    val text: LiveData<String> = _text
}