package com.appli.folist.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class UserUtils(val activity: AppCompatActivity,
                val auth:FirebaseAuth,
                val LOG_TAG:String="USER_TAG"
){

    private val database = FirebaseDatabase.getInstance()

    fun createUser(
        email:String,
        password:String,
        callback:(FirebaseUser?)->Unit={
            Toast.makeText(activity.applicationContext,"user-uid:${it?.uid?:"null"}",Toast.LENGTH_SHORT).show()
        },
        whileCreate:()->Unit={
            Toast.makeText(activity.applicationContext,"please wait",Toast.LENGTH_SHORT).show()
        },
        createFailed:(task: Task<AuthResult>)->Unit={
            Toast.makeText(activity.applicationContext,"create failed:${it.exception}",Toast.LENGTH_SHORT).show()
        }
    ){
        whileCreate()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    callback(user)
                } else {
                    createFailed(task)
                    callback(null)
                }
            }
    }

    fun login(
        email:String,
        password:String,
        callback:(FirebaseUser?)->Unit={
            Toast.makeText(activity.applicationContext,"user-uid:${it?.uid?:"null"}",Toast.LENGTH_SHORT).show()
        },
        whileLogin:()->Unit={
            Toast.makeText(activity.applicationContext,"please wait",Toast.LENGTH_SHORT).show()
        },
        loginFailed:(task: Task<AuthResult>)->Unit={
            Toast.makeText(activity.applicationContext,"login failed:${it.exception}",Toast.LENGTH_SHORT).show()
        }){
        whileLogin()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    callback(user)
                } else {
                    loginFailed(task)
                    callback(null)
                }
            }
    }

    fun set(key:String,value:Any?){
        if(auth.currentUser!=null){
            val ref = database.getReference("user-info/${auth.currentUser!!.uid}")
            ref.child(key).setValue(value)
        }
    }

    fun<T> get(key:String, type:Class<T> , callback:(T?)->Unit, cancelled:(DatabaseError)->Unit={;}){
        if(auth.currentUser!=null){
            val ref=database.getReference("user-info/${auth.currentUser!!.uid}")
            ref.child(key).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    callback(dataSnapshot.getValue(type))
                }
                override fun onCancelled(error: DatabaseError) {
                    cancelled(error)
                }
            })
        }
    }
}