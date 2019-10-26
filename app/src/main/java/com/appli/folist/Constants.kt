package com.appli.folist

enum class Constants(str:String?){
//    RAW_TREE_ROOT_UUID(null)
}

enum class Tags{
    DEFAULT
}

val TAG=Tags.DEFAULT.name //for debug

enum class VariableNames{
    NODE,
    NODE_UUID
}

enum class NodeTypes{
    BINARY_NODE,
    PROGRESS_NODE,
    WEAK_NODE,
    TREE_NODE,
    TIME_NODE,//TODO
    MAP_NODE,//TODO
    QUICK_CREATE_NODE
}