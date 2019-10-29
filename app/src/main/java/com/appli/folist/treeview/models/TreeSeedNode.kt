package com.appli.folist.treeview.models

import com.algolia.search.client.ClientSearch
import com.algolia.search.model.IndexName
import com.appli.folist.NodeTypes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.annotations.Expose
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.json
import java.util.*

open class TreeSeedNode(
    @Expose open var value: NodeValue?=null,
    @Expose open var children: RealmList<TreeSeedNode>,
    @Expose open var parent: TreeSeedNode?,
    @Expose open var downloadFrom:String?=null,
    @Expose open var publishedKey:String?=null,
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
    constructor(seed: SeedNodeForFirebase,downloadFrom: String?=null):this(seed,null,downloadFrom)
    constructor(seed: SeedNodeForFirebase, parent: TreeSeedNode?,downloadFrom: String?=null):this() {

        this.value = NodeValue(
            seed.value.str, seed.value.type, seed.value.mediaUri,
            null, seed.value.link, seed.value.power, seed.value.uuid, seed.value.checked
        )
        seed.value.detail?.forEach { (key, value) ->
            this.value?.setDetail(key, value)
        }
        this.uuid=UUID.randomUUID().toString()
        this.parent = parent
        this.downloadFrom=downloadFrom
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
    fun upload(title:String="", description:String="", authorUid:String="", price:Int=0,
               password:String="", algolia: ClientSearch?,
               callback: (String?) -> Unit={}){
        val ref = FirebaseDatabase.getInstance().getReference("seeds")
        val newRef=ref.push()
        newRef.child("title").setValue(title)
        newRef.child("description").setValue(description)
        newRef.child("authorUid").setValue(authorUid)
        newRef.child("price").setValue(price)
        newRef.child("password").setValue(password)
        newRef.child("data").setValue(
            SeedNodeForFirebase(
                this,
                null
            )
        )
        //for algolia search
        //TODO
        if(algolia!=null){
            val index=algolia.initIndex(IndexName("seeds"))
            val json = json {
                "title" to title
                "description" to description
                "key" to newRef.key
                "price" to price
            }

            runBlocking {
                try {
                    index.saveObject(json)
                }catch (e:RuntimeException){
                    //TODO
                }
            }
        }

        callback(newRef.key)
    }

    fun download(key:String, cancelled:(DatabaseError)->Unit={},
                 callback:(TreeSeedNode?)->Unit){
        val ref=FirebaseDatabase.getInstance().getReference("seeds/$key/data")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val result=dataSnapshot.getValue(SeedNodeForFirebase::class.java)
                callback(result?.let { TreeSeedNode(it, key) })
            }
            override fun onCancelled(error: DatabaseError) { cancelled(error) }
        })
    }
}