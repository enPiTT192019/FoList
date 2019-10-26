package com.appli.folist.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.realm.Realm

class SharedViewModel : ViewModel() {
    val realm=MutableLiveData<Realm>()




    override fun onCleared() {
        realm.value?.close()
        super.onCleared()
    }
}