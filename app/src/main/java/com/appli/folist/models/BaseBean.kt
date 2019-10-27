package com.appli.folist.models

class BaseBean {
    //for spinner
    var Name: String? = null
    var Id: Int = 0

    override fun toString(): String {
        return Name?:""
    }
}