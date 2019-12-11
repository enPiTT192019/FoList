package com.appli.folist.treeview.models

import java.util.*

data class NodeForFirebase(
    var value: NodeValueForFirebase,
    var children: HashMap<String,NodeForFirebase>,
    var uuid: String = "",
    var sharedId: String? = null,
    var progress: Double? = 0.0,
    var notice: Date? = null,
    var path:String?=null
) {
    constructor() : this(NodeValueForFirebase(), hashMapOf())
    constructor(node: RawTreeNode) : this(node,null)
    constructor(node: RawTreeNode, parent: NodeForFirebase?) : this(
        NodeValueForFirebase(), hashMapOf()
    ) {
        this.value = NodeValueForFirebase(node.value!!)
        this.uuid = node.uuid
        this.sharedId = node.sharedId
        this.progress = node.progress
        this.notice = node.notice
        this.path=node.firebaseRefPath
//        node.children.forEach {
//            this.children.add(
//                NodeForFirebase(
//                    it,
//                    this
//                )
//            )
//        }
    }

    override fun toString(): String {
        return this.value.str
    }
}