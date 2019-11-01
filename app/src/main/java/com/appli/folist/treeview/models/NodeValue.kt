package com.appli.folist.treeview.models
import android.util.Log
import com.appli.folist.NodeTypes
import com.appli.folist.treeview.views.Checkable
import com.google.firebase.database.DatabaseReference
import io.realm.RealmObject
import io.realm.annotations.Ignore
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
    @Ignore var firebaseRef: DatabaseReference?=null
    open var remoteUuid:String?=remoteUuid
    override var checked: Boolean=checked//いらない変数、気にしなくていい。削除予定
    open var nodeSyncedId: String? = nodeSyncedId

    open var str: String=str
        set(value) {
            if(!nodeSyncedId.isNullOrBlank() && firebaseRef!=null){

            }
            Log.d("setter","str set to $value")
            field=value
        }
    open var type:String= type
        set(value) {
            if(!nodeSyncedId.isNullOrBlank() && firebaseRef!=null){

            }
            field=value
        }
    open var mediaUri:String?=mediaUri
        set(value) {
            if(!nodeSyncedId.isNullOrBlank() && firebaseRef!=null){

            }
            field=value
        }
    open var detail: NodeDetailMap?=detail//use set & unset functions
    open var link:String?=link
        set(value) {
            if(!nodeSyncedId.isNullOrBlank() && firebaseRef!=null){

            }
            field=value
        }
    open var power:Int=power
        set(value) {
            if(!nodeSyncedId.isNullOrBlank() && firebaseRef!=null){

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
        detail?.unset(key)
    }

    fun setDetail(key:String,value:String?){
        detail?.set(key,value)
    }

}