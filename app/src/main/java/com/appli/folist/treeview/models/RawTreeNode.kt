package com.appli.folist.treeview.models

import android.util.Log
import com.appli.folist.utils.executeTransactionIfNotInTransaction
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.annotations.Expose
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmModule
import java.util.*

@RealmModule(allClasses = true)
open class RawTreeNode(
    @Expose open var value: NodeValue? = null,
    @Expose open var children: RealmList<RawTreeNode>,
    @Expose open var parent: RawTreeNode? = null,
    @Expose open var progress: Double = 0.0,
    @Expose open var notice: Date? = null,
    @Expose open var sharedId: String? = null,
    @Expose open var syncedId: String? = null,
    @Expose @PrimaryKey open var uuid: String = UUID.randomUUID().toString()
) : RealmObject() {
    @Ignore var refreshView:((RawTreeNode)->Unit)?=null
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
            this.children.add(RawTreeNode(it, this))
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
            this.children=RealmList<RawTreeNode>()
            remoteNode.children.forEach { n ->
                this.children.add(RawTreeNode(n, this, realm))
            }
        }
//        this.setSync()
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

    fun setSync() {
        if (!syncedId.isNullOrBlank()) {
            val ref = FirebaseDatabase.getInstance().getReference("synced-nodes/$syncedId/data")
            ref.addValueEventListener(object : ValueEventListener {
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

    fun edited() {
        //TODO
//        if(... shareId!=null){
//            upload()
//        }
    }

    fun upload(realm: Realm,callback: (String?) -> Unit = {}) {
        val ref = FirebaseDatabase.getInstance().getReference("synced-nodes")
        val newRef = ref.push()
        newRef.child("data").setValue(NodeForFirebase(this))
        realm.executeTransactionIfNotInTransaction {
            syncedId=newRef.key
        }
        setSync()
        callback(newRef.key)
    }

    fun restore() {
        //TODO

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

    fun getSumOfPower(): Int {
        return children.sumBy { it.value!!.power }
    }

}