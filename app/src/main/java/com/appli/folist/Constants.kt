package com.appli.folist

val ALGOLIA_APP_ID="2HDQPZXROG"
val ALGOLIA_API_KEY="3cd584b8253847a23a50cbaf50afb2cc"//read only

val STORE_SHOW_LATEST_NUM=10

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