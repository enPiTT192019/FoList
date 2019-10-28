package com.appli.folist.models

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.algolia.search.saas.Client
import com.appli.folist.treeview.models.RawTreeNode
import com.appli.folist.treeview.models.TreeSeedNode
import com.appli.folist.utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.realm.Realm

class SharedViewModel : ViewModel() {
    val realm = MutableLiveData<Realm>()
    private val auth =  FirebaseAuth.getInstance()
    val user=MutableLiveData<FirebaseUser>()
    val root=MutableLiveData<RawTreeNode>()
    val seedRoot=MutableLiveData<TreeSeedNode>()
    val algolia=MutableLiveData<Client>()

    fun login(activity: AppCompatActivity,email:String,password:String ) {
        UserUtils(activity, auth).login(email,password,
            callback = {
                Toast.makeText(activity, "user-uid:${it?.uid ?: "null"}", Toast.LENGTH_SHORT).show()
                user.value = it
            }
        )
    }

    override fun onCleared() {
        realm.value?.close()
        super.onCleared()
    }
}