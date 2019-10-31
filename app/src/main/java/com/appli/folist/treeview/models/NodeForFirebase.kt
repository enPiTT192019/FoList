package com.appli.folist.treeview.models

import java.util.*

data class NodeForFirebase(
    var value: NodeValueForFirebase,
    var children: MutableList<NodeForFirebase>,
    var uuid: String = "",
    var sharedId: String? = null,
    var progress: Double = 0.0,
    var notice: Date? = null
) {
    constructor() : this(NodeValueForFirebase(), mutableListOf())
    constructor(node: RawTreeNode) : this(node,null)
    constructor(node: RawTreeNode, parent: NodeForFirebase?) : this(
        NodeValueForFirebase(), mutableListOf()
    ) {
        this.value = NodeValueForFirebase(node.value!!)
        this.uuid = node.uuid
        this.sharedId = node.sharedId
        this.progress = node.progress
        this.notice = node.notice
        node.children.forEach {
            this.children.add(
                NodeForFirebase(
                    it,
                    this
                )
            )
        }
    }
}