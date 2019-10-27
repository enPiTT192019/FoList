package com.appli.folist.utils

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.realm.Realm
import io.realm.RealmConfiguration



class AppUtils {
    fun toast(activity: Context, str:String){
        Toast.makeText(activity,str,Toast.LENGTH_SHORT).show()
    }
    fun setSetting(activity: AppCompatActivity,key:String,value:String){
        val pref= PreferenceManager.getDefaultSharedPreferences(activity)
        pref.edit{
            putString(key,value)
        }
    }
    fun getSetting(activity: AppCompatActivity,key:String):String?{
        val pref= PreferenceManager.getDefaultSharedPreferences(activity)
        return pref.getString(key,null)
    }

    fun getGson():Gson{
        return GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
    }
    fun getRealm(activity: AppCompatActivity): Realm {
        Realm.init(activity)
        val config = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
        Realm.setDefaultConfiguration(config)
        return Realm.getDefaultInstance()!!
    }

    fun hideKeyboard(mActivity: Activity) {
        // Check if no view has focus:
        val view = mActivity.currentFocus
        if (view != null) {
            val inputManager = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


}