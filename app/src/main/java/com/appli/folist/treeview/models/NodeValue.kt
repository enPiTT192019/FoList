package com.appli.folist.treeview.models
import com.appli.folist.NodeTypes
import com.appli.folist.treeview.models.NodeDetailMap
import com.appli.folist.treeview.views.Checkable
import com.google.gson.annotations.Expose
import io.realm.RealmObject
import io.realm.annotations.RealmModule
import java.util.*

@RealmModule(allClasses = true)
open class NodeValue(
    @Expose open var str: String="",
    @Expose open var type:String= NodeTypes.NODE.name,
    @Expose open var mediaUri:String?=null,
    @Expose open var detail: NodeDetailMap?=null,
    @Expose open var link:String?=null,
    @Expose open var power:Int=1,
    @Expose open var uuid:String=UUID.randomUUID().toString(),
    @Expose override var checked: Boolean=false
) : Checkable, RealmObject() {

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