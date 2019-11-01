package com.appli.folist.treeview.models

import android.util.Log
import com.appli.folist.utils.executeTransactionIfNotInTransaction
import com.google.firebase.database.*
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmModule
import java.util.*

@RealmModule(allClasses = true)
open class RawTreeNode(
    value: NodeValue? = null,
    children: RealmList<RawTreeNode>,
    parent: RawTreeNode? = null,
    progress: Double = 0.0,
    notice: Date? = null,
    sharedId: String? = null,
    syncedId: String? = null,
    uuid: String = UUID.randomUUID().toString()
) : RealmObject() {
    @Ignore var refreshView:((RawTreeNode)->Unit)?=null
    @Ignore var firebaseRef:DatabaseReference?=null

    @PrimaryKey open var uuid: String = uuid//cannot be changed
    open var value: NodeValue?=value
    open var parent: RawTreeNode? = parent
    open var syncedId: String? = syncedId
    open var children: RealmList<RawTreeNode> =children
        fun addChild(child:RawTreeNode){
            if(!syncedId.isNullOrBlank() && firebaseRef!=null){
                firebaseRef!!.child("${children.size}")
            }
            children.add(child)
        }
    open var progress: Double = progress
        set(value) {
            if(!syncedId.isNullOrBlank() && firebaseRef!=null){

            }
            field=value
        }
    open var notice: Date? = notice
        set(value) {
            if(!syncedId.isNullOrBlank() && firebaseRef!=null){

            }
            field=value
        }
    open var sharedId: String? = sharedId
        set(value) {
            if(!syncedId.isNullOrBlank() && firebaseRef!=null){

            }
            field=value
        }


    constructor() : this(children = RealmList<RawTreeNode>())
    constructor(value: NodeValue) : this(value, children = RealmList<RawTreeNode>())
    constructor(value: NodeValue, parent: RawTreeNode?) : this(
        value,
        children = RealmList<RawTreeNode>(),
        parent = parent
    )
    constructor(nodeRoot: TreeSeedNode) : this(nodeRoot, null)
    constructor(nodeRoot: TreeSeedNode, parent: RawTreeNode? = null) : this() {
        this.value = nodeRoot.value
        this.progress = 0.0
        this.parent = parent
        this.notice = null
        this.sharedId = null
        this.uuid = UUID.randomUUID().toString()
        this.children.clear()
        this.sharedId = nodeRoot.sharedId
        nodeRoot.children.forEach {
            this.addChild(RawTreeNode(it, this))
        }
    }

    constructor(remoteNode: NodeForFirebase) : this(remoteNode, null, null)
    constructor(remoteNode: NodeForFirebase, parent: RawTreeNode?, realm: Realm?) : this() {
        reset(remoteNode, parent, realm)
        setSync()
    }


    fun reset(
        remoteNode: NodeForFirebase,
        parent: RawTreeNode?,
        realm: Realm?
    ) {
        if (realm == null) {
            Log.e("realm", "no realm")
            return
        }
        realm.executeTransactionIfNotInTransaction {
            val uuid = UUID.randomUUID().toString()
            val nodeValue = realm.createObject(NodeValue::class.java, uuid).apply {
                str = remoteNode.value.str
                type = remoteNode.value.type
                mediaUri = remoteNode.value.mediaUri
                detail = null
                link = remoteNode.value.link
                power = remoteNode.value.power
                remoteUuid = remoteNode.value.uuid
                checked = remoteNode.value.checked
            }
            this.value = nodeValue
            //detail
            remoteNode.value.detail?.forEach { (key, value) ->
                this.value?.setDetail(key, value)
            }
            this.parent = parent
            this.sharedId = remoteNode.sharedId
            this.progress = remoteNode.progress
            this.notice = remoteNode.notice
            this.children=RealmList()
            remoteNode.children.forEach { n ->
                this.addChild(RawTreeNode(n, this, realm))
            }
        }
    }

    init {
        setSync()
    }


    fun findFirstSyncedNode(): RawTreeNode? {
        if (!syncedId.isNullOrBlank()) {
            return this
        } else if (parent == null) {
            return null
        } else {
            return parent!!.findFirstSyncedNode()
        }
    }

    private fun setSync() {
        if (!syncedId.isNullOrBlank() && this.firebaseRef!=null) {
            this.firebaseRef!!.child("data").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val newRemoteNode = dataSnapshot.getValue(NodeForFirebase::class.java)
                    if (newRemoteNode != null) {
                        Log.d("firebase", "data changed:${newRemoteNode.value.str}")
                        reset(newRemoteNode, null, getRealm(this@RawTreeNode))
                        if(refreshView!=null) refreshView?.invoke(this@RawTreeNode)
                    }
                }
            })
        }
    }

    fun upload(realm: Realm,syncedId:String?=null,callback: (String?) -> Unit = {}) {
        val ref = FirebaseDatabase.getInstance().getReference("synced-nodes")
        var newRef:DatabaseReference
        newRef = if(syncedId.isNullOrBlank()){
            ref.push()
        }else{
            ref.child(syncedId)
        }
        newRef.child("data").setValue(NodeForFirebase(this))
        realm.executeTransactionIfNotInTransaction {
            this.syncedId=syncedId?: newRef.key
            value!!.nodeSyncedId=this.syncedId
        }
        this.firebaseRef = FirebaseDatabase.getInstance().getReference("synced-nodes/${this.syncedId}/data")
        this.value!!.firebaseRef=this.firebaseRef!!.child("value")
        fun setChildrenFirebaseRef(node:RawTreeNode,index:Int,ref:DatabaseReference){
            node.firebaseRef=ref.child("children/$index")
            node.value!!.firebaseRef=node.firebaseRef!!.child("value")
            if(node.children.size>0){
                node.children.forEachIndexed { index, rawTreeNode ->
                    setChildrenFirebaseRef(rawTreeNode,index,this.firebaseRef!!)
                }
            }
        }
        this.children.forEachIndexed { index, rawTreeNode ->
            setChildrenFirebaseRef(rawTreeNode,index,this.firebaseRef!!)
        }
        setSync()
        callback(newRef.key)
    }

    override fun toString(): String {
        return value.toString()
    }

    fun calcProgress(): Double {
        this.progress = if (children.size >= 1) {
            ((children.sumByDouble { it.calcProgress() }) / getSumOfPower()) * this.value!!.power
        } else {
            this.progress
        }
        return this.progress
    }

    fun getRoot(): RawTreeNode {
        var result = this
        while (result.parent != null) result = result.parent!!
        return result
    }

    private fun getSumOfPower(): Int {
        return children.sumBy { it.value!!.power }
    }

}