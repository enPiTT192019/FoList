package com.appli.folist.utils

import com.appli.folist.treeview.models.NodeDetailMap
import com.appli.folist.treeview.models.ViewTreeNode
import com.appli.folist.treeview.views.SingleRecyclerViewImpl
import com.appli.folist.treeview.views.TreeAdapter
import com.appli.folist.models.NodeValue
import com.appli.folist.models.RawTreeNode
import io.realm.Realm

class NodeUtils {
    fun getRoot(realm:Realm):RawTreeNode{
        realm.beginTransaction()
        val result=realm.where(RawTreeNode::class.java).findFirst()
            ?:realm.createObject(RawTreeNode::class.java,RawTreeNode().uuid).apply {
                value=realm.createObject(NodeValue::class.java).apply {
                    str="root"
                    detail=realm.createObject(NodeDetailMap::class.java)
                }
            }
        realm.commitTransaction()
        return result
    }

    fun refreshView(view: SingleRecyclerViewImpl, root:RawTreeNode?){
        if(root!=null){
            view.setRoots(mutableListOf(ViewTreeNode(root,null,(view.adapter as TreeAdapter).viewNodes.firstOrNull())))
        }
    }
}