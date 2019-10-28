package com.appli.folist.treeview.models

import com.appli.folist.NodeTypes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.annotations.Expose
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class TreeSeedNode(
    @Expose open var value: NodeValue?=null,
    @Expose open var children: RealmList<TreeSeedNode>,
    @Expose open var parent: TreeSeedNode?,
    @Expose @PrimaryKey open var uuid:String= UUID.randomUUID().toString()
): RealmObject()  {
    constructor():this(NodeValue(""), RealmList<TreeSeedNode>(),null)
    constructor(raw: RawTreeNode):this(raw,null)
    constructor(raw: RawTreeNode, parent: TreeSeedNode?):this(){
        this.value=raw.value!!
        this.parent=parent
        this.children.clear()
        raw.children.forEach {
            this.children.add(TreeSeedNode(it, this))
        }
    }
    constructor(seed: SeedNodeForFirebase):this(seed,null)
    constructor(seed: SeedNodeForFirebase, parent: TreeSeedNode?):this() {

        this.value = NodeValue(
            seed.value.str, seed.value.type, seed.value.mediaUri,
            null, seed.value.link, seed.value.power, seed.value.uuid, seed.value.checked
        )
        seed.value.detail?.forEach { (key, value) ->
            this.value?.setDetail(key, value)
        }
        //TODO: right?
//        this.uuid = seed.uuid
        this.uuid=UUID.randomUUID().toString()
        this.parent = parent
        seed.children.forEach {
            this.children.add(TreeSeedNode(it, this))
        }
    }


    data class SeedValueForFirebase(
        var str: String="",
        var type:String= NodeTypes.BINARY_NODE.name,
        var mediaUri:String?=null,
        var detail: MutableMap<String,String?>?= mutableMapOf(),
        var link:String?=null,
        var power:Int=1,
        var uuid:String="",
        var checked: Boolean=false
    ){
        constructor():this("")
        constructor(nodeValue: NodeValue):this(){
            this.str=nodeValue.str
            this.type=nodeValue.type
            this.mediaUri=nodeValue.mediaUri
            nodeValue.detail?.list?.forEach {
                this.detail?.set(it.key,it.value)
            }
            this.link=nodeValue.link
            this.power=nodeValue.power
            this.checked=nodeValue.checked
            this.uuid=nodeValue.uuid
        }
    }
    data class SeedNodeForFirebase(
        var value: SeedValueForFirebase,
        var children:MutableList<SeedNodeForFirebase>,
        var uuid:String= ""){
        constructor():this(SeedValueForFirebase(), mutableListOf())
        constructor(seed: TreeSeedNode, parent: SeedNodeForFirebase?):this(
            SeedValueForFirebase(), mutableListOf()){
            this.value=
                SeedValueForFirebase(seed.value!!)
            this.uuid=seed.uuid
            seed.children.forEach {
                this.children.add(
                    SeedNodeForFirebase(
                        it,
                        this
                    )
                )
            }
        }
    }
    fun upload(title:String="",description:String=""
               ,callback: (String?) -> Unit={}){
        val ref = FirebaseDatabase.getInstance().getReference("seeds")
        val newRef=ref.push()
        newRef.child("title").setValue(title)
        newRef.child("description").setValue(description)
        newRef.child("data").setValue(
            SeedNodeForFirebase(
                this,
                null
            )
        )
        callback(newRef.key)
    }

    fun download(key:String, cancelled:(DatabaseError)->Unit={},
                 callback:(TreeSeedNode?)->Unit){
        val ref=FirebaseDatabase.getInstance().getReference("seeds/$key/data")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val result=dataSnapshot.getValue(SeedNodeForFirebase::class.java)
                callback(result?.let { TreeSeedNode(it, null) })
//                callback(result)
            }
            override fun onCancelled(error: DatabaseError) {
                cancelled(error)
            }
        })
    }
}