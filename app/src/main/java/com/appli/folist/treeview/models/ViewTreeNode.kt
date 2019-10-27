package com.appli.folist.treeview.models

import com.appli.folist.NodeTypes
import com.appli.folist.treeview.utils.IdGenerator
import com.appli.folist.treeview.views.Expandable
import com.appli.folist.treeview.views.HasId
import com.appli.folist.treeview.views.NodeCheckedStatus
import com.google.gson.annotations.Expose
import io.realm.Realm

class ViewTreeNode(
    @Expose var value: NodeValue,
    var type:ViewNodeTypes=ViewNodeTypes.NODE,
    var parent: ViewTreeNode?,
    @Expose var children: MutableList<ViewTreeNode>,
    @Expose override var isExpanded: Boolean =false,
    var rawReference: RawTreeNode?=null
) : HasId, Expandable {
    override val id: Long by lazy {
        IdGenerator.generate()
    }
    // constructor for root node
    constructor(value: NodeValue) : this(value,ViewNodeTypes.NODE, null,  mutableListOf<ViewTreeNode>())
    // constructor for leaf node
    constructor(value: NodeValue, parent: ViewTreeNode) : this(value,ViewNodeTypes.NODE, parent,  mutableListOf<ViewTreeNode>())
    // constructor for parent node
    constructor(value: NodeValue, children: MutableList<ViewTreeNode>) : this(value,ViewNodeTypes.NODE, null, children)

    constructor(raw: RawTreeNode, parent:ViewTreeNode?=null,
                before:ViewTreeNode?=null,onlyText:Boolean=false):this(NodeValue()){
        this.parent=parent
        this.isExpanded=(before!=null && before.isExpanded)
        this.value=raw.value!!
        this.children.clear()
        this.type=when(value.type){
            NodeTypes.PROGRESS_NODE.name->ViewNodeTypes.PROGRESS_NODE
            else->ViewNodeTypes.NODE
        }
        if(onlyText)this.type=ViewNodeTypes.ONLY_TEXT
        this.rawReference=raw
        raw.children.forEach {
            val childBefore= before?.children?.findLast {it2->
                it2.value.uuid== it.value?.uuid
            }
            this.children.add(ViewTreeNode(it,this,childBefore,onlyText=onlyText))
        }
        if(!onlyText)this.children.add(ViewTreeNode(NodeValue(checked = true),ViewNodeTypes.QUICK_CREATE_NODE,this, mutableListOf()))
    }

    fun isTop(): Boolean {
        return parent == null
    }
    fun isLeaf(): Boolean {
        return children.isEmpty()
//        return children.size<=1//1: quick create node
    }

    fun getLevel(): Int {
        fun stepUp (viewNode: ViewTreeNode): Int {
            return viewNode.parent?.let { 1 + stepUp(it) } ?: 0
        }
        return stepUp(this)
    }
    fun setChecked(isChecked: Boolean,realm: Realm?,viewNode:ViewTreeNode) {
//        realm?.executeTransactionIfNotInTransaction{
//            viewNode.value.checked=rawReference?.progress?:0>0
//            viewNode.children.forEach {
//                it.rawReference?.progress=if(rawReference?.progress?:0>0)1 else 0
//                setChecked(isChecked,realm,it)
//            }
//        }
    }
    fun getCheckedStatus(): NodeCheckedStatus {
        if(type==ViewNodeTypes.QUICK_CREATE_NODE) {
            return NodeCheckedStatus(false, true)
        }
        //1:quick create
        if (children.size<=1) return NodeCheckedStatus(value.checked, value.checked)
        var hasChildChecked = false
        var allChildrenChecked = true
        children.forEach {
            val checkedStatus = it.getCheckedStatus()
            hasChildChecked = hasChildChecked || checkedStatus.hasChildChecked
            allChildrenChecked = allChildrenChecked && checkedStatus.allChildrenChecked
        }
        return NodeCheckedStatus(hasChildChecked, allChildrenChecked)
    }
    fun getAggregatedValues(): List<NodeValue> {
        return if (isLeaf()) {
            if (value.checked) listOf(value) else emptyList()
        } else {
            if (getCheckedStatus().allChildrenChecked) {
                listOf(value)
            } else {
                val result = mutableListOf<NodeValue>()
                children.forEach {
                    result.addAll(it.getAggregatedValues())
                }
                result
            }
        }
    }
    fun getRoot(): ViewTreeNode {
        var result=this
        while(result.parent!=null)result= result.parent!!
        return result
    }
//    fun deleteRaw(realm: Realm){
//        if(parent!=null){
//            children.filter { type==ViewNodeTypes.QUICK_CREATE_NODE }.forEach {
//                it.deleteRaw(realm)
//            }
//            parent!!.children.remove(this)
//            AppUtils().executeTransactionIfNotInTransaction(realm){
//                rawReference!!.parent!!.children.remove(rawReference)
////                realm.where(RawTreeNode::class.java).equalTo("uuid",rawReference!!.uuid)
////                    .findAllAsync().deleteAllFromRealm()
//            }
//        }
//    }
    fun toList():MutableList<ViewTreeNode>{
        var result= mutableListOf(this)
        children.forEach {
            result.addAll(it.toList())
        }
        return result
    }

    fun getDisplayedNodeNumber():Int{
        if(!isExpanded){
            return 1
        }else{
            return 1+children.sumBy { it.getDisplayedNodeNumber() }
        }
    }
}