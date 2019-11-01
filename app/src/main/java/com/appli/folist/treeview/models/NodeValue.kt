package com.appli.folist.treeview.models
import com.appli.folist.NodeTypes
import com.appli.folist.treeview.views.Checkable
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmModule
import java.util.*

@RealmModule(allClasses = true)
open class NodeValue(
     str: String="",
     type:String= NodeTypes.BINARY_NODE.name,
     mediaUri:String?=null,
     detail: NodeDetailMap?=null,
     link:String?=null,
     power:Int=1,
     uuid:String=UUID.randomUUID().toString(),
     remoteUuid:String?=null,
     checked: Boolean=false,
     nodeSyncedId:String?=null
) : Checkable, RealmObject() {
    @PrimaryKey open var uuid:String=uuid
    open var firebaseRefPath: String?=null
    open var remoteUuid:String?=remoteUuid
    override var checked: Boolean=checked//いらない変数、気にしなくていい。削除予定
    open var nodeSyncedId: String? = nodeSyncedId


    open var str: String=str
        set(value) {
            if(!firebaseRefPath.isNullOrBlank()){
                getRef()!!.child("str").setValue(str)
            }
            field=value
        }

    private fun getRef(): DatabaseReference?{
        return if(!firebaseRefPath.isNullOrBlank()){
            FirebaseDatabase.getInstance().getReference(firebaseRefPath!!)
        }else{
            null
        }
    }

    open var type:String= type
        set(value) {
            if(!firebaseRefPath.isNullOrBlank()){
                getRef()!!.child("type").setValue(type)
            }
            field=value
        }
    open var mediaUri:String?=mediaUri
        set(value) {
            if(!firebaseRefPath.isNullOrBlank()){
                //TODO:upload image
            }
            field=value
        }
    open var detail: NodeDetailMap?=detail//use set & unset functions
    open var link:String?=link
        set(value) {
            if(!firebaseRefPath.isNullOrBlank()){
                getRef()!!.child("link").setValue(link)
            }
            field=value
        }
    open var power:Int=power
        set(value) {
            if(!firebaseRefPath.isNullOrBlank()){
                getRef()!!.child("power").setValue(power)
            }
            field=value
        }

    override fun toString(): String {
        return str
    }

    fun getDetail(key:String):String?{
        return detail?.get(key)
    }

    fun unsetDetail(key:String){
        if(!firebaseRefPath.isNullOrBlank()){
            getRef()!!.child("detail/$key").setValue(null)
        }
        detail?.unset(key)
    }

    fun setDetail(key:String,value:String?){
        if(!firebaseRefPath.isNullOrBlank()){
            getRef()!!.child("detail/$key").setValue(value)
        }
        detail?.set(key,value)
    }

}