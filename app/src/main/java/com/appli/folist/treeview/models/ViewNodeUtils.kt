package com.appli.folist.treeview.models

import com.appli.folist.R


enum class ViewNodeTypes {
    //TODO: add your node type here
    NODE,
    QUICK_CREATE_NODE,
    PROGRESS_NODE,
    ONLY_TEXT,
    TEST_NODE
}

class ViewNodeUtils{
    fun getLayout(type:Int):Int{
        return when(type){
            //TODO: add your layout here
            ViewNodeTypes.QUICK_CREATE_NODE.ordinal->R.layout.item_quick_create_node
            ViewNodeTypes.PROGRESS_NODE.ordinal->R.layout.item_progress
            ViewNodeTypes.ONLY_TEXT.ordinal->R.layout.item_only_text
            ViewNodeTypes.TEST_NODE.ordinal-> R.layout.item_node
            else-> R.layout.item_node
        }
    }
}