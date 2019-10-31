package com.appli.folist.treeview.models


data class SeedNodeForFirebase(
    var value: NodeValueForFirebase,
    var children: MutableList<SeedNodeForFirebase>,
    var uuid: String = "",
    var sharedId: String? = null
) {
    constructor() : this(NodeValueForFirebase(), mutableListOf())
    constructor(seed: TreeSeedNode) : this(seed,null)
    constructor(seed: TreeSeedNode, parent: SeedNodeForFirebase?) : this(
        NodeValueForFirebase(), mutableListOf()
    ) {
        this.value =
            NodeValueForFirebase(seed.value!!)
        this.uuid = seed.uuid
        this.sharedId = seed.sharedId
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