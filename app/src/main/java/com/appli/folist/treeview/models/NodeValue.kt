package com.appli.folist.treeview.models
import com.appli.folist.NodeTypes
import com.appli.folist.treeview.views.Checkable
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
    open var remoteUuid:String?=remoteUuid
    override var checked: Boolean=checked//いらない変数、気にしなくていい。削除予定
    open var nodeSyncedId: String? = nodeSyncedId


    open var str: String=str
    open var type:String= type
    open var mediaUri:String?=mediaUri
    open var detail: NodeDetailMap?=detail//use set & unset functions
    open var link:String?=link
    open var power:Int=power

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
        if(detail==null)detail=NodeDetailMap()
        detail?.set(key,value)
    }

}