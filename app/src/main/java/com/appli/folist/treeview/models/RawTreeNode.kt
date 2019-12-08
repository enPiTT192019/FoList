package com.appli.folist.treeview.models

import android.annotation.SuppressLint
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
    progress: Double? = 0.0,
    notice: Date? = null,
    sharedId: String? = null,
    syncedId: String? = null,
    uuid: String = UUID.randomUUID().toString(),
    mRealm: Realm?=null
) : RealmObject() {

    @Ignore
    var refreshView: ((RawTreeNode) -> Unit)? = null
    @Ignore
    var refreshChildAdded: ((RawTreeNode,ViewTreeNode,RawTreeNode) -> Unit)? = null
    @Ignore
    var refreshChildRemoved: ((RawTreeNode,ViewTreeNode) -> Unit)? = null
    @Ignore
    var viewNodeRef:ViewTreeNode?=null


    @Ignore var mRealm: Realm?=mRealm
    open var firebaseRefPath: String? = null
    set(value) {
        field=value
        if (value != null) {
//            getRef()?.addListenerForSingleValueEvent(object :ValueEventListener{
//                override fun onCancelled(p0: DatabaseError) {}
//                override fun onDataChange(p0: DataSnapshot) {
//                    val remote = p0.getValue(NodeForFirebase::class.java)
//                    if(remote!=null){
//                        reset(remote, parent, mRealm)
//                        setSync()
//                    }
//                }
//            })
        }
    }
    public fun getRef(): DatabaseReference? {
        return if (!firebaseRefPath.isNullOrBlank()) {
            FirebaseDatabase.getInstance().getReference(firebaseRefPath!!)
        } else {
            null
        }
    }

    @PrimaryKey
    open var uuid: String = uuid//cannot be changed
    open var value: NodeValue? = value
    open var parent: RawTreeNode? = parent
    open var syncedId: String? = syncedId
    open var children: RealmList<RawTreeNode> = children
    fun addChild(child: RawTreeNode, needUpload: Boolean = true) {
        Log.d("firebase","raw child added:$child")
        if (!this.firebaseRefPath.isNullOrBlank() && needUpload) {
            val key = getRef()!!.child("children").push().key
            child.upload("$firebaseRefPath/children/$key")
        }
        if (child.uuid !in children.map { it.uuid }) {
            mRealm?.executeTransactionIfNotInTransaction {
                children.add(child)
            }
        }
    }

    fun removeChild(child: RawTreeNode, needUpload: Boolean = true) {
        val ref=child.getRef()
        child.firebaseRefPath=null
        mRealm?.executeTransactionIfNotInTransaction {
            children.remove(child)
        }
        if (ref!=null&& needUpload) {
            ref.removeValue()
        }
    }

    fun removeAllChild(needUpload: Boolean = true, condition: (RawTreeNode) -> Boolean) {
        if (!this.firebaseRefPath.isNullOrBlank() && needUpload) {
            children.filter(condition).forEach {child->
                val ref=child.getRef()
                child.firebaseRefPath=null
                children.remove(child)
                ref?.removeValue()
            }
        }

        mRealm?.executeTransactionIfNotInTransaction {
            children.removeAll(condition)

        }
    }

    open var progress: Double? = progress
    open var notice: Date? = notice
    open var sharedId: String? = sharedId

    constructor() : this(null)
    constructor(mRealm: Realm?) : this(children = RealmList<RawTreeNode>(), mRealm = mRealm)
    constructor(value: NodeValue, mRealm: Realm?) : this(value, children = RealmList<RawTreeNode>(), mRealm = mRealm)
    constructor(value: NodeValue, parent: RawTreeNode?,mRealm: Realm?) : this(
        value,
        children = RealmList<RawTreeNode>(),
        parent = parent,
                mRealm = mRealm
    )

    constructor(nodeRoot: TreeSeedNode,mRealm: Realm?) : this(nodeRoot, null,mRealm = mRealm)
    constructor(nodeRoot: TreeSeedNode, parent: RawTreeNode? = null,mRealm: Realm?) : this(mRealm = mRealm) {
        this.value = nodeRoot.value
        this.progress = 0.0
        this.parent = parent
        this.notice = null
        this.sharedId = null
        this.uuid = UUID.randomUUID().toString()
        this.children.clear()
        this.sharedId = nodeRoot.sharedId
        nodeRoot.children.forEach {
            this.addChild(RawTreeNode(it, this,mRealm))
        }
    }

    constructor(remoteNode: NodeForFirebase, parent: RawTreeNode?, realm: Realm?) : this(mRealm = realm) {
        resetWithoutChildren(remoteNode, parent, realm)
        setSync()
    }

    @SuppressLint("NewApi")
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
             uuid = remoteNode.uuid

            val nodeValue = realm.where(NodeValue::class.java).equalTo("uuid", remoteNode.value.uuid).findFirst()
                ?:realm.createObject(NodeValue::class.java, remoteNode.value.uuid)
            nodeValue.apply {
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
            this.firebaseRefPath = remoteNode.path
//            this.children = RealmList()
//            this.removeAllChild { true }
            remoteNode.children.forEach { (k, v) ->
                this.addChild(RawTreeNode(v, this, realm),needUpload = false)
            }
        }
    }

    @SuppressLint("NewApi")
    fun resetWithoutChildren(
        remoteNode: NodeForFirebase,
        parent: RawTreeNode?,
        realm: Realm?
    ) {
        if (realm == null) {
            Log.e("realm", "no realm")
            return
        }
        realm.executeTransactionIfNotInTransaction {
            uuid = remoteNode.uuid

            val nodeValue = realm.where(NodeValue::class.java).equalTo("uuid", remoteNode.value.uuid).findFirst()
                ?:realm.createObject(NodeValue::class.java, remoteNode.value.uuid)
            nodeValue.apply {
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
            this.firebaseRefPath = remoteNode.path
        }
    }

    init {
        if (firebaseRefPath != null) {
            getRef()?.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {}
                 override fun onDataChange(p0: DataSnapshot) {
                     val remote = p0.getValue(NodeForFirebase::class.java)
                     if(remote!=null){
                         resetWithoutChildren(remote, parent, mRealm)
                         setSync()
                     }
                }

            })
        }
    }




    fun findFirstSyncedNode(): RawTreeNode? {
        return if (!syncedId.isNullOrBlank()) {
            this
        } else if (parent == null) {
            null
        } else {
            parent!!.findFirstSyncedNode()
        }
    }
    fun setSync() {
        if (firebaseRefPath != null) {
            val ref = getRef()
            if (ref != null) {
                val realm = this.realm
                ref.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val remote = dataSnapshot.getValue(NodeForFirebase::class.java)
                        if (remote != null&& !remote.value.str.isNullOrBlank()) {
                            Log.d("firebase", "node changed:${remote.toString()}")
                            mRealm?.executeTransactionIfNotInTransaction {
                                if(this@RawTreeNode.progress != remote.progress
                                    ||this@RawTreeNode.notice != remote.notice
                                    ||this@RawTreeNode.sharedId != remote.sharedId){
                                    this@RawTreeNode.progress = remote.progress
                                    this@RawTreeNode.notice = remote.notice
                                    this@RawTreeNode.sharedId = remote.sharedId
                                    this@RawTreeNode.refreshView?.invoke(this@RawTreeNode)
                            }
                            }
                        }
                    }
                })
                ref.child("value").addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val remote = dataSnapshot.getValue(NodeValueForFirebase::class.java)
                        if (remote != null && !remote.str.isNullOrBlank()) {
                            Log.d("firebase", "value changed:$remote")
                            mRealm?.executeTransactionIfNotInTransaction {
                                this@RawTreeNode.value!!.str = remote.str
                                this@RawTreeNode.value!!.type = remote.type
//                                this@RawTreeNode.value!!.mediaUri = remote.mediaUri//TODO
//                                this@RawTreeNode.value!!.detail = remote.detail//TODO
                                this@RawTreeNode.value!!.link = remote.link
                                this@RawTreeNode.value!!.power = remote.power
                                this@RawTreeNode.refreshView?.invoke(this@RawTreeNode)
                            }
                        }
                    }
                })
                ref.child("children").addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

                    override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                        val remote = dataSnapshot.getValue(NodeForFirebase::class.java)
                        if (remote != null
                            && remote.uuid !in this@RawTreeNode.children.map { it.uuid }
                            && !remote.value.str.isNullOrBlank()) {
                            Log.d("firebase", "child added:$remote")
                            val newChild=RawTreeNode(
                                remote,
                                this@RawTreeNode,
                                this@RawTreeNode.mRealm
                            )

                            newChild.refreshView=this@RawTreeNode.refreshView
                            newChild.refreshChildAdded=this@RawTreeNode.refreshChildAdded
                            newChild.refreshChildRemoved=this@RawTreeNode.refreshChildRemoved

                            this@RawTreeNode.addChild(
                                newChild, needUpload = false
                            )
                            this@RawTreeNode.refreshChildAdded?.invoke(this@RawTreeNode,this@RawTreeNode.viewNodeRef!!,newChild)
                        }
                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                        val remote = dataSnapshot.getValue(NodeForFirebase::class.java)
                        Log.d("firebase", "child removed:$remote")
                        if (remote != null
                            && remote.uuid in this@RawTreeNode.children.map { it.uuid }
                            && !remote.value.str.isNullOrBlank()) {
                            var v:ViewTreeNode?=null
                            this@RawTreeNode.children.forEach {
                                if(it.uuid==remote.uuid){
                                    v=it.viewNodeRef
                                }
                            }
                            this@RawTreeNode.removeAllChild(needUpload = false) { it.uuid == remote.uuid }
                            if(v!=null){
                                this@RawTreeNode.refreshChildRemoved?.invoke(this@RawTreeNode,v!!)
                            }
                        }else{
                            Log.d("firebase","child removed but do nothing")
                        }
                    }
                })
            }
        }
    }

    fun upload(refPath: String? = null, callback: (String?) -> Unit = {}) {
        if(this.value!!.str.isNullOrBlank())return
        Log.d("firebase","upload:$this")
        var ref = FirebaseDatabase.getInstance().getReference("")
        var syncedId: String? = null
        if (refPath.isNullOrBlank()) {
            ref = ref.child("synced-nodes").push()
            syncedId = ref.key
            ref = ref.child("data")
            this.firebaseRefPath = "synced-nodes/$syncedId/data"
        } else {
            ref = ref.child(refPath)
            this.firebaseRefPath = refPath
        }
        ref.setValue(NodeForFirebase(this))

        this.children.forEach {
            if(it.firebaseRefPath.isNullOrBlank()){
                it.firebaseRefPath=FirebaseDatabase.getInstance().getReference(firebaseRefPath!!).push().key
            }
            it.upload("${this.firebaseRefPath}/children/${it.firebaseRefPath}")
        }
        this.syncedId = syncedId
        value!!.nodeSyncedId = this.syncedId

        setSync()
        if (syncedId != null) callback(syncedId)
    }

    override fun toString(): String {
        return value.toString()
    }

    fun calcProgress(): Double {
        return if (children.size >= 1) {
            ((children.sumByDouble { it.calcProgress() }) / getSumOfPower()) * this.value!!.power
        } else {
            this.progress!!
        }
//        return this.progress
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