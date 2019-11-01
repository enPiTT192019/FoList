package com.appli.folist.treeview.models

import com.appli.folist.NodeTypes


data class NodeValueForFirebase(
    var str: String = "",
    var type: String = NodeTypes.BINARY_NODE.name,
    var mediaUri: String? = null,
    var detail: MutableMap<String, String?>? = mutableMapOf(),
    var link: String? = null,
    var power: Int = 1,
    var uuid: String = "",
    var checked: Boolean = false,
    var path:String?=null
) {
    constructor() : this("")
    constructor(nodeValue: NodeValue) : this() {
        this.str = nodeValue.str
        this.type = nodeValue.type
        this.mediaUri = nodeValue.mediaUri
        nodeValue.detail?.list?.forEach {
            this.detail?.set(it.key, it.value)
        }
        this.link = nodeValue.link
        this.power = nodeValue.power
        this.checked = nodeValue.checked
        this.uuid = nodeValue.uuid
        this.path=nodeValue.firebaseRefPath
    }
}