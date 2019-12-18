package com.appli.folist.utils

import com.appli.folist.NodeTypes
import com.appli.folist.treeview.models.*
import com.appli.folist.treeview.views.SingleRecyclerViewImpl
import com.appli.folist.treeview.views.TreeAdapter
import io.realm.Realm

class NodeUtils {
    fun getRoot(realm: Realm): RawTreeNode {
        val inTransaction=realm.isInTransaction
        if(!inTransaction)realm.beginTransaction()
        var result = realm.where(RawTreeNode::class.java).findFirst()
        //取得できない場合（初めて起動するとき）、Rootノードを作る
        if(result==null){
            result= RawTreeNode(NodeValue("root"),realm)
            realm.copyToRealmOrUpdate(result)
        }
        if(!inTransaction)realm.commitTransaction()

        fun setRealm(n:RawTreeNode,realm:Realm){
            n.mRealm=realm
            n.children.forEach {
                setRealm(it,realm)
            }
        }
        setRealm(result,realm)
        result.mRealm=realm
        return result!!
    }

    fun clearAllNodesForTest(realm: Realm) {
        realm.executeTransaction {
            realm.deleteAll()
        }
    }

    fun findTask(root: RawTreeNode, id: String): RawTreeNode? {
        return root.children.find { it.uuid == id }
    }


    private fun _getNodeFromRoot(node: RawTreeNode, id: String): RawTreeNode? {
        return when {
            node.uuid == id -> node
            node.children.size > 0 -> node.children.find { _getNodeFromRoot(it, id) != null }
            else -> null
        }
    }

    fun getNode(realm: Realm, id: String): RawTreeNode? {
        var result: RawTreeNode? = null
        realm.executeTransactionIfNotInTransaction {
            result = realm.where(RawTreeNode::class.java).equalTo("uuid", id).findFirst()
        }
        return result
    }

    fun refreshView(view: SingleRecyclerViewImpl, root: RawTreeNode?) {
        if (root != null) {
            view.setRoots(
                mutableListOf(
                    ViewTreeNode(
                        root,
                        null,
                        (view.adapter as TreeAdapter).viewNodes.firstOrNull(),
                        position = null
                    )
                )
            )
        }
    }

    fun refreshViewWithOnlyText(view: SingleRecyclerViewImpl, root: RawTreeNode?){
        if (root != null) {
            view.setRoots(
                mutableListOf(
                    ViewTreeNode(
                        root,
                        null,
                        (view.adapter as TreeAdapter).viewNodes.firstOrNull(),
                        onlyText = true,
                        position = null
                    )
                )
            )
        }
    }

    fun expandAll(node: ViewTreeNode) {
        node.isExpanded = true
        node.children.forEach { expandAll(it) }
    }

    fun expandAllExceptLeaves(node: ViewTreeNode) {
        node.isExpanded = true
        node.children.forEach {
            if (it.children.size > 1 && it.value!!.type!=NodeTypes.TEST_NODE.name) {
                expandAllExceptLeaves(it)
            }
        }
    }

    fun getSeedRoot(realm: Realm): TreeSeedNode {
        realm.beginTransaction()
        val result = realm.where(TreeSeedNode::class.java).findFirst()
        //取得できない場合（初めて起動するとき）、Rootノードを作る
            ?: realm.createObject(
                TreeSeedNode::class.java,
                TreeSeedNode().uuid
            ).apply {
                val value=NodeValue("root").apply {
                    str = "root"
                    detail = realm.createObject(NodeDetailMap::class.java)
                }
                realm.copyToRealmOrUpdate(value)
            }
        realm.commitTransaction()
        return result
    }

    fun searchSeedByName(){
        //TODO
    }
}