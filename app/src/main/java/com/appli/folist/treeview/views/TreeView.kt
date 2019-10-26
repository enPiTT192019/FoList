package com.appli.folist.treeview.views

import com.appli.folist.treeview.models.ViewTreeNode

private const val DEFAULT_INDENTATION_IN_DP = 16

interface TreeView<T : Checkable> {

    val indentation: Int
        get() = DEFAULT_INDENTATION_IN_DP

    fun setRoots(roots: MutableList<ViewTreeNode>)
}

data class NodeCheckedStatus(val hasChildChecked: Boolean, val allChildrenChecked: Boolean)

interface Checkable{
    var checked: Boolean
}

interface HasId {
    val id: Long
}

interface Expandable {
    var isExpanded: Boolean
}